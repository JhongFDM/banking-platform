package com.voltio.mcptestserver;

import java.time.Instant;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * The one tool this test server exposes over MCP. Deliberately trivial and
 * side-effect-free - its only job is to prove a real MCP client-server
 * round trip works, not to do anything useful on its own.
 */
@Component
public class PingTool {

    @Tool(description = "Returns a fixed reply plus this server's current time. "
            + "Used only to prove an MCP client can reach and call this test server - "
            + "it has no real function beyond that.")
    public String ping() {
        return "pong from mcp-test-server at " + Instant.now();
    }
}
