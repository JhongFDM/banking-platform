package com.group1.banking.service.chat;

import com.group1.banking.enums.ChatTopic;

/**
 * Generation layer, kept behind an interface on purpose (per the feature's
 * NFR that retrieval, prompt construction, and generation be specifiable
 * independently). TemplateResponseGenerator is the phase-1 implementation:
 * fully deterministic, no external model call, which is what makes the
 * bounded QA scenarios in the spec ("same persona/query set produces
 * predictable outputs") achievable. GroqResponseGenerator is the real-LLM
 * implementation, swapped in via chatbot.generator=groq.
 */
public interface ResponseGenerator {

    ChatGeneration generate(String rawQuery, ChatTopic topic, SafeChatContext context);
}
