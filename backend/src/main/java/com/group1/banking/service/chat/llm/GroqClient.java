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

/**
 * Minimal client for Groq's OpenAI-compatible chat completions endpoint
 * (https://api.groq.com/openai/v1/chat/completions). Deliberately uses the
 * JDK's built-in java.net.http.HttpClient + the Jackson ObjectMapper Spring
 * Boot already provides, so no new dependency is needed in pom.xml.
 *
 * This class only knows how to make one HTTP call and parse one response
 * shape. It does not know anything about banking data, guardrails, or
 * prompts -- that composition lives in GroqResponseGenerator, so this class
 * could be reused for any Groq-backed feature later.
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
