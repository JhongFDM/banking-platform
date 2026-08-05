package com.group1.banking.service.chat.agent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.group1.banking.security.CustomUserPrincipal;
import com.group1.banking.service.chat.agent.ChatTool;
import com.group1.banking.service.chat.agent.ToolResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Pure arithmetic, done in Java rather than left to the model. Financial
 * math is exactly the kind of thing that shouldn't be produced by token
 * prediction even approximately -- this tool exists so the model has a
 * deterministic way to get a real number instead of guessing one.
 */
@Component
public class ComputeSavingsPlanTool implements ChatTool {

    private final ObjectMapper objectMapper;

    public ComputeSavingsPlanTool(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "compute_savings_plan";
    }

    @Override
    public String description() {
        return "Given a target amount, an amount already saved, and a target date (YYYY-MM-DD), computes the "
                + "monthly savings amount needed to reach the goal on time. Always use this tool for "
                + "savings-plan arithmetic instead of calculating it yourself.";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "targetAmount", Map.of("type", "number", "description", "The savings goal target amount."),
                        "currentAmount", Map.of("type", "number", "description", "The amount already saved."),
                        "targetDate", Map.of("type", "string", "description", "Target date, format YYYY-MM-DD.")
                ),
                "required", List.of("targetAmount", "currentAmount", "targetDate")
        );
    }

    @Override
    public ToolResult execute(JsonNode arguments, CustomUserPrincipal caller, Long accountId) {
        ObjectNode result = objectMapper.createObjectNode();
        try {
            BigDecimal target = arguments.path("targetAmount").decimalValue();
            BigDecimal current = arguments.path("currentAmount").decimalValue();
            LocalDate targetDate = LocalDate.parse(arguments.path("targetDate").asText());
            LocalDate today = LocalDate.now();

            long monthsRemaining = Math.max(1,
                    ChronoUnit.MONTHS.between(today.withDayOfMonth(1), targetDate.withDayOfMonth(1)));
            BigDecimal remaining = target.subtract(current).max(BigDecimal.ZERO);
            BigDecimal monthly = remaining.divide(BigDecimal.valueOf(monthsRemaining), 2, RoundingMode.HALF_UP);

            result.put("monthlyAmountNeeded", monthly);
            result.put("monthsRemaining", monthsRemaining);
            return new ToolResult(result.toString(),
                    List.of("Based on a savings-plan calculation toward your target date"));
        } catch (Exception ex) {
            result.put("error", "Could not compute -- targetAmount, currentAmount, and targetDate must be valid.");
            return ToolResult.of(result.toString());
        }
    }
}
