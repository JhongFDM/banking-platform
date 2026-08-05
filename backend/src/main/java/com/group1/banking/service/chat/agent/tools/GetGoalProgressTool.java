package com.group1.banking.service.chat.agent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.group1.banking.dto.SavingsGoalResponse;
import com.group1.banking.entity.Account;
import com.group1.banking.security.CustomUserPrincipal;
import com.group1.banking.service.SavingsGoalService;
import com.group1.banking.service.chat.agent.AccountAccessGuard;
import com.group1.banking.service.chat.agent.ChatTool;
import com.group1.banking.service.chat.agent.ToolResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GetGoalProgressTool implements ChatTool {

    private final SavingsGoalService savingsGoalService;
    private final AccountAccessGuard accessGuard;
    private final ObjectMapper objectMapper;

    public GetGoalProgressTool(SavingsGoalService savingsGoalService, AccountAccessGuard accessGuard,
                                ObjectMapper objectMapper) {
        this.savingsGoalService = savingsGoalService;
        this.accessGuard = accessGuard;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "get_goal_progress";
    }

    @Override
    public String description() {
        return "Returns the customer's savings goal: name, target amount, current balance, progress "
                + "percentage, and target date. Call this whenever the question involves a savings goal or "
                + "how close the customer is to a target.";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "accountId", Map.of(
                                // Some models emit an explicit `null` for an omitted optional
                                // argument rather than leaving the key out entirely -- Groq's
                                // tool-call validator rejects that against a plain "integer"
                                // type, so "null" has to be an accepted type too.
                                "type", List.of("integer", "null"),
                                "description", "The account to check. Omit or pass null to use the account already in context.")
                ),
                "required", List.of()
        );
    }

    @Override
    public ToolResult execute(JsonNode arguments, CustomUserPrincipal caller, Long accountId) {
        Long explicit = arguments.hasNonNull("accountId") ? arguments.get("accountId").asLong() : null;
        Account account = accessGuard.resolve(explicit, accountId, caller);

        List<SavingsGoalResponse> goals = savingsGoalService.getAllGoalsForCustomer(caller.getCustomerId());
        SavingsGoalResponse goal = account != null
                ? goals.stream().filter(g -> account.getAccountId().equals(g.getAccountId())).findFirst().orElse(null)
                : (goals.isEmpty() ? null : goals.get(0));

        ObjectNode result = objectMapper.createObjectNode();
        if (goal == null) {
            result.put("hasGoal", false);
            return ToolResult.of(result.toString());
        }

        result.put("hasGoal", true);
        result.put("goalName", goal.getGoalName());
        result.put("targetAmount", goal.getTargetAmount());
        result.put("currentBalance", goal.getCurrentBalance());
        result.put("progressPercentage", goal.getProgressPercentage());
        result.put("targetDate", String.valueOf(goal.getTargetDate()));

        return new ToolResult(result.toString(),
                List.of("Based on progress toward your \"" + goal.getGoalName() + "\" savings goal"));
    }
}
