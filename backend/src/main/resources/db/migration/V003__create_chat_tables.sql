-- Savings Insight Chatbot: chat traceability + curated knowledge base.
--
-- NOTE (see code review findings): this repo's db/migration/*.sql files are
-- not currently wired to Flyway/Liquibase -- there's no such dependency in
-- backend/pom.xml, and the live schema is actually produced by Hibernate's
-- spring.jpa.hibernate.ddl-auto=update against the JPA entities in this
-- feature (ChatMessage, KnowledgeArticle). This file is kept for the same
-- documentation-of-intent purpose as V001/V002, and as the actual migration
-- script IF the team wires up Flyway later. Until then, ddl-auto=update will
-- create chat_message/knowledge_article automatically from the entities, and
-- KnowledgeArticleSeeder.java is what actually inserts the seed rows below.

CREATE TABLE chat_message (
    chat_message_id BIGINT          PRIMARY KEY AUTO_INCREMENT,
    customer_id     BIGINT          NOT NULL,
    account_id      BIGINT          NULL,
    query_text      VARCHAR(1000)   NOT NULL,
    response_text   VARCHAR(2000)   NOT NULL,
    topic           VARCHAR(32)     NULL,
    blocked         BOOLEAN         NOT NULL DEFAULT FALSE,
    limited_data    BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chat_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE INDEX idx_chat_customer_id ON chat_message (customer_id);
CREATE INDEX idx_chat_created_at ON chat_message (created_at);

CREATE TABLE knowledge_article (
    article_id  BIGINT          PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(255)    NOT NULL,
    content     VARCHAR(2000)   NOT NULL,
    tags        VARCHAR(500)    NOT NULL,
    topic       VARCHAR(32)     NOT NULL,
    active      BOOLEAN         NOT NULL DEFAULT TRUE
);

-- Seed: curated, approved savings/spending/wellness tips (no open-internet
-- retrieval per the feature's out-of-scope list). Keep this list small and
-- reviewed -- it's the entire retrieval corpus for the knowledge-base source.

INSERT INTO knowledge_article (title, content, tags, topic, active) VALUES
('Start with a small automatic transfer',
 'Setting up a small automatic transfer to savings right after payday -- even $20 -- tends to stick better than trying to save whatever is left over at the end of the month.',
 'save,saving,savings,goal,automatic,transfer', 'SAVINGS', TRUE),

('Emergency fund basics',
 'A common starting target for an emergency fund is one month of essential expenses, then building toward three to six months over time.',
 'emergency fund,save,saving,savings,goal', 'SAVINGS', TRUE),

('Round-up savings',
 'Rounding up everyday purchases to the nearest dollar and moving the difference into savings is a low-effort way to build a habit without feeling a big impact on spending.',
 'save,saving,savings,budget,round up', 'SAVINGS', TRUE),

('Reviewing recurring subscriptions',
 'Recurring subscriptions are easy to lose track of. Reviewing them every few months and cancelling the ones you no longer use is one of the fastest ways to free up money for savings.',
 'spend,spending,expense,subscription,category', 'SPENDING_TRENDS', TRUE),

('Understanding category spikes',
 'A single large purchase in a category (like a one-time repair under Home) can make that category look unusually high for the month -- it is worth checking whether a spending increase is a one-off or a pattern before adjusting a budget.',
 'spend,spending,expense,category,transaction', 'SPENDING_TRENDS', TRUE),

('The 50/30/20 guideline',
 'A widely used, simple guideline is to aim for roughly 50% of income on needs, 30% on wants, and 20% on savings and debt paydown -- treat it as a starting point to adjust, not a strict rule.',
 'budget,save,saving,savings,spend,spending', 'GENERAL_WELLNESS', TRUE),

('Building a simple monthly check-in habit',
 'Spending ten minutes once a month to glance at your spending categories and account balances is often enough to catch surprises early, without needing to track every transaction in detail.',
 'budget,spend,spending,save,saving,wellness', 'GENERAL_WELLNESS', TRUE);
