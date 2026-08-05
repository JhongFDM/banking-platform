package com.group1.banking.service.chat.agent.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.group1.banking.enums.ChatTopic;
import com.group1.banking.security.CustomUserPrincipal;
import com.group1.banking.service.chat.agent.ChatTool;
import com.group1.banking.service.chat.agent.ToolResult;
import com.group1.banking.service.chat.SafeChatContext;
import com.group1.banking.service.chat.vectorstore.PgVectorKnowledgeStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class SearchKnowledgeBaseTool implements ChatTool {

    private final PgVectorKnowledgeStore knowledgeStore;
    private final ObjectMapper objectMapper;

    @Value("${pgvector.top-k:2}")
    private int topK;

    public SearchKnowledgeBaseTool(PgVectorKnowledgeStore knowledgeStore, ObjectMapper objectMapper) {
        this.knowledgeStore = knowledgeStore;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "search_knowledge_base";
    }

    @Override
    public String description() {
        return "Searches the curated savings/spending/wellness tips for a query and topic. Call this to find "
                + "an approved tip to cite in your answer -- never invent a tip yourself.";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        return Map.of(
                "type", "object",
                "properties", Map.of(
                        "query", Map.of(
                                "type", "string",
                                "description", "What to search for, e.g. the customer's own question."),
                        "topic", Map.of(
                                "type", "string",
                                "description", "One of SAVINGS, SPENDING_TRENDS, GENERAL_WELLNESS.")
                ),
                "required", List.of("query", "topic")
        );
    }

    @Override
    public ToolResult execute(JsonNode arguments, CustomUserPrincipal caller, Long accountId) {
        String query = arguments.hasNonNull("query") ? arguments.get("query").asText() : "";

        ChatTopic topic;
        try {
            topic = ChatTopic.valueOf(arguments.path("topic").asText("").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            topic = ChatTopic.GENERAL_WELLNESS;
        }

        List<SafeChatContext.KnowledgeSnippet> snippets = knowledgeStore.query(query, topic, topK);

        ArrayNode array = objectMapper.createArrayNode();
        List<String> basis = new ArrayList<>();
        for (SafeChatContext.KnowledgeSnippet snippet : snippets) {
            ObjectNode node = array.addObject();
            node.put("title", snippet.title());
            node.put("content", snippet.content());
            basis.add("From the savings tip \"" + snippet.title() + "\"");
        }
        return new ToolResult(array.toString(), basis);
    }
}
