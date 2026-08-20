package com.group1.banking.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Confirms the MCP (Model Context Protocol) client is wired up at startup.
 *
 * This adds no product feature yet - no MCP servers are configured (see the
 * "MCP client" section in application.properties), so {@code toolCallbackProvider}
 * resolves to zero tool callbacks today. It exists purely so the plumbing
 * (dependency + autoconfiguration, and a place for an Agent to later pull in
 * MCP-provided tools alongside its existing @Tool methods) is visible and
 * verifiable from the boot log, ahead of any real MCP server being connected.
 *
 * Uses {@link ObjectProvider} rather than a required constructor dependency so
 * that if no MCP-related bean is ever produced (e.g. the starter is removed, or
 * autoconfiguration changes across a Spring AI version bump), this component
 * degrades to a no-op log line instead of failing application startup.
 */
@Component
public class McpClientDiagnostics {

    private static final Logger log = LoggerFactory.getLogger(McpClientDiagnostics.class);

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
        int toolCount = provider.getToolCallbacks().length;
        log.info("MCP client wired up: {} tool callback(s) available from configured MCP servers "
                + "(0 is expected until a server is added under spring.ai.mcp.client.* in "
                + "application.properties).", toolCount);
    }
}
