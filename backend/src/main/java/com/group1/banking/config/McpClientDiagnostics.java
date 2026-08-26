package com.group1.banking.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Confirms the MCP (Model Context Protocol) client is wired up at startup.
 *
 * This adds no product feature yet - no MCP servers are configured by default (see
 * the "MCP client" section in application.properties), so {@code toolCallbackProvider}
 * resolves to zero tool callbacks under normal startup. It exists purely so the
 * plumbing (dependency + autoconfiguration, and a place for an Agent to later pull in
 * MCP-provided tools alongside its existing @Tool methods) is visible and verifiable
 * from the boot log, ahead of any real MCP server being connected.
 *
 * When the opt-in "mcptest" profile is active (see application-mcptest.properties)
 * and mcp-test-server is running, a "ping" tool becomes available. In that case this
 * also calls it and logs the real response, proving a full MCP round trip
 * (initialize -> list tools -> call tool) rather than just tool discovery. That call
 * only ever happens if a tool literally named "ping" is present, so it stays a no-op
 * under the default (server-less) configuration.
 *
 * Uses {@link ObjectProvider} rather than a required constructor dependency so that
 * if no MCP-related bean is ever produced (e.g. the starter is removed, or
 * autoconfiguration changes across a Spring AI version bump), this component
 * degrades to a no-op log line instead of failing application startup.
 */
@Component
public class McpClientDiagnostics {

    private static final Logger log = LoggerFactory.getLogger(McpClientDiagnostics.class);

    private static final String PING_TOOL_NAME = "ping";

    private final ObjectProvider<ToolCallbackProvider> toolCallbackProvider;

    public McpClientDiagnostics(ObjectProvider<ToolCallbackProvider> toolCallbackProvider) {
        this.toolCallbackProvider = toolCallbackProvider;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logMcpStatus() {
        ToolCallbackProvider provider = toolCallbackProvider.getIfAvailable();
        if (provider == null) {
            log.info("MCP client: no ToolCallbackProvider bean found (is spring-ai-starter-mcp-client "
                    + "on the classpath?). No MCP tools are available.");
            return;
        }

        ToolCallback[] callbacks = provider.getToolCallbacks();
        log.info("MCP client wired up: {} tool callback(s) available from configured MCP servers "
                + "(0 is expected until a server is added under spring.ai.mcp.client.* in "
                + "application.properties).", callbacks.length);

        for (ToolCallback callback : callbacks) {
            if (PING_TOOL_NAME.equals(callback.getToolDefinition().name())) {
                callPingForRoundTripCheck(callback);
            }
        }
    }

    private void callPingForRoundTripCheck(ToolCallback pingCallback) {
        try {
            String result = pingCallback.call("{}");
            log.info("MCP client round-trip check: called the '{}' tool on the connected test server "
                    + "and got back: {}", PING_TOOL_NAME, result);
        } catch (Exception ex) {
            log.warn("MCP client round-trip check: found the '{}' tool but calling it failed. "
                    + "Tool discovery worked; invocation did not.", PING_TOOL_NAME, ex);
        }
    }
}
