import { AccountStatus } from '../enums/account-status.enum';
import { AccountControlActionType } from '../enums/account-control-action-type.enum';


/**
 * Request body for:
 *
 * POST /accounts/{accountId}/freeze
 *
 * Matches backend:
 * FreezeAccountRequest DTO
 */
export interface FreezeAccountRequest {

  /**
   * Reason the account is being frozen.
   */
  reason: string;

  /**
   * Optional reason code used internally.
   */
  reasonCode?: string | null;

  /**
   * Additional admin notes.
   */
  notes?: string | null;

}


/**
 * Request body for:
 *
 * POST /accounts/{accountId}/unfreeze
 *
 * Matches backend:
 * UnfreezeAccountRequest DTO
 */
export interface UnfreezeAccountRequest {

  reason?: string | null;

  notes?: string | null;

}


/**
 * Response returned by:
 *
 * POST /accounts/{accountId}/freeze
 *
 * POST /accounts/{accountId}/unfreeze
 *
 * Matches backend:
 * AccountControlActionResponse DTO
 */
export interface AccountControlActionResponse {

  accountId: number;

  previousStatus: AccountStatus;

  newStatus: AccountStatus;

  actionType: AccountControlActionType;

  timestamp: string;

}


/**
 * Single event in account control history.
 *
 * Matches backend:
 * AccountControlHistoryEventResponse DTO
 */
export interface AccountControlHistoryEventResponse {

  eventId: number;

  actionType: AccountControlActionType;

  previousStatus: AccountStatus;

  newStatus: AccountStatus;

  adminUserId: string;

  adminRole: string;

  reason: string;

  notes: string;

  timestamp: string;

}


/**
 * Response returned by:
 *
 * GET /accounts/{accountId}/control-history
 *
 * Matches backend:
 * AccountControlHistoryResponse DTO
 */
export interface AccountControlHistoryResponse {

  accountId: number;

  events: AccountControlHistoryEventResponse[];

}