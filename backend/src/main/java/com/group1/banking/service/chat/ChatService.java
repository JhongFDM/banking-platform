package com.group1.banking.service.chat;

import com.group1.banking.dto.chat.ChatRequest;
import com.group1.banking.dto.chat.ChatResponse;
import com.group1.banking.entity.ChatMessage;
import com.group1.banking.enums.ChatTopic;
import com.group1.banking.exception.PermissionDeniedException;
import com.group1.banking.repository.ChatMessageRepository;
import com.group1.banking.security.CustomUserPrincipal;
import com.group1.banking.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Orchestrates a single chat turn: classify -> (short-circuit if blocked) ->
 * build context -> generate -> re-check output -> persist -> respond.
 *
 * Kept intentionally thin -- each step is delegated to a single-purpose
 * collaborator so the retrieval, guardrail, and generation pieces really
 * can be replaced independently later, as the feature's NFRs call for.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private static final String FALLBACK_UNSUPPORTED =
            "I can help with savings, spending trends, and general financial wellness questions, "
                    + "but I'm not able to help with that one. For account-specific decisions like loans, "
                    + "investments, taxes, or legal/medical matters, please speak with the right specialist "
                    + "or contact support.";

    private static final String FALLBACK_UNSAFE_OUTPUT =
            "I wasn't able to put together a reliable answer to that one. Please try rephrasing, "
                    + "or contact support if you need help with your account.";

    private final ChatGuardrailService guardrailService;
    private final ChatContextBuilderService contextBuilderService;
    private final ResponseGenerator responseGenerator;
    private final ChatMessageRepository chatMessageRepository;
    private final AuditService auditService;

    /**
     * Demo-safe traceability toggle (business requirement: "store ... in
     * non-production or demo-safe form"). Defaults on for this feature's
     * initial release; set chatbot.persistence.enabled=false to disable
     * storage entirely (e.g. in a real production profile) without
     * touching this class.
     */
    @Value("${chatbot.persistence.enabled:true}")
    private boolean persistenceEnabled;

    public ChatService(ChatGuardrailService guardrailService,
                        ChatContextBuilderService contextBuilderService,
                        ResponseGenerator responseGenerator,
                        ChatMessageRepository chatMessageRepository,
                        AuditService auditService) {
        this.guardrailService = guardrailService;
        this.contextBuilderService = contextBuilderService;
        this.responseGenerator = responseGenerator;
        this.chatMessageRepository = chatMessageRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ChatResponse handle(ChatRequest request, CustomUserPrincipal caller) {
        if (caller == null) {
            throw new PermissionDeniedException("CHAT:AUTHENTICATION");
        }

        String rawQuery = request.getMessage().trim();
        ChatTopic topic = guardrailService.classify(rawQuery);

        if (topic == ChatTopic.UNSUPPORTED) {
            return respond(caller, request.getAccountId(), rawQuery, FALLBACK_UNSUPPORTED,
                    List.of(), topic, true, false);
        }

        SafeChatContext context;
        try {
            context = contextBuilderService.build(caller, request.getAccountId(), rawQuery, topic);
        } catch (RuntimeException ex) {
            // Retrieval failure -> safe fallback rather than a hallucinated answer (FR-010).
            log.warn("Chat context build failed for customer {}: {}", caller.getCustomerId(), ex.toString(), ex);
            return respond(caller, request.getAccountId(), rawQuery,
                    "I couldn't pull up your account details just now, so I can't personalize this answer. "
                            + "Please try again shortly.",
                    List.of(), topic, false, true);
        }

        boolean limitedData = context.limitedTransactionData() && context.limitedGoalData();

        ChatGeneration generation;
        try {
            generation = responseGenerator.generate(rawQuery, topic, context);
        } catch (RuntimeException ex) {
            // Covers Groq outages, missing API keys, timeouts, etc.
            // A generation-layer failure degrades to a safe fallback rather than
            // a raw 500 -- the customer never sees provider error details, but we
            // log the real cause here since this is the only place it's visible.
            log.warn("Chat response generation failed for customer {}: {}", caller.getCustomerId(), ex.toString(), ex);
            return respond(caller, request.getAccountId(), rawQuery,
                    "I'm having trouble putting together an answer right now. Please try again in a moment.",
                    List.of(), topic, false, limitedData);
        }

        if (!guardrailService.isSafeToReturn(generation.reply())) {
            return respond(caller, request.getAccountId(), rawQuery, FALLBACK_UNSAFE_OUTPUT,
                    List.of(), topic, true, limitedData);
        }

        return respond(caller, request.getAccountId(), rawQuery, generation.reply(),
                generation.basis(), topic, false, limitedData);
    }

    private ChatResponse respond(CustomUserPrincipal caller, Long accountId, String rawQuery,
                                  String replyText, List<String> basis, ChatTopic topic,
                                  boolean blocked, boolean limitedData) {
        Long chatMessageId = null;

        if (persistenceEnabled) {
            ChatMessage message = new ChatMessage();
            message.setCustomerId(caller.getCustomerId());
            message.setAccountId(accountId);
            message.setQueryText(truncate(rawQuery, 1000));
            message.setResponseText(truncate(replyText, 2000));
            message.setTopic(topic);
            message.setBlocked(blocked);
            message.setLimitedData(limitedData);
            chatMessageId = chatMessageRepository.save(message).getChatMessageId();
        }

        auditService.log(caller.getCustomerId().toString(), "CUSTOMER", "CHATBOT_QUERY",
                "chat_message", chatMessageId != null ? String.valueOf(chatMessageId) : "not-persisted",
                blocked ? "BLOCKED" : "SUCCESS");

        return new ChatResponse(chatMessageId, replyText, basis, topic, blocked, limitedData, Instant.now());
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}
