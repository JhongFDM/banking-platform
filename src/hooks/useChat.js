/**
 * useChat Hook
 *
 * Manages the conversation transcript for the Savings Insight Chatbot.
 * Mirrors the style of useGoals.js (plain useState/useCallback, no extra
 * dependency) so it drops into this codebase without introducing a new
 * state-management pattern.
 *
 * Each transcript entry: { id, role: 'user' | 'assistant', text, basis,
 * blocked, limitedData, createdAt }
 */
import { useCallback, useState } from "react";
import chatAPI from "../api/chat";
import { mapAxiosError } from "../api/axiosClient";

let localIdCounter = 0;
function nextLocalId() {
  localIdCounter += 1;
  return `local-${localIdCounter}`;
}

const useChat = () => {
  const [messages, setMessages] = useState([]);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState(null);

  const sendMessage = useCallback(async (text, accountId = null) => {
    const trimmed = (text || "").trim();
    if (!trimmed) {
      return null;
    }

    setError(null);
    setSending(true);

    const userEntry = {
      id: nextLocalId(),
      role: "user",
      text: trimmed,
      createdAt: new Date().toISOString(),
    };
    setMessages((prev) => [...prev, userEntry]);

    try {
      const response = await chatAPI.sendMessage(trimmed, accountId);
      const assistantEntry = {
        id: response.chatMessageId ?? nextLocalId(),
        role: "assistant",
        text: response.reply,
        basis: response.basis || [],
        topic: response.topic,
        blocked: Boolean(response.blocked),
        limitedData: Boolean(response.limitedData),
        createdAt: response.respondedAt || new Date().toISOString(),
      };
      setMessages((prev) => [...prev, assistantEntry]);
      return assistantEntry;
    } catch (err) {
      const mapped = mapAxiosError(err);
      setError(mapped);
      setMessages((prev) => [
        ...prev,
        {
          id: nextLocalId(),
          role: "assistant",
          text:
            mapped.message ||
            "Something went wrong reaching the assistant. Please try again.",
          basis: [],
          blocked: true,
          limitedData: false,
          createdAt: new Date().toISOString(),
          isError: true,
        },
      ]);
      throw err;
    } finally {
      setSending(false);
    }
  }, []);

  const clearConversation = useCallback(() => {
    setMessages([]);
    setError(null);
  }, []);

  return {
    messages,
    sending,
    error,
    sendMessage,
    clearConversation,
  };
};

export default useChat;
