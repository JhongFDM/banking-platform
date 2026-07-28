import { CustomerType } from '../enums/customer-type.enum';
import { AccountResponse } from './account.model';

/**
 * Mirrors:
 * CreateCustomerRequest
 */
export interface CreateCustomerRequest {

  name: string;

  address: string;

  type: CustomerType;

  /**
   * Backend expects yyyy-MM-dd.
   */
  dateOfBirth: string;

  kycVerified: boolean;

}

/**
 * Mirrors:
 * PatchCustomerRequest in backend
 *
 * Every field is optional because PATCH
 * updates only the supplied values.
 */
export interface PatchCustomerRequest {

  name?: string;

  address?: string;

  type?: CustomerType;

  /**
   * Included because the backend accepts them,
   * although the service layer ignores changes to these fields.
   */
  email?: string;

  accountNumber?: string;

}

/**
 * Mirrors:
 * CustomerResponse.java
 *
 * Returned by all customer endpoints.
 */
export interface CustomerResponse {

  customerId: number;

  name: string;

  address: string;

  type: CustomerType;

  /**
   * yyyy-MM-dd
   */
  dateOfBirth: string;

  kycVerified: boolean;

  accounts: AccountResponse[];

  /**
   * ISO-8601 timestamps returned by Spring.
   */
  createdAt: string;

  updatedAt: string;

  deletedAt: string | null;

}