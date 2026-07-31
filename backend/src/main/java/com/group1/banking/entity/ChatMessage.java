package com.group1.banking.entity;

import com.group1.banking.enums.ChatTopic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Chat Message Entity
 *
 * Traceability record for a single chatbot turn (FR-008). Intentionally
 * lean: stores the query/response text needed for support and QA review,
 * not raw transaction/account data. Should only be persisted in
 * non-production or demo-safe environments per the feature's privacy
 * constraints -- gate writes behind a config flag (see ChatService) rather
 * than assuming this table is safe to run in a real production deployment
 * without a privacy/retention review.
 */
@Entity
@Table(name = "chat_message",
        indexes = {
                @Index(name = "idx_chat_customer_id", columnList = "customer_id"),
                @Index(name = "idx_chat_created_at", columnList = "created_at")
        })
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Long chatMessageId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** Nullable: a query answered without account-specific context. */
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "query_text", nullable = false, length = 1000)
    private String queryText;

    @Column(name = "response_text", nullable = false, length = 2000)
    private String responseText;

    @Enumerated(EnumType.STRING)
    @Column(name = "topic")
    private ChatTopic topic;

    @Column(name = "blocked", nullable = false)
    private boolean blocked;

    @Column(name = "limited_data", nullable = false)
    private boolean limitedData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getChatMessageId() { return chatMessageId; }
    public void setChatMessageId(Long chatMessageId) { this.chatMessageId = chatMessageId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getQueryText() { return queryText; }
    public void setQueryText(String queryText) { this.queryText = queryText; }

    public String getResponseText() { return responseText; }
    public void setResponseText(String responseText) { this.responseText = responseText; }

    public ChatTopic getTopic() { return topic; }
    public void setTopic(ChatTopic topic) { this.topic = topic; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public boolean isLimitedData() { return limitedData; }
    public void setLimitedData(boolean limitedData) { this.limitedData = limitedData; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
