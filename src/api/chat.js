/**
 * Savings Insight Chatbot API wrapper.
 *
 * Follows the same pattern as src/api/goals.js: uses the shared axios
 * client (JWT attached automatically, 401 handling built in), and never
 * sends a customerId -- the backend resolves that from the authenticated
 * principal (see ChatRequest.java for why).
 */
import { accountApiClient as axiosClient } from "./axiosClient";

const CHAT_API = {
  /**
   * Send a message to the savings insight chatbot.
   * POST /api/chat
   *
   * @param {string} message - the customer's natural-language question
   * @param {number|null} [accountId] - optional account to scope context to
   * @returns {Promise<{chatMessageId:number|null, reply:string, basis:string[], topic:string, blocked:boolean, limitedData:boolean, respondedAt:string}>}
   */
  sendMessage: async (message, accountId = null) => {
    const response = await axiosClient.post("/api/chat", {
      message,
      accountId: accountId ?? undefined,
    });
    return response.data;
  },
};

export default CHAT_API;
