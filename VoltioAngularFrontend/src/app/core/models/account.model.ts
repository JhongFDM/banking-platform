import { AccountType } from '../enums/account-type.enum'
import { AccountStatus } from '../enums/account-status.enum'
import { TransactionResponse } from './transaction.model'

export interface AccountResponse {

  accountId: number;

  customerId: number;

  accountType: AccountType;

  status: AccountStatus;

  balance: number;

  interestRate: number;

  createdAt: string;

  updatedAt: string;

}

/**
 * Request body for:
 *
 * POST /customers/{customerId}/accounts
 *
 * Matches backend:
 * CreateAccountRequest
 */
export interface CreateAccountRequest {

  accountType: AccountType;

  balance: number;

  /**
   * Required only for certain account types.
   * 
   * SAVINGS
   * TFSA
   */
  interestRate?: number;

}

/**
 * Request body for:
 *
 * PUT /accounts/{accountId}
 *
 * Matches backend:
 * UpdateAccountRequest
 */
export interface UpdateAccountRequest {

  interestRate?: number;

}

/**
 * Request body for:
 *
 * POST /accounts/transfer
 *
 * Matches backend:
 * TransferRequest DTO
 */
export interface TransferRequest {

  fromAccountId: number;

  toAccountId: number;

  amount: number;

  description?: string | null;

}

/**
 * Response body for:
 * 
 * POST /accounts/transfer
 * 
 * Matches backend:
 * TransferResponse DTO
 */
export interface TransferResponse {

  /**
   * Success message returned by the backend.
   */
  message: string;

  fromAccount: AccountResponse;
  toAccount: AccountResponse;

  debitTransaction: TransactionResponse;

  creditTransaction: TransactionResponse;

}