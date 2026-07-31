package com.group1.banking.enums;

/**
 * The bounded set of topics the Savings Insight Chatbot is allowed to
 * answer. Doubles as both the knowledge-base retrieval filter and the
 * guardrail's allow-list -- anything that doesn't classify as one of the
 * first three becomes UNSUPPORTED, and the response generator never runs
 * for it (see ChatGuardrailService.classify / ChatService.handle).
 */
public enum ChatTopic {
    SAVINGS,
    SPENDING_TRENDS,
    GENERAL_WELLNESS,
    UNSUPPORTED
}
