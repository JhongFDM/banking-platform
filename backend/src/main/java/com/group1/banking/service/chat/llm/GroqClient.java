package com.group1.banking.service.chat.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal client for Groq's OpenAI-compatible chat completions endpoint
 * (https://api.groq.com/openai/v1/chat/completions). Deliberately uses the
 * JDK's built-in java.net.http.HttpClient + the Jackson ObjectMapper Spring
 * Boot already provides, so no new dependency is needed in pom.xml.
 *
 * This class only knows how to make HTTP calls and parse responses -- it
 * does not know anything about banking data, guardrails, or prompts. The
 * single-turn complete() is used by GroqResponseGenerator (chatbot.generator
 * =groq); the tool-calling completeWithTools() is used by
 * AgentResponseGenerator (chatbot.generator=agent), whose multi-round loop
 * lives entirely in that class, not here.
 */
@Component
public class GroqClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.api.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    // llama-3.3-70b-versatile was deprecated by Groq (shutdown 08/16/26);
    // openai/gpt-oss-120b is their recommended replacement as of this writing.
    // Check https://console.groq.com/docs/deprecations before assuming this
    // default is still current.
    @Value("${groq.api.model:openai/gpt-oss-120b}")
    private String model;

    @Value("${groq.api.timeout-ms:8000}")
    private long timeoutMs;

    @Value("${groq.api.temperature:0.2}")
    private double temperature;

    @Value("${groq.api.max-tokens:300}")
    private int maxTokens;

    public GroqClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Low temperature and a capped token budget on purpose: this is a
     * constrained, in-scope assistant, not an open-ended chat -- short,
     * consistent replies are what the guardrail and QA scenarios expect.
     */
    public String complete(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ChatGenerationException("GROQ_API_KEY is not configured");
        }

        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(message("system", systemPrompt));
        messages.add(message("user", userPrompt));

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.set("messages", messages);
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                throw new ChatGenerationException(
                        "Groq API returned status " + response.statusCode() + ": " + safeTruncate(response.body()));
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new ChatGenerationException("Groq API response had no message content");
            }
            return content.asText().trim();
        } catch (ChatGenerationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ChatGenerationException("Failed to call Groq API", ex);
        }
    }

    /**
     * A single tool call the model requested. id must be echoed back
     * verbatim in the follow-up "tool" role message so Groq can match the
     * result to the request -- see completeWithTools' javadoc.
     */
    public record ToolCallRequest(String id, String name, String argumentsJson) {
    }

    /**
     * Either textReply is set (the model produced a final answer and
     * toolCalls is empty) or toolCalls is non-empty (the model wants one or
     * more tools executed before it can continue). rawAssistantMessage is
     * the exact message node Groq returned -- callers append it to their
     * running conversation array unmodified before appending tool results,
     * per Groq's own tool-calling protocol (see
     * https://console.groq.com/docs/tool-use/local-tool-calling).
     */
    public record GroqToolResponse(String textReply, List<ToolCallRequest> toolCalls, JsonNode rawAssistantMessage) {
        public boolean hasToolCalls() {
            return toolCalls != null && !toolCalls.isEmpty();
        }
    }

    /**
     * Tool-calling variant of complete(). The caller owns the full
     * conversation array (system/user/assistant/tool messages) and appends
     * to it across rounds -- this method only knows how to make one call
     * and interpret one response, same as complete(); the multi-round loop
     * lives in AgentResponseGenerator, not here.
     *
     * tools is the JSON-Schema-shaped tool list in Groq's
     * {"type":"function","function":{...}} wrapper format. Pass an empty
     * array to force a plain-text-only response (used to get a final
     * answer once a tool-call round budget is exhausted).
     */
    public GroqToolResponse completeWithTools(ArrayNode messages, ArrayNode tools) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ChatGenerationException("GROQ_API_KEY is not configured");
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.set("messages", messages);
        if (tools != null && tools.size() > 0) {
            body.set("tools", tools);
        }
        body.put("temperature", temperature);
        body.put("max_tokens", maxTokens);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                throw new ChatGenerationException(
                        "Groq API returned status " + response.statusCode() + ": " + safeTruncate(response.body()));
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode messageNode = root.path("choices").path(0).path("message");
            JsonNode toolCallsNode = messageNode.path("tool_calls");

            if (toolCallsNode.isArray() && toolCallsNode.size() > 0) {
                List<ToolCallRequest> calls = new ArrayList<>();
                for (JsonNode call : toolCallsNode) {
                    calls.add(new ToolCallRequest(
                            call.path("id").asText(),
                            call.path("function").path("name").asText(),
                            call.path("function").path("arguments").asText("{}")));
                }
                return new GroqToolResponse(null, calls, messageNode);
            }

            JsonNode content = messageNode.path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new ChatGenerationException("Groq API response had no message content and no tool calls");
            }
            return new GroqToolResponse(content.asText().trim(), List.of(), messageNode);
        } catch (ChatGenerationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ChatGenerationException("Failed to call Groq API", ex);
        }
    }

    private ObjectNode message(String role, String content) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("role", role);
        node.put("content", content);
        return node;
    }

    private String safeTruncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= 300 ? text : text.substring(0, 300);
    }
}
