package com.group1.banking.service.chat.vectorstore;

import com.group1.banking.entity.KnowledgeArticle;
import com.group1.banking.enums.ChatTopic;
import com.group1.banking.service.chat.SafeChatContext;
import com.group1.banking.service.chat.embedding.EmbeddingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The actual retrieval and indexing logic against the separate
 * Postgres+pgvector instance. knowledge_embedding is a self-contained,
 * denormalized copy of the curated articles (article_id, title, content,
 * topic, embedding) -- deliberately not requiring a cross-database join
 * back to the app's own H2 knowledge_article table at query time.
 */
@Component
public class PgVectorKnowledgeStore {

    private static final Logger log = LoggerFactory.getLogger(PgVectorKnowledgeStore.class);

    private final PgVectorConnectionPool connectionPool;
    private final EmbeddingProvider embeddingProvider;

    public PgVectorKnowledgeStore(PgVectorConnectionPool connectionPool, EmbeddingProvider embeddingProvider) {
        this.connectionPool = connectionPool;
        this.embeddingProvider = embeddingProvider;
    }

    /**
     * Embeds the customer's question and runs a cosine-similarity search
     * (ORDER BY embedding <=> ?::vector) against knowledge_embedding,
     * filtered to the classified topic, returning up to topK snippets.
     *
     * If pgvector isn't configured, or the query throws a SQLException,
     * this returns an empty list rather than failing the chat turn -- a
     * pgvector outage just means no cited tip shows up, nothing more.
     */
    public List<SafeChatContext.KnowledgeSnippet> query(String rawQuery, ChatTopic topic, int topK) {
        if (!connectionPool.isConfigured()) {
            return List.of();
        }

        String vectorLiteral = toVectorLiteral(embeddingProvider.embed(rawQuery));
        String sql = "SELECT title, content FROM knowledge_embedding "
                + "WHERE topic = ? ORDER BY embedding <=> ?::vector LIMIT ?";

        List<SafeChatContext.KnowledgeSnippet> results = new ArrayList<>();
        try (Connection connection = connectionPool.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, topic.name());
            statement.setString(2, vectorLiteral);
            statement.setInt(3, topK);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(new SafeChatContext.KnowledgeSnippet(rs.getString("title"), rs.getString("content")));
                }
            }
        } catch (SQLException ex) {
            log.warn("pgvector knowledge query failed, degrading to no cited tips: {}", ex.toString());
            return List.of();
        }
        return results;
    }

    /**
     * Write side, used only by KnowledgeArticleEmbeddingIndexer: re-embeds
     * a batch of articles and writes them with ON CONFLICT (article_id) DO
     * UPDATE, so re-running it is always safe.
     */
    public void upsertAll(List<KnowledgeArticle> articles) {
        if (!connectionPool.isConfigured() || articles.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO knowledge_embedding (article_id, title, content, topic, embedding) "
                + "VALUES (?, ?, ?, ?, ?::vector) "
                + "ON CONFLICT (article_id) DO UPDATE SET "
                + "title = EXCLUDED.title, content = EXCLUDED.content, "
                + "topic = EXCLUDED.topic, embedding = EXCLUDED.embedding";

        try (Connection connection = connectionPool.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (KnowledgeArticle article : articles) {
                String vectorLiteral = toVectorLiteral(
                        embeddingProvider.embed(article.getTitle() + " " + article.getContent()));
                statement.setLong(1, article.getArticleId());
                statement.setString(2, article.getTitle());
                statement.setString(3, article.getContent());
                statement.setString(4, article.getTopic().name());
                statement.setString(5, vectorLiteral);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            log.warn("pgvector upsert failed: {}", ex.toString());
        }
    }

    /** pgvector's text input format for a vector literal: "[v1,v2,...,vn]". */
    private String toVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder(vector.length * 8);
        sb.append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vector[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
