import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

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
  getAccount(accountId: string): Observable<any> {

    return this.http.get(

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
  listCustomerAccounts(
    customerId: string
  ): Observable<any> {

    return this.http.get(

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
  createAccount(payload: any): Observable<any> {

    /*
     * Build the request body expected
     * by the backend.
     */
    const body: any = {

      accountType: payload.accountType,

      balance: Number(payload.balance)

    };


    /*
     * RRSP accounts always begin
     * with zero balance.
     */
    if (payload.accountType === 'RRSP') {

      body.balance = 0;

      body.interestRate = 0.5;

    }

    /*
     * Savings and TFSA accounts
     * require an interest rate.
     */
    else if (

      payload.accountType === 'SAVINGS' ||

      payload.accountType === 'TFSA'

    ) {

      body.interestRate =
        Number(payload.interestRate);

    }

    /*
     * TFSA accounts additionally
     * require date of birth.
     */
    if (

      payload.accountType === 'TFSA' &&

      payload.dateOfBirth

    ) {

      body.dateOfBirth =
        payload.dateOfBirth;

    }


    return this.http.post(

      `${this.backendBaseUrl}/customers/${payload.customerId}/accounts`,

      body

    );

  }

  /**
   * PUT /accounts/{accountId}
   *
   * Updates editable account fields.
   */
  updateAccount(payload: any): Observable<any> {

    const body: any = {};

    /*
     * Only send the interest rate
     * if the user entered one.
     */
    if (payload.interestRate !== '') {

      body.interestRate =
        payload.interestRate;

    }

    return this.http.put(

      `${this.backendBaseUrl}/accounts/${payload.accountId}`,

      body

    );

  }

  /**
   * DELETE /accounts/{accountId}
   *
   * Permanently deletes an account.
   */
  deleteAccount(
    accountId: string
  ): Observable<any> {

    return this.http.delete(

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
depositToAccount(payload: any): Observable<any> {

  return this.http.post(

    `${this.backendBaseUrl}/accounts/${payload.accountId}/deposit`,

    {

      amount: payload.amount,

      description: payload.description ?? null,

      category: payload.category ?? null

    },

    {

      headers: this.buildIdempotencyHeaders(
        payload.idempotencyKey
      )

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
withdrawFromAccount(payload: any): Observable<any> {

  return this.http.post(

    `${this.backendBaseUrl}/accounts/${payload.accountId}/withdraw`,

    {

      amount: payload.amount,

      description: payload.description ?? null,

      category: payload.category ?? null

    },

    {

      headers: this.buildIdempotencyHeaders(
        payload.idempotencyKey
      )

    }

  );

}


/**
 * POST /accounts/transfer
 *
 * Transfers money between two accounts.
 *
 * This also requires an Idempotency-Key
 * because money is moving between accounts.
 *
 * React equivalent:
 *
 * transferBetweenAccounts(payload)
 */
transferBetweenAccounts(
  payload: any
): Observable<any> {

  return this.http.post(

    `${this.backendBaseUrl}/accounts/transfer`,

    {

      fromAccountId: payload.fromAccountId,

      toAccountId: payload.toAccountId,

      amount: payload.amount,

      description: payload.description ?? null,

      category: payload.category ?? null

    },

    {

      headers: this.buildIdempotencyHeaders(
        payload.idempotencyKey
      )

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
freezeAccount(
  payload: any
): Observable<any> {

  return this.http.post(

    `${this.backendBaseUrl}/accounts/${payload.accountId}/freeze`,

    {

      reason: payload.reason,

      reasonCode: payload.reasonCode ?? null,

      notes: payload.notes ?? null

    }

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
unfreezeAccount(
  payload: any
): Observable<any> {

  return this.http.post(

    `${this.backendBaseUrl}/accounts/${payload.accountId}/unfreeze`,

    {

      reason: payload.reason ?? null,

      notes: payload.notes ?? null

    }

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
getAccountControlHistory(
  accountId: string
): Observable<any> {

  return this.http.get(

    `${this.backendBaseUrl}/accounts/${accountId}/control-history`

  );

}

}