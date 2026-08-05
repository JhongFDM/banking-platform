package com.group1.banking.service.chat.agent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.group1.banking.dto.response.SpendingInsightResponse;
import com.group1.banking.entity.Account;
import com.group1.banking.security.CustomUserPrincipal;
import com.group1.banking.service.SpendingInsightService;
import com.group1.banking.service.chat.agent.AccountAccessGuard;
import com.group1.banking.service.chat.agent.ChatTool;
import com.group1.banking.service.chat.agent.ToolResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The tool that gives the agent flexibility TemplateResponseGenerator and
 * GroqResponseGenerator don't have: SpendingInsightService.getInsights()
 * only ever answers for one specific month, so this tool loops over up to
 * MAX_MONTHS_BACK individual month calls and aggregates them, rather than
 * being hardcoded to "this month" the way ChatContextBuilderService is.
 */
@Component
public class GetSpendingSummaryTool implements ChatTool {

    private static final int MAX_MONTHS_BACK = 6;

    private final SpendingInsightService spendingInsightService;
    private final AccountAccessGuard accessGuard;
    private final ObjectMapper objectMapper;

    public GetSpendingSummaryTool(SpendingInsightService spendingInsightService, AccountAccessGuard accessGuard,
                                   ObjectMapper objectMapper) {
        this.spendingInsightService = spendingInsightService;
        this.accessGuard = accessGuard;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "get_spending_summary";
    }

    @Override
    public String description() {
        return "Returns total spend and the top spending category for an account, aggregated over the last "
                + "N months starting from the current month (default 1, max 6). Call this for spending-trend "
                + "questions, especially ones about a period other than the current month.";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        // "null" is accepted alongside the real type on every optional field
                        // here -- some models emit an explicit null for an omitted argument
                        // instead of leaving the key out, and Groq's tool-call validator
                        // rejects that against a plain non-nullable type.
                        "accountId", Map.of(
                                "type", List.of("integer", "null"),
                                "description", "The account to check. Omit or pass null to use the account already in context."),
                        "monthsBack", Map.of(
                                "type", List.of("integer", "null"),
                                "description", "How many recent months to include, starting from the current month. 1-6, default 1."),
                        "category", Map.of(
                                "type", List.of("string", "null"),
                                "description", "Optional: also report the total for one specific category, e.g. \"Food & Drink\".")
                ),
                "required", List.of()
        );
    }

    @Override
    public ToolResult execute(JsonNode arguments, CustomUserPrincipal caller, Long accountId) {
        Long explicit = arguments.hasNonNull("accountId") ? arguments.get("accountId").asLong() : null;
        Account account = accessGuard.resolve(explicit, accountId, caller);

        ObjectNode result = objectMapper.createObjectNode();
        if (account == null) {
            result.put("available", false);
            result.put("reason", "No account is in context for this question.");
            return ToolResult.of(result.toString());
        }

        int monthsBack = arguments.hasNonNull("monthsBack")
                ? Math.max(1, Math.min(MAX_MONTHS_BACK, arguments.get("monthsBack").asInt()))
                : 1;

        YearMonth cursor = YearMonth.now();
        BigDecimal total = BigDecimal.ZERO;
        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        int monthsWithData = 0;

        for (int i = 0; i < monthsBack; i++) {
            YearMonth month = cursor.minusMonths(i);
            try {
                SpendingInsightResponse insights = spendingInsightService.getInsights(
                        account.getAccountId(), month.getYear(), month.getMonthValue(), caller);
                total = total.add(insights.getTotalDebitSpend());
                monthsWithData++;
                if (insights.getCategoryBreakdown() != null) {
                    for (SpendingInsightResponse.CategoryBreakdownItem item : insights.getCategoryBreakdown()) {
                        categoryTotals.merge(item.getCategory(), item.getTotalAmount(), BigDecimal::add);
                    }
                }
            } catch (RuntimeException ex) {
                // No data for that month, or a transient failure -- skip it, same fail-soft
                // approach ChatContextBuilderService uses for the template/groq path.
            }
        }

        String topCategory = null;
        BigDecimal topAmount = null;
        for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
            if (topAmount == null || entry.getValue().compareTo(topAmount) > 0) {
                topCategory = entry.getKey();
                topAmount = entry.getValue();
            }
        }

        String requestedCategory = arguments.hasNonNull("category") ? arguments.get("category").asText() : null;

        result.put("available", monthsWithData > 0);
        result.put("monthsCovered", monthsWithData);
        result.put("totalSpend", total);
        if (topCategory != null) {
            result.put("topCategory", topCategory);
            result.put("topCategoryAmount", topAmount);
        }
        if (requestedCategory != null) {
            result.put("requestedCategory", requestedCategory);
            result.put("requestedCategoryAmount", categoryTotals.get(requestedCategory));
        }

        List<String> basis = new ArrayList<>();
        if (topCategory != null) {
            basis.add("Based on your " + topCategory + " spend over the last " + monthsWithData
                    + (monthsWithData == 1 ? " month" : " months"));
        }
        return new ToolResult(result.toString(), basis);
    }
}
