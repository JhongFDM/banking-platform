package com.group1.banking.service.chat;

import java.math.BigDecimal;
import java.util.List;

/**
 * Exactly what a response generator is allowed to see -- the security
 * boundary for this whole feature. Nothing gets into a Groq prompt or a
 * template reply unless it's a field on this record, so internal-only data
 * (admin notes, freeze reasons, risk-model factors, full account numbers)
 * structurally cannot leak through the chatbot, because it's never even
 * loaded into this object in the first place.
 *
 * Built by ChatContextBuilderService, which reuses the app's existing
 * ownership-checked services rather than querying entities directly.
 */
public record SafeChatContext(
        boolean accountRestricted,
        boolean limitedTransactionData,
        boolean limitedGoalData,
        String topCategory,
        BigDecimal topCategoryAmount,
        BigDecimal totalSpend30d,
        boolean hasGoal,
        String goalName,
        BigDecimal goalProgressPercentage,
        BigDecimal goalTargetAmount,
        List<KnowledgeSnippet> matchedArticles
) {
    public record KnowledgeSnippet(String title, String content) {
    }
}
