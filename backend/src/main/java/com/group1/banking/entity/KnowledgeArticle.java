package com.group1.banking.entity;

import com.group1.banking.enums.ChatTopic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Curated, approved content for the chatbot's knowledge base -- short
 * savings/spending/wellness tips. Never customer-editable and never pulled
 * from the open internet; the only way content gets in is a developer
 * adding it to KnowledgeArticleSeeder. Source of truth for the retrieval
 * layer: KnowledgeArticleEmbeddingIndexer reads active rows here and pushes
 * them into pgvector.
 */
@Entity
@Table(name = "knowledge_article")
public class KnowledgeArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id")
    private Long articleId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    /** Comma-separated, left over from an earlier keyword-matching approach -- harmless now. */
    @Column(name = "tags", nullable = false, length = 500)
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(name = "topic", nullable = false)
    private ChatTopic topic;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public ChatTopic getTopic() { return topic; }
    public void setTopic(ChatTopic topic) { this.topic = topic; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
