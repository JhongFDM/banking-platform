package com.group1.banking.service.chat.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.group1.banking.security.CustomUserPrincipal;

import java.util.Map;

/**
 * One capability the agentic chatbot generator can choose to invoke.
 * Implementations are picked up automatically by ChatToolRegistry (Spring
 * collects every ChatTool bean) -- adding a new capability means adding a
 * new class here, not editing the orchestration loop.
 *
 * Security note: arguments are LLM-controlled and therefore untrusted in
 * exactly the same way a client-supplied request field would be. Any tool
 * that touches account-scoped data MUST independently verify ownership
 * against caller (see AccountAccessGuard) -- never trust an accountId found
 * in `arguments` on its own.
 */
public interface ChatTool {

    /** Must match the name the model is told about in the tool schema. */
    String name();

    /** Shown to the model verbatim -- this is what it uses to decide when to call the tool. */
    String description();

    /** JSON-Schema "parameters" object, e.g. {"type":"object","properties":{...},"required":[...]}. */
    Map<String, Object> parametersSchema();

    /**
     * Executes the tool. accountId is the account from the original chat
     * request (already ownership-checked once before the agent loop
     * started) -- tools use it as the default when the model's arguments
     * don't specify one.
     */
    ToolResult execute(JsonNode arguments, CustomUserPrincipal caller, Long accountId);
}
