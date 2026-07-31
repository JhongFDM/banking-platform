package com.group1.banking.service.chat.llm;

/**
 * Wraps any generation-provider failure -- bad key, timeout, non-2xx
 * response, malformed body. ChatService catches this (and any other
 * RuntimeException) and converts it to a safe fallback; the customer never
 * sees a provider error message directly.
 */
public class ChatGenerationException extends RuntimeException {

    public ChatGenerationException(String message) {
        super(message);
    }

    public ChatGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
