package com.group1.banking.controller;

import com.group1.banking.dto.chat.ChatRequest;
import com.group1.banking.dto.chat.ChatResponse;
import com.group1.banking.exception.PermissionDeniedException;
import com.group1.banking.security.CustomUserPrincipal;
import com.group1.banking.service.chat.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Savings Insight Chatbot controller.
 *
 * Single endpoint by design (business requirement: "a bounded set of
 * prompt/response interactions"). No endpoint accepts a customerId or
 * ownership identifier from the client -- it's always taken from the JWT
 * principal, same as SavingsGoalController/InsightController.
 */
@RestController
@RequestMapping("/api/chat")
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(chatService.handle(request, extractPrincipal(principal)));
    }

    private CustomUserPrincipal extractPrincipal(CustomUserPrincipal principal) {
        if (principal == null) {
            throw new PermissionDeniedException("CHAT:AUTHENTICATION");
        }
        return principal;
    }
}
