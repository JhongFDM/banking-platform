package com.group1.banking.service.chat.vectorstore;

import com.group1.banking.entity.KnowledgeArticle;
import com.group1.banking.repository.KnowledgeArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Reads the active KnowledgeArticle rows out of the app's normal H2
 * database and pushes them into pgvector via PgVectorKnowledgeStore.upsertAll().
 * Runs automatically on every startup (pgvector.indexing.on-startup=true by
 * default) -- that's safe only because embedding is done locally via
 * HashingEmbeddingProvider (no external API cost) and the upsert is
 * idempotent, so re-running it with unchanged content is a harmless no-op.
 * If pgvector isn't configured or unreachable, it logs and returns rather
 * than failing.
 *
 * run() never lets an exception escape: Spring Boot treats an uncaught
 * exception from a CommandLineRunner as fatal to the *entire application's*
 * startup, not just this feature, so any failure here is caught and logged
 * as a warning instead.
 *
 * @Order(2): must run after KnowledgeArticleSeeder, which is what actually
 * populates the rows this class reads.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "pgvector.indexing.on-startup", havingValue = "true", matchIfMissing = true)
public class KnowledgeArticleEmbeddingIndexer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeArticleEmbeddingIndexer.class);

    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final PgVectorKnowledgeStore pgVectorKnowledgeStore;
    private final PgVectorConnectionPool connectionPool;

    public KnowledgeArticleEmbeddingIndexer(KnowledgeArticleRepository knowledgeArticleRepository,
                                             PgVectorKnowledgeStore pgVectorKnowledgeStore,
                                             PgVectorConnectionPool connectionPool) {
        this.knowledgeArticleRepository = knowledgeArticleRepository;
        this.pgVectorKnowledgeStore = pgVectorKnowledgeStore;
        this.connectionPool = connectionPool;
    }

    @Override
    public void run(String... args) {
        try {
            if (!connectionPool.isConfigured()) {
                log.info("pgvector not configured (PGVECTOR_URL blank) -- skipping knowledge-article indexing");
                return;
            }
            List<KnowledgeArticle> articles = knowledgeArticleRepository.findByActiveTrue();
            if (articles.isEmpty()) {
                log.info("No active knowledge articles to index yet");
                return;
            }
            pgVectorKnowledgeStore.upsertAll(articles);
            log.info("Indexed {} knowledge articles into pgvector", articles.size());
        } catch (Exception ex) {
            // Never let this escape -- an uncaught CommandLineRunner exception
            // would take down the entire application's startup, not just this feature.
            log.warn("Knowledge-article embedding indexing failed, chatbot will run without cited tips: {}",
                    ex.toString());
        }
    }
}
