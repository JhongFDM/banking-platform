package com.group1.banking.service.chat;

import com.group1.banking.enums.ChatTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic, template-based generator. No external LLM call -- every
 * branch below is a fixed string composed from SafeChatContext fields, so
 * the same context always produces the same reply. This satisfies the
 * "bounded, QA-deterministic" requirement for phase 1 while still feeling
 * conversational and citing its basis in plain language.
 */
@Service
@ConditionalOnProperty(name = "chatbot.generator", havingValue = "template", matchIfMissing = true)
public class TemplateResponseGenerator implements ResponseGenerator {

    @Override
    public ChatGeneration generate(String rawQuery, ChatTopic topic, SafeChatContext context) {
        List<String> basis = new ArrayList<>();

        if (context.accountRestricted()) {
            return new ChatGeneration(
                    "I can only give you general tips right now because there's a restriction on this "
                            + "account. For anything account-specific, please contact support.",
                    basis);
        }

        StringBuilder reply = new StringBuilder();

        if (context.hasGoal()) {
            reply.append(String.format(
                    "You're %s%% of the way to your \"%s\" goal (target $%s). ",
                    context.goalProgressPercentage() == null ? "0"
                            : context.goalProgressPercentage().setScale(0, RoundingMode.HALF_UP),
                    context.goalName(),
                    context.goalTargetAmount()));
            basis.add("Based on progress toward your \"" + context.goalName() + "\" savings goal");
        }

        if (!context.limitedTransactionData() && context.topCategory() != null) {
            reply.append(String.format(
                    "Over the last 30 days your biggest spending category was %s at $%s. "
                            + "Trimming that a bit could get you to your goal faster.",
                    context.topCategory(), context.topCategoryAmount()));
            basis.add("Based on your " + context.topCategory() + " spend over the last 30 days");
        } else {
            reply.append(notEnoughSpendData(topic));
        }

        if (!context.matchedArticles().isEmpty()) {
            SafeChatContext.KnowledgeSnippet snippet = context.matchedArticles().get(0);
            reply.append(" Tip: ").append(snippet.content());
            basis.add("From the savings tip \"" + snippet.title() + "\"");
        }

        if (!context.hasGoal() && context.limitedTransactionData()) {
            reply.append(" Once you set a savings goal and have a bit more transaction history, "
                    + "I can give you more personalized guidance.");
        }

        return new ChatGeneration(reply.toString().trim(), basis);
    }

    private String notEnoughSpendData(ChatTopic topic) {
        return switch (topic) {
            case SPENDING_TRENDS -> " I don't have enough recent transaction history yet to break down your "
                    + "spending by category, so here's a general tip instead.";
            case SAVINGS -> " I don't have enough account activity yet to personalize this, so here's a "
                    + "general savings tip.";
            default -> " Here's a general financial wellness tip.";
        };
    }
}
