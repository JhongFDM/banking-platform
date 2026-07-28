import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpParams
} from '@angular/common/http';

import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TransactionHistoryResponse } from '../models/transaction-history.model';

/**
 * Service responsible for Transaction History endpoints.
 *
 * Mirrors:
 *
 * TransactionController.java
 */
@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  private readonly backendBaseUrl =
    environment.backendBaseUrl;

  constructor(
    private http: HttpClient
  ) {}

  /**
   * GET
   * /accounts/{accountId}/transactions
   *
   * Returns transaction history.
   *
   * startDate and endDate are optional.
   */
  getTransactionHistory(
    accountId: number,
    startDate?: string,
    endDate?: string
  ): Observable<TransactionHistoryResponse> {

    let params = new HttpParams();

    if (startDate) {

      params =
        params.set('startDate', startDate);

    }

    if (endDate) {

      params =
        params.set('endDate', endDate);

    }

    return this.http.get<TransactionHistoryResponse>(

      `${this.backendBaseUrl}/accounts/${accountId}/transactions`,

      {
        params
      }

    );

  }


  /**
   * GET
   * /accounts/{accountId}/transactions/export
   *
   * Downloads a PDF.
   *
   * Angular receives PDFs as a Blob.
   *
   * This satisfies the story acceptance criterion:
   *
   * "Blob responses are supported."
   */
  exportTransactionsPdf(
    accountId: number,
    startDate?: string,
    endDate?: string
  ): Observable<Blob> {

    let params = new HttpParams();

    if (startDate) {

      params =
        params.set('startDate', startDate);

    }

    if (endDate) {

      params =
        params.set('endDate', endDate);

    }

    return this.http.get(

      `${this.backendBaseUrl}/accounts/${accountId}/transactions/export`,

      {

        params,

        responseType: 'blob'

      }

    );

  }

}