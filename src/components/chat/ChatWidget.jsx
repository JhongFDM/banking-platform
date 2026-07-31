import { useEffect, useRef, useState } from "react";
import useChat from "../../hooks/useChat";
import "./ChatWidget.css";

/**
 * The entire client-side UI for the Savings Insight Chatbot: a floating
 * button that opens a chat panel with a disclaimer, scrollable message
 * history, citation bullets under any reply that has a basis, a visibly
 * different style for fallback/blocked replies, and a simple input form.
 *
 * Contains zero business logic -- no guardrail checks, no context
 * building, nothing -- by design, for the same reason this app already
 * keeps things like ownership checks server-side rather than trusting the
 * client. All of that lives in ChatService/ChatGuardrailService on the
 * backend; this component just renders whatever useChat gives it back.
 */
export default function ChatWidget({ accountId = null }) {
  const [open, setOpen] = useState(false);
  const [draft, setDraft] = useState("");
  const { messages, sending, sendMessage } = useChat();
  const scrollRef = useRef(null);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages, open]);

  async function handleSubmit(e) {
    e.preventDefault();
    const text = draft;
    setDraft("");
    try {
      await sendMessage(text, accountId);
    } catch {
      // useChat already appended an error-flagged assistant message --
      // nothing else to do here.
    }
  }

  return (
    <div className="chat-widget-root">
      {open && (
        <div className="chat-widget-panel" role="dialog" aria-label="Savings Insight Assistant">
          <div className="chat-widget-header">
            <span>Savings Insight Assistant</span>
            <button
              type="button"
              className="chat-widget-close"
              aria-label="Close chat"
              onClick={() => setOpen(false)}
            >
              ×
            </button>
          </div>

          <p className="chat-widget-disclaimer">
            I can help with savings, spending trends, and general financial wellness questions.
            I can't give investment, loan, legal, tax, or medical advice.
          </p>

          <div className="chat-widget-messages" ref={scrollRef}>
            {messages.length === 0 && (
              <p className="chat-widget-empty">Ask me something like "how are my savings going?"</p>
            )}
            {messages.map((m) => (
              <div
                key={m.id}
                className={`chat-widget-message chat-widget-message-${m.role}${
                  m.blocked ? " chat-widget-message-blocked" : ""
                }`}
              >
                <p>{m.text}</p>
                {m.basis && m.basis.length > 0 && (
                  <ul className="chat-widget-basis">
                    {m.basis.map((b, i) => (
                      <li key={i}>{b}</li>
                    ))}
                  </ul>
                )}
              </div>
            ))}
            {sending && (
              <div className="chat-widget-message chat-widget-message-assistant chat-widget-message-pending">
                <p>Thinking…</p>
              </div>
            )}
          </div>

          <form className="chat-widget-form" onSubmit={handleSubmit}>
            <input
              type="text"
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              placeholder="Ask about savings or spending…"
              maxLength={500}
              disabled={sending}
              aria-label="Message"
            />
            <button type="submit" disabled={sending || !draft.trim()}>
              Send
            </button>
          </form>
        </div>
      )}

      <button
        type="button"
        className="chat-widget-toggle"
        onClick={() => setOpen((v) => !v)}
        aria-label={open ? "Close savings assistant" : "Open savings assistant"}
      >
        {open ? "×" : "💬"}
      </button>
    </div>
  );
}
