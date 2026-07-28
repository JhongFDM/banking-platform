import { AccountResponse } from './account.model';
import { TransactionResponse } from './transaction.model';


/**
 * Request body for:
 *
 * POST /accounts/{accountId}/deposit
 *
 * POST /accounts/{accountId}/withdraw
 *
 * Matches backend:
 * MonetaryRequest DTO
 */
export interface MonetaryRequest {

  amount: number;

  description?: string | null;

}


/**
 * Response returned by:
 *
 * POST /accounts/{accountId}/deposit
 *
 * POST /accounts/{accountId}/withdraw
 *
 * Matches backend:
 * MonetaryOperationResponse DTO
 */
export interface MonetaryOperationResponse {

  message: string;

  account: AccountResponse;

  transaction: TransactionResponse;

}