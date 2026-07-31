-- Real, manually-run setup script for the separate pgvector database used
-- only by the Savings Insight Chatbot's knowledge retrieval. Run this once
-- against the container started by docker-compose.pgvector.yml (see
-- CHATBOT_SETUP.md for the exact command).
--
-- knowledge_embedding is a self-contained, denormalized copy of the
-- curated knowledge_article rows (title/content duplicated in) so a query
-- never needs a cross-database join back to the app's own H2 database.
-- KnowledgeArticleEmbeddingIndexer keeps it in sync on every backend startup.

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS knowledge_embedding (
    article_id  BIGINT          PRIMARY KEY,
    title       VARCHAR(255)    NOT NULL,
    content     VARCHAR(2000)   NOT NULL,
    topic       VARCHAR(32)     NOT NULL,
    -- 256 to match HashingEmbeddingProvider's output size.
    embedding   vector(256)     NOT NULL
);

-- HNSW index for fast approximate cosine-similarity search (the <=> operator).
CREATE INDEX IF NOT EXISTS idx_knowledge_embedding_hnsw
    ON knowledge_embedding USING hnsw (embedding vector_cosine_ops);

-- Plain index for the topic filter clause in PgVectorKnowledgeStore.query().
CREATE INDEX IF NOT EXISTS idx_knowledge_embedding_topic
    ON knowledge_embedding (topic);
