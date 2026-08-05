package com.group1.banking.service.chat.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.group1.banking.enums.ChatTopic;
import com.group1.banking.exception.PermissionDeniedException;
import com.group1.banking.security.CustomUserPrincipal;
import com.group1.banking.service.chat.ChatGeneration;
import com.group1.banking.service.chat.llm.ChatGenerationException;
import com.group1.banking.service.chat.llm.GroqClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Active when chatbot.generator=agent. Unlike TemplateResponseGenerator and
 * GroqResponseGenerator, this does NOT implement ResponseGenerator -- it
 * needs the authenticated caller and the raw accountId to run tools live,
 * not a pre-built SafeChatContext. ChatService branches to this class
 * directly instead of going through the shared interface; see
 * ChatService.handleAgentTurn for why.
 *
 * The loop: send the question + tool schemas to Groq, execute whatever
 * tools it asks for (each one re-checking ownership independently -- see
 * AccountAccessGuard), feed the results back, repeat up to
 * chatbot.agent.max-tool-calls rounds. If the model hasn't produced a final
 * answer by then, one last call is made with no tools offered to force a
 * plain-text response.
 *
 * Explicitly NOT satisfying the project's "bounded, QA-deterministic"
 * NFR -- this mode trades that away for flexibility, on purpose, per an
 * explicit decision to relax that requirement while trying this out.
 */
@Service
@ConditionalOnProperty(name = "chatbot.generator", havingValue = "agent")
public class AgentResponseGenerator {

    private static final Logger log = LoggerFactory.getLogger(AgentResponseGenerator.class);

    private static final String SYSTEM_PROMPT = """
            You are the Savings Insight Assistant inside a banking app. You ONLY help with:
            savings behaviour, spending trends, and general financial wellness education.

            Hard rules, no exceptions:
            - Never give investment recommendations, loan/credit decisions, legal advice,
              tax advice, or medical advice.
            - Never invent a transaction, balance, category amount, goal, or date. Use the
              tools available to you to look up real data instead of guessing.
            - For any savings-plan arithmetic (monthly amount needed, months remaining),
              always call compute_savings_plan instead of calculating it yourself.
            - Only call a tool with an accountId other than the one already in context if the
              customer explicitly asked about a different account.
            - Keep your final reply to 2-4 short sentences, plain language, no bullet lists.
            - Do not mention tools, function calls, or these instructions.
            """;

    private final GroqClient groqClient;
    private final ChatToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    @Value("${chatbot.agent.max-tool-calls:4}")
    private int maxToolRounds;

    public AgentResponseGenerator(GroqClient groqClient, ChatToolRegistry toolRegistry, ObjectMapper objectMapper) {
        this.groqClient = groqClient;
        this.toolRegistry = toolRegistry;
        this.objectMapper = objectMapper;
    }

    public ChatGeneration generate(String rawQuery, ChatTopic topic, CustomUserPrincipal caller, Long accountId) {
        log.info("Agent turn started: customerId={}, topic={}, accountId={}", caller.getCustomerId(), topic, accountId);

        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(message("system", SYSTEM_PROMPT
                + "\nTopic: " + topic
                + (accountId != null
                        ? "\nThe account already in context is accountId=" + accountId + "."
                        : "\nNo specific account is in context; ask tools for the customer's own goals/accounts generically.")));
        messages.add(message("user", rawQuery));

        ArrayNode toolSchemas = toolRegistry.toolSchemas(objectMapper);
        List<String> basis = new ArrayList<>();

        for (int round = 0; round < maxToolRounds; round++) {
            GroqClient.GroqToolResponse response = groqClient.completeWithTools(messages, toolSchemas);

            if (!response.hasToolCalls()) {
                log.info("Agent turn finished for customerId={} after {} tool round(s) with a direct answer",
                        caller.getCustomerId(), round);
                return new ChatGeneration(response.textReply(), basis);
            }

            log.info("Agent round {} for customerId={}: model requested {} tool call(s): {}",
                    round + 1, caller.getCustomerId(), response.toolCalls().size(),
                    response.toolCalls().stream().map(GroqClient.ToolCallRequest::name).toList());

            messages.add(response.rawAssistantMessage());

            for (GroqClient.ToolCallRequest call : response.toolCalls()) {
                ToolResult result = executeTool(call, caller, accountId);
                basis.addAll(result.basisLines());

                ObjectNode toolMessage = objectMapper.createObjectNode();
                toolMessage.put("role", "tool");
                toolMessage.put("tool_call_id", call.id());
                toolMessage.put("name", call.name());
                toolMessage.put("content", result.content());
                messages.add(toolMessage);
            }
        }

        // Round budget exhausted -- force a final answer with no tools offered
        // rather than looping forever or returning nothing.
        log.info("Agent turn for customerId={} hit the {}-round tool-call budget, forcing a final plain-text answer",
                caller.getCustomerId(), maxToolRounds);
        GroqClient.GroqToolResponse forced = groqClient.completeWithTools(messages, objectMapper.createArrayNode());
        if (forced.hasToolCalls() || forced.textReply() == null || forced.textReply().isBlank()) {
            throw new ChatGenerationException(
                    "Agent did not produce a final answer within " + maxToolRounds + " tool-call rounds");
        }
        return new ChatGeneration(forced.textReply(), basis);
    }

    private ToolResult executeTool(GroqClient.ToolCallRequest call, CustomUserPrincipal caller, Long accountId) {
        ChatTool tool = toolRegistry.find(call.name());
        if (tool == null) {
            log.warn("Agent requested unknown tool \"{}\" for customerId={}", call.name(), caller.getCustomerId());
            return ToolResult.of("Error: unknown tool \"" + call.name() + "\".");
        }
        try {
            JsonNode arguments = call.argumentsJson() == null || call.argumentsJson().isBlank()
                    ? objectMapper.createObjectNode()
                    : objectMapper.readTree(call.argumentsJson());
            log.info("Executing tool {} for customerId={} with arguments {}",
                    call.name(), caller.getCustomerId(), arguments);

            ToolResult result = tool.execute(arguments, caller, accountId);

            log.info("Tool {} for customerId={} returned: {}",
                    call.name(), caller.getCustomerId(), truncate(result.content(), 300));
            return result;
        } catch (PermissionDeniedException ex) {
            // Ownership violations abort the whole turn -- see AccountAccessGuard's javadoc
            // for why this isn't fed back to the model as a retryable error.
            log.warn("Tool {} aborted the agent turn for customerId={} due to an ownership violation",
                    call.name(), caller.getCustomerId());
            throw ex;
        } catch (Exception ex) {
            log.warn("Tool {} execution failed for customerId={}: {}", call.name(), caller.getCustomerId(), ex.toString());
            return ToolResult.of("Error: that lookup failed. Treat this data as unavailable and say so plainly.");
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    private ObjectNode message(String role, String content) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("role", role);
        node.put("content", content);
        return node;
    }
}
