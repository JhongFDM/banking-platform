package com.group1.banking.service.chat.agent;

import java.util.List;

/**
 * What a tool hands back to AgentResponseGenerator. content is the raw
 * string fed back to Groq as the "tool" message content (usually a small
 * JSON blob). basisLines are plain-language citation strings -- computed
 * here, by the tool that actually ran the query, never parsed out of the
 * model's own text. That's the same trust boundary TemplateResponseGenerator
 * and GroqResponseGenerator already use for their "basis" field; agent mode
 * keeps it, just sourced from tool executions instead of a pre-built
 * SafeChatContext.
 */
public record ToolResult(String content, List<String> basisLines) {

    public static ToolResult of(String content) {
        return new ToolResult(content, List.of());
    }
}
