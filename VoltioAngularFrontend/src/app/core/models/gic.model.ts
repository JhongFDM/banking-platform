import { GicStatus } from '../enums/gic-status.enum';
import { GicTerm } from '../enums/gic-term.enum';

/**
 * Mirrors:
 * CreateGicRequest.java
 *
 * Used when opening a new GIC.
 */
export interface CreateGicRequest {

  amount: number;

  term: GicTerm;

}

/**
 * Mirrors:
 * GicResponse.java
 *
 * Returned by GET and POST endpoints.
 */
export interface GicResponse {

  gicId: string;

  accountId: number;

  principalAmount: number;

  interestRate: number;

  term: GicTerm;

  /**
   * yyyy-MM-dd
   */
  startDate: string;

  /**
   * yyyy-MM-dd
   */
  maturityDate: string;

  maturityAmount: number;

  status: GicStatus;

  /**
   * ISO timestamps returned by Spring.
   */
  deletedAt: string | null;

  createdAt: string;

  updatedAt: string;

}

/**
 * Mirrors:
 * RedeemGicResponse.java
 */
export interface RedeemGicResponse {

  message: string;

  payoutAmount: number;

}