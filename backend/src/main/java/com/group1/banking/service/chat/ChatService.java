package com.group1.banking.service.chat;

import com.group1.banking.dto.chat.ChatRequest;
import com.group1.banking.dto.chat.ChatResponse;
import com.group1.banking.entity.Account;
import com.group1.banking.entity.AccountStatus;
import com.group1.banking.entity.ChatMessage;
import com.group1.banking.enums.ChatTopic;
import com.group1.banking.exception.PermissionDeniedException;
import com.group1.banking.repository.AccountRepository;
import com.group1.banking.repository.ChatMessageRepository;
import com.group1.banking.security.CustomUserPrincipal;
import com.group1.banking.service.AuditService;
import com.group1.banking.service.chat.agent.AgentResponseGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
    private final Optional<ResponseGenerator> responseGenerator;
    private final ChatMessageRepository chatMessageRepository;
    private final AuditService auditService;
    private final AccountRepository accountRepository;
    private final Optional<AgentResponseGenerator> agentResponseGenerator;

    /**
     * Demo-safe traceability toggle (business requirement: "store ... in
     * non-production or demo-safe form"). Defaults on for this feature's
     * initial release; set chatbot.persistence.enabled=false to disable
     * storage entirely (e.g. in a real production profile) without
     * touching this class.
     */
    @Value("${chatbot.persistence.enabled:true}")
    private boolean persistenceEnabled;

    /**
     * template/groq both go through responseGenerator (built from a
     * pre-vetted SafeChatContext). agent is a different shape entirely --
     * it needs the raw caller/accountId to run tools live -- so it's
     * branched on directly in handle() rather than living behind the same
     * ResponseGenerator interface. See handleAgentTurn.
     */
    @Value("${chatbot.generator:template}")
    private String generatorMode;

    public ChatService(ChatGuardrailService guardrailService,
                        ChatContextBuilderService contextBuilderService,
                        Optional<ResponseGenerator> responseGenerator,
                        ChatMessageRepository chatMessageRepository,
                        AuditService auditService,
                        AccountRepository accountRepository,
                        Optional<AgentResponseGenerator> agentResponseGenerator) {
        this.guardrailService = guardrailService;
        this.contextBuilderService = contextBuilderService;
        this.responseGenerator = responseGenerator;
        this.chatMessageRepository = chatMessageRepository;
        this.auditService = auditService;
        this.accountRepository = accountRepository;
        this.agentResponseGenerator = agentResponseGenerator;
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

        if ("agent".equals(generatorMode) && agentResponseGenerator.isPresent()) {
            return handleAgentTurn(caller, request.getAccountId(), rawQuery, topic);
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
            generation = responseGenerator.orElseThrow(() ->
                            new IllegalStateException("No ResponseGenerator bean active for chatbot.generator=" + generatorMode))
                    .generate(rawQuery, topic, context);
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

    /**
     * chatbot.generator=agent path. Deliberately does NOT call
     * contextBuilderService.build() -- that method's whole job is
     * pre-fetching a fixed bundle of facts for a generator that can't ask
     * for more, which is exactly backwards for a generator that decides
     * what it needs via tools. The one piece of that method worth keeping
     * unconditionally is the frozen/closed-account short-circuit: that's a
     * hard security gate, not a data-shaping choice, so it's re-checked
     * directly here rather than trusted to the model's tool choices.
     */
    private ChatResponse handleAgentTurn(CustomUserPrincipal caller, Long accountId, String rawQuery, ChatTopic topic) {
        if (accountId != null) {
            Account account = accountRepository.findByAccountIdAndDeletedAtIsNull(accountId).orElse(null);
            if (account != null) {
                if (!account.getCustomer().getCustomerId().equals(caller.getCustomerId())) {
                    throw new PermissionDeniedException("CHAT:ACCOUNT_OWNERSHIP");
                }
                if (account.getStatus() != AccountStatus.ACTIVE) {
                    return respond(caller, accountId, rawQuery,
                            "I can only give you general tips right now because there's a restriction on this "
                                    + "account. For anything account-specific, please contact support.",
                            List.of(), topic, false, true);
                }
            }
        }

        ChatGeneration generation;
        try {
            generation = agentResponseGenerator.get().generate(rawQuery, topic, caller, accountId);
        } catch (RuntimeException ex) {
            log.warn("Agentic chat generation failed for customer {}: {}", caller.getCustomerId(), ex.toString(), ex);
            return respond(caller, accountId, rawQuery,
                    "I'm having trouble putting together an answer right now. Please try again in a moment.",
                    List.of(), topic, false, false);
        }

        if (!guardrailService.isSafeToReturn(generation.reply())) {
            return respond(caller, accountId, rawQuery, FALLBACK_UNSAFE_OUTPUT, List.of(), topic, true, false);
        }

        return respond(caller, accountId, rawQuery, generation.reply(), generation.basis(), topic, false, false);
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
