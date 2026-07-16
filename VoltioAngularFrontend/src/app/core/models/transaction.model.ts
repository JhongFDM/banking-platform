import { TransactionDirection } from '../enums/transaction-direction.enum';
import { TransactionStatus } from '../enums/transaction-status.enum';

/**
 * Response returned by transaction-related endpoints.
 *
 * Matches backend:
 * com.group1.banking.dto.customer.TransactionResponse
 */
export interface TransactionResponse {

  transactionId: string;

  accountId: number;

  amount: number;

  direction: TransactionDirection;

  status: TransactionStatus;

  /**
   * ISO-8601 timestamp returned by Spring.
   *
   * Example:
   * 2026-07-16T18:30:45Z
   */
  timestamp: string;

  description: string | null;

  senderInfo: string | null;

  receiverInfo: string | null;

  idempotencyKey: string | null;

}