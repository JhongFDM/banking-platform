package com.group1.banking.service.chat.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Collects every ChatTool bean Spring knows about -- adding a new tool
 * class is the entire integration step, nothing here needs to change.
 */
@Component
public class ChatToolRegistry {

    private final Map<String, ChatTool> toolsByName;

    public ChatToolRegistry(List<ChatTool> tools) {
        this.toolsByName = tools.stream().collect(Collectors.toMap(ChatTool::name, tool -> tool));
    }

    public ChatTool find(String name) {
        return toolsByName.get(name);
    }

    /** Builds the {"type":"function","function":{...}} schema array Groq's tools parameter expects. */
    public ArrayNode toolSchemas(ObjectMapper mapper) {
        ArrayNode array = mapper.createArrayNode();
        for (ChatTool tool : toolsByName.values()) {
            ObjectNode function = mapper.createObjectNode();
            function.put("name", tool.name());
            function.put("description", tool.description());
            function.set("parameters", mapper.valueToTree(tool.parametersSchema()));

            ObjectNode wrapper = mapper.createObjectNode();
            wrapper.put("type", "function");
            wrapper.set("function", function);
            array.add(wrapper);
        }
        return array;
    }
}
