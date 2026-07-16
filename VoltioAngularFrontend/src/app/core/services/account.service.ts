import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { TransferRequest, TransferResponse, AccountResponse, CreateAccountRequest, UpdateAccountRequest} from '../models/account.model'
import { MonetaryRequest, MonetaryOperationResponse } from '../models/monetary.model'
import { MessageResponse } from '../models/common.model'
import { FreezeAccountRequest, UnfreezeAccountRequest, AccountControlActionResponse, AccountControlHistoryResponse} from '../models/account-control.model';


/**
 * Angular service responsible for communicating
 * with all Account-related backend endpoints.
 *
 * This replaces the React accountApiClient wrapper.
 */
@Injectable({
  providedIn: 'root'
})
export class AccountService {

  private readonly backendBaseUrl = environment.backendBaseUrl;

  constructor(
    private http: HttpClient
  ) {}

  /**
   * Generates the headers required for money movement APIs.
   *
   * The backend expects an Idempotency-Key so duplicate
   * requests (double-clicks, retries, etc.) are ignored.
   *
   * If the caller already provides one, use it.
   * Otherwise generate a new UUID.
   */
  private buildIdempotencyHeaders(idempotencyKey?: string): HttpHeaders {

    return new HttpHeaders({
      'Idempotency-Key': idempotencyKey ?? crypto.randomUUID()
    });

  }

  /**
   * GET /accounts/{accountId}
   *
   * Retrieves a single account.
   *
   * React equivalent:
   *
   * accountApiClient.get(...)
   */
  getAccount(accountId: number): Observable<AccountResponse> {

    return this.http.get<AccountResponse>(

      `${this.backendBaseUrl}/accounts/${accountId}`

    );

  }

  /**
   * GET /customers/{customerId}/accounts
   *
   * Returns every account belonging
   * to a customer.
   *
   * The React code handled several
   * possible response formats.
   */
  listCustomerAccounts(customerId: number): Observable<AccountResponse[]> {

    return this.http.get<AccountResponse[]>(

      `${this.backendBaseUrl}/customers/${customerId}/accounts`

    );

  }

  /**
   * POST /customers/{customerId}/accounts
   *
   * Creates a new account.
   *
   * The request body depends on
   * the account type.
   */
  createAccount(customerId: number, payload: CreateAccountRequest): Observable<AccountResponse> {

    return this.http.post<AccountResponse>(

      `${this.backendBaseUrl}/customers/${customerId}/accounts`,

      payload

    );
  }

  /**
   * PUT /accounts/{accountId}
   *
   * Updates editable account fields.
   */
  updateAccount(accountId: number, payload: UpdateAccountRequest): Observable<AccountResponse> {

  return this.http.put<AccountResponse>(

    `${this.backendBaseUrl}/accounts/${accountId}`,

    payload

  );

}

  /**
   * DELETE /accounts/{accountId}
   *
   * Permanently deletes an account.
   */
  deleteAccount(accountId: number): Observable<MessageResponse> {

    return this.http.delete<MessageResponse>(

      `${this.backendBaseUrl}/accounts/${accountId}`

    );

  }

  /**
 * POST /accounts/{accountId}/deposit
 *
 * Deposits money into an account.
 *
 * This endpoint requires an Idempotency-Key header
 * to prevent duplicate deposits if the user
 * accidentally submits the request multiple times.
 *
 * React equivalent:
 *
 * depositToAccount(payload)
 */
  depositToAccount(
  accountId: number,
  payload: MonetaryRequest,
  idempotencyKey?: string
  ): Observable<MonetaryOperationResponse> {

  return this.http.post<MonetaryOperationResponse>(

    `${this.backendBaseUrl}/accounts/${accountId}/deposit`,

    payload,

    {
      headers: this.buildIdempotencyHeaders(idempotencyKey)
    }

  );

}


/**
 * POST /accounts/{accountId}/withdraw
 *
 * Withdraws money from an account.
 *
 * Like deposits, withdrawals use an
 * Idempotency-Key to prevent duplicate requests.
 *
 * React equivalent:
 *
 * withdrawFromAccount(payload)
 */
withdrawFromAccount(
  accountId: number, 
  payload: MonetaryRequest, 
  idempotencyKey?: string
): Observable<MonetaryOperationResponse> {

  return this.http.post<MonetaryOperationResponse>(

    `${this.backendBaseUrl}/accounts/${accountId}/withdraw`,

    payload,

    {

      headers: this.buildIdempotencyHeaders(idempotencyKey)

    }

  );

}


/**
 * POST /accounts/transfer
 *
 * Transfers money between two accounts.
 *
 * Matches backend:
 * TransferRequest DTO
 *
 * An Idempotency-Key header is included to prevent
 * duplicate transfers if the request is retried.
 */
transferBetweenAccounts(payload: TransferRequest, idempotencyKey?: string): Observable<TransferResponse> {

  return this.http.post<TransferResponse>(

    `${this.backendBaseUrl}/accounts/transfer`,

    payload,

    {
      headers: this.buildIdempotencyHeaders(idempotencyKey)
    }

  );

}


/**
 * POST /accounts/{accountId}/freeze
 *
 * Freezes an account.
 *
 * The backend expects a reason and
 * optionally accepts a reason code
 * and additional notes.
 *
 * React equivalent:
 *
 * freezeAccount(payload)
 */
freezeAccount(accountId: number, payload: FreezeAccountRequest): Observable<AccountControlActionResponse> {

  return this.http.post<AccountControlActionResponse>(

    `${this.backendBaseUrl}/accounts/${accountId}/freeze`,
    payload

  );

}


/**
 * POST /accounts/{accountId}/unfreeze
 *
 * Removes the freeze from an account.
 *
 * Reason and notes are optional.
 *
 * React equivalent:
 *
 * unfreezeAccount(payload)
 */
unfreezeAccount(accountId: number, payload?: UnfreezeAccountRequest): Observable<AccountControlActionResponse> {

  return this.http.post<AccountControlActionResponse>(

    `${this.backendBaseUrl}/accounts/${accountId}/unfreeze`,

    payload ?? {}

  );

}


/**
 * GET /accounts/{accountId}/control-history
 *
 * Retrieves the account's control history.
 *
 * This includes actions such as:
 * - freezes
 * - unfreezes
 * - other administrative controls
 *
 * React equivalent:
 *
 * getAccountControlHistory(accountId)
 */
getAccountControlHistory(accountId: number): Observable<AccountControlHistoryResponse> {

  return this.http.get<AccountControlHistoryResponse>(

    `${this.backendBaseUrl}/accounts/${accountId}/control-history`

  );

}

}