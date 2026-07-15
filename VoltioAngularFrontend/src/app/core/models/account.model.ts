import { AccountType } from '../enums/account-type.enum'
import { AccountStatus } from '../enums/account-status.enum'

export interface Account {

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