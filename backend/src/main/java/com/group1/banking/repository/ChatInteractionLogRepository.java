package com.group1.banking.repository;

import java.sql.Timestamp;
import java.time.Instant;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Traceability log for the Savings Insight Chatbot: records each customer query and
 * the generated response (plus whether it was blocked by a guardrail) for QA/demo
 * review. Lives on the chatbot's own Postgres datasource, alongside the pgvector
 * knowledge base - not on the app's primary MySQL/H2 datasource, and not a JPA entity.
 *
 * Deliberately does not persist raw transaction/account data - only the query text,
 * response text, and minimal metadata needed for support/demo traceability, per the
 * "demo-safe" logging requirement.
 */
@Repository
public class ChatInteractionLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public ChatInteractionLogRepository(@Qualifier("chatbotVectorJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureSchema() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS chat_interaction_log (
                    id BIGSERIAL PRIMARY KEY,
                    customer_id BIGINT NOT NULL,
                    query TEXT NOT NULL,
                    response TEXT NOT NULL,
                    outcome VARCHAR(32) NOT NULL,
                    created_at TIMESTAMPTZ NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_chat_interaction_log_customer_id
                    ON chat_interaction_log (customer_id)
                """);
    }

    /**
     * @param outcome one of ANSWERED, GUARDRAIL_BLOCKED, FALLBACK, ERROR - used by QA
     *                to validate that unsafe topics were actually blocked.
     */
    public void log(Long customerId, String query, String response, String outcome) {
        jdbcTemplate.update(
                "INSERT INTO chat_interaction_log (customer_id, query, response, outcome, created_at) " +
                        "VALUES (?, ?, ?, ?, ?)",
                customerId, query, response, outcome, Timestamp.from(Instant.now()));
    }
}
