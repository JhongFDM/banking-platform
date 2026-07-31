package com.group1.banking.service.chat;

import com.group1.banking.entity.KnowledgeArticle;
import com.group1.banking.enums.ChatTopic;
import com.group1.banking.repository.KnowledgeArticleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs once at every startup and inserts the 7 curated knowledge articles if
 * the table is empty. This exists because of a real gap: this project's
 * db/migration/*.sql files aren't actually wired to Flyway (no such
 * dependency in pom.xml), so the INSERT statements in
 * V003__create_chat_tables.sql document the intended seed content but never
 * execute on their own. This seeder is what actually gets that content into
 * the database on a fresh checkout, with no manual SQL step required.
 *
 * Idempotent by construction: skips entirely if any row already exists, so
 * re-running it on every boot is always a safe no-op after the first.
 *
 * @Order(1): must run before KnowledgeArticleEmbeddingIndexer, which reads
 * these same rows back out to push into pgvector.
 */
@Component
@Order(1)
public class KnowledgeArticleSeeder implements CommandLineRunner {

    private final KnowledgeArticleRepository knowledgeArticleRepository;

    public KnowledgeArticleSeeder(KnowledgeArticleRepository knowledgeArticleRepository) {
        this.knowledgeArticleRepository = knowledgeArticleRepository;
    }

    @Override
    public void run(String... args) {
        if (knowledgeArticleRepository.count() > 0) {
            return;
        }
        knowledgeArticleRepository.saveAll(List.of(
                article("Start with a small automatic transfer",
                        "Setting up a small automatic transfer to savings right after payday -- even $20 -- "
                                + "tends to stick better than trying to save whatever is left over at the end "
                                + "of the month.",
                        "save,saving,savings,goal,automatic,transfer", ChatTopic.SAVINGS),

                article("Emergency fund basics",
                        "A common starting target for an emergency fund is one month of essential expenses, "
                                + "then building toward three to six months over time.",
                        "emergency fund,save,saving,savings,goal", ChatTopic.SAVINGS),

                article("Round-up savings",
                        "Rounding up everyday purchases to the nearest dollar and moving the difference into "
                                + "savings is a low-effort way to build a habit without feeling a big impact "
                                + "on spending.",
                        "save,saving,savings,budget,round up", ChatTopic.SAVINGS),

                article("Reviewing recurring subscriptions",
                        "Recurring subscriptions are easy to lose track of. Reviewing them every few months "
                                + "and cancelling the ones you no longer use is one of the fastest ways to "
                                + "free up money for savings.",
                        "spend,spending,expense,subscription,category", ChatTopic.SPENDING_TRENDS),

                article("Understanding category spikes",
                        "A single large purchase in a category (like a one-time repair under Home) can make "
                                + "that category look unusually high for the month -- it is worth checking "
                                + "whether a spending increase is a one-off or a pattern before adjusting a "
                                + "budget.",
                        "spend,spending,expense,category,transaction", ChatTopic.SPENDING_TRENDS),

                article("The 50/30/20 guideline",
                        "A widely used, simple guideline is to aim for roughly 50% of income on needs, 30% "
                                + "on wants, and 20% on savings and debt paydown -- treat it as a starting "
                                + "point to adjust, not a strict rule.",
                        "budget,save,saving,savings,spend,spending", ChatTopic.GENERAL_WELLNESS),

                article("Building a simple monthly check-in habit",
                        "Spending ten minutes once a month to glance at your spending categories and account "
                                + "balances is often enough to catch surprises early, without needing to "
                                + "track every transaction in detail.",
                        "budget,spend,spending,save,saving,wellness", ChatTopic.GENERAL_WELLNESS)
        ));
    }

    private KnowledgeArticle article(String title, String content, String tags, ChatTopic topic) {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setTitle(title);
        article.setContent(content);
        article.setTags(tags);
        article.setTopic(topic);
        article.setActive(true);
        return article;
    }
}
