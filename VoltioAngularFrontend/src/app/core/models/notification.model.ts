/**
 * Request payload for notification evaluation.
 *
 * Backend:
 * NotificationEventRequest
 */
export interface NotificationEventRequest {

  eventId: string;

  eventType: string;

  accountId: number;

  customerId: number;

  businessTimestamp: string;

  payload?: string | null;

}


/**
 * Response after evaluating
 * whether a notification should be sent.
 *
 * Backend:
 * NotificationDecisionResponse
 */
export interface NotificationDecisionResponse {

  eventId: string;

  decision: string;

  decisionReason: string;

  customerId: number;

  accountId: number;

  evaluatedAt: string;

  mandatoryOverride: boolean;

}