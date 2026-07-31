package com.group1.banking.service.chat;

import com.group1.banking.enums.ChatTopic;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Two checks, not one. classify() runs first, before any context is built
 * or any generation happens: it scans the raw question against a
 * blocked-keyword list (loan approval, investment advice, legal advice,
 * medical advice, tax advice, and similar) and against savings/spending
 * keyword lists, returning UNSUPPORTED if it's out of scope.
 * isSafeToReturn() runs last, after generation, re-scanning the actual
 * reply text against the same blocked list. That second check matters most
 * when Groq is the generator -- it's what keeps the guardrail meaningful
 * even though the code has no way to control what an LLM decides to say.
 *
 * Both checks live in this one file on purpose, so they can't drift out of
 * sync with each other.
 */
@Service
public class ChatGuardrailService {

    private static final List<String> BLOCKED_KEYWORDS = List.of(
            "loan approval", "approve my loan", "mortgage rate",
            "should i invest", "which stock", "stocks to buy", "crypto", "cryptocurrency", "buy bitcoin",
            "investment advice", "financial advisor",
            "legal advice", "lawsuit",
            "diagnose", "diagnosis", "medication", "medical advice",
            "tax advice", "file my taxes",
            "credit score dispute", "bankruptcy",
            "system prompt", "ignore previous instructions", "as an ai language model", "invest in"
            );

    private static final List<String> SAVINGS_KEYWORDS = List.of(
            "save", "saving", "savings", "goal", "emergency fund", "round up", "round-up", "automatic transfer"
    );

    private static final List<String> SPENDING_KEYWORDS = List.of(
            "spend", "spending", "expense", "expenses", "category", "categories",
            "transaction", "transactions", "subscription", "subscriptions", "purchase", "purchases"
    );

    private static final List<String> WELLNESS_KEYWORDS = List.of(
            "budget", "budgeting", "wellness", "financial health", "financial tip", "tips", "50/30/20"
    );

    /**
     * Cheap pre-check, before any context is built or Groq is called at
     * all. Blocked-topic questions short-circuit here regardless of
     * whether they also mention a supported keyword.
     */
    public ChatTopic classify(String rawQuery) {
        String normalized = normalize(rawQuery);

        if (containsAny(normalized, BLOCKED_KEYWORDS)) {
            return ChatTopic.UNSUPPORTED;
        }
        if (containsAny(normalized, SAVINGS_KEYWORDS)) {
            return ChatTopic.SAVINGS;
        }
        if (containsAny(normalized, SPENDING_KEYWORDS)) {
            return ChatTopic.SPENDING_TRENDS;
        }
        if (containsAny(normalized, WELLNESS_KEYWORDS)) {
            return ChatTopic.GENERAL_WELLNESS;
        }
        return ChatTopic.UNSUPPORTED;
    }

    /**
     * Post-check on the actual generated reply. The only guardrail that
     * still matters once an LLM is in the loop, since there's no way to
     * fully control what it decides to write.
     */
    public boolean isSafeToReturn(String generatedReply) {
        return !containsAny(normalize(generatedReply), BLOCKED_KEYWORDS);
    }

    private boolean containsAny(String normalizedText, List<String> keywords) {
        for (String keyword : keywords) {
            if (normalizedText.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT).trim();
    }
}
