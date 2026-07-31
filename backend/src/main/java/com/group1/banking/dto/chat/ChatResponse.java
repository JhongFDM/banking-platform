package com.group1.banking.dto.chat;

import com.group1.banking.enums.ChatTopic;

import java.time.Instant;
import java.util.List;

public class ChatResponse {

    private Long chatMessageId;
    private String reply;

    /**
     * Plain-language basis statements, e.g. "Based on your Dining spend over
     * the last 30 days" -- satisfies the explainability requirement without
     * exposing internal fields the reply was computed from.
     */
    private List<String> basis;

    private ChatTopic topic;
    private boolean blocked;
    private boolean limitedData;
    private Instant respondedAt;

    public ChatResponse() {
    }

    public ChatResponse(Long chatMessageId, String reply, List<String> basis, ChatTopic topic,
                         boolean blocked, boolean limitedData, Instant respondedAt) {
        this.chatMessageId = chatMessageId;
        this.reply = reply;
        this.basis = basis;
        this.topic = topic;
        this.blocked = blocked;
        this.limitedData = limitedData;
        this.respondedAt = respondedAt;
    }

    public Long getChatMessageId() { return chatMessageId; }
    public void setChatMessageId(Long chatMessageId) { this.chatMessageId = chatMessageId; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public List<String> getBasis() { return basis; }
    public void setBasis(List<String> basis) { this.basis = basis; }

    public ChatTopic getTopic() { return topic; }
    public void setTopic(ChatTopic topic) { this.topic = topic; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public boolean isLimitedData() { return limitedData; }
    public void setLimitedData(boolean limitedData) { this.limitedData = limitedData; }

    public Instant getRespondedAt() { return respondedAt; }
    public void setRespondedAt(Instant respondedAt) { this.respondedAt = respondedAt; }
}
