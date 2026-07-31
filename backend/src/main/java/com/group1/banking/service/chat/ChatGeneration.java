package com.group1.banking.service.chat;

import java.util.List;

/**
 * Shape both response generators return, so ChatService doesn't need to
 * care which one produced it.
 */
public record ChatGeneration(String reply, List<String> basis) {
}
