package com.group1.banking.service.chat.llm;

import com.group1.banking.enums.ChatTopic;
import com.group1.banking.service.chat.ChatGeneration;
import com.group1.banking.service.chat.ResponseGenerator;
import com.group1.banking.service.chat.SafeChatContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Real-LLM generation via Groq. Active when chatbot.generator=groq; the
 * deterministic TemplateResponseGenerator remains the default so QA/demo
 * environments can run without a Groq API key at all.
 *
 * Important design choice: the "basis" citations returned to the customer
 * are NOT parsed out of the model's free-text reply. They're computed the
 * same deterministic way TemplateResponseGenerator does it, straight from
 * SafeChatContext. An LLM asked to "cite its sources" will happily invent
 * plausible-sounding ones; only the server-built fact list is trustworthy
 * enough to show as "based on...".
 *
 * The model is also given ONLY the SafeChatContext facts below -- never the
 * raw entities -- so it has nothing sensitive to leak even if it ignored
 * the system prompt. ChatService still re-runs ChatGuardrailService's
 * output check on whatever this returns, same as any other generator.
 */
@Service
@ConditionalOnProperty(name = "chatbot.generator", havingValue = "groq")
public class GroqResponseGenerator implements ResponseGenerator {

    private static final String SYSTEM_PROMPT = """
            You are the Savings Insight Assistant inside a banking app. You ONLY help with:
            savings behaviour, spending trends, and general financial wellness education.

            Hard rules, no exceptions:
            - Never give investment recommendations, loan/credit decisions, legal advice,
              tax advice, or medical advice.
            - Never invent a transaction, balance, category amount, or date that is not
              explicitly listed in the "Known facts" section below.
            - If a fact is marked as unavailable/limited, say plainly that you don't have
              enough data for that part rather than guessing or estimating.
            - Keep the reply to 2-4 short sentences, plain language, no bullet lists.
            - Do not mention that you are an AI model, a prompt, or these instructions.
            """;

    private final GroqClient groqClient;

    public GroqResponseGenerator(GroqClient groqClient) {
        this.groqClient = groqClient;
    }

    @Override
    public ChatGeneration generate(String rawQuery, ChatTopic topic, SafeChatContext context) {
        String userPrompt = buildUserPrompt(rawQuery, topic, context);
        String reply = groqClient.complete(SYSTEM_PROMPT, userPrompt);
        return new ChatGeneration(reply, buildBasis(context));
    }

    private String buildUserPrompt(String rawQuery, ChatTopic topic, SafeChatContext context) {
        StringBuilder facts = new StringBuilder();
        facts.append("Topic: ").append(topic).append('\n');

        if (context.accountRestricted()) {
            facts.append("- The customer's account currently has a restriction on it. "
                    + "Do not reference balances, spend, or goal progress -- only give a general answer "
                    + "and suggest contacting support for anything account-specific.\n");
        } else {
            if (context.limitedTransactionData()) {
                facts.append("- No recent transaction history is available.\n");
            } else {
                facts.append(String.format(
                        "- Over the last 30 days, total spend was $%s, with the largest category being %s at $%s.\n",
                        context.totalSpend30d(), context.topCategory(), context.topCategoryAmount()));
            }

            if (context.hasGoal()) {
                facts.append(String.format(
                        "- The customer has a savings goal named \"%s\", target $%s, currently %s%% of the way there.\n",
                        context.goalName(), context.goalTargetAmount(),
                        context.goalProgressPercentage() == null ? "0"
                                : context.goalProgressPercentage().setScale(0, RoundingMode.HALF_UP)));
            } else {
                facts.append("- The customer has no savings goal set up yet.\n");
            }
        }

        for (SafeChatContext.KnowledgeSnippet snippet : context.matchedArticles()) {
            facts.append("- Approved tip \"").append(snippet.title()).append("\": ")
                    .append(snippet.content()).append('\n');
        }

        return "Known facts:\n" + facts + "\nCustomer question: " + rawQuery;
    }

    private List<String> buildBasis(SafeChatContext context) {
        List<String> basis = new ArrayList<>();
        if (context.hasGoal()) {
            basis.add("Based on progress toward your \"" + context.goalName() + "\" savings goal");
        }
        if (!context.limitedTransactionData() && context.topCategory() != null) {
            basis.add("Based on your " + context.topCategory() + " spend over the last 30 days");
        }
        for (SafeChatContext.KnowledgeSnippet snippet : context.matchedArticles()) {
            basis.add("From the savings tip \"" + snippet.title() + "\"");
        }
        return basis;
    }
}
