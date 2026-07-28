import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  SpendingInsightResponse,
  RecategoriseRequest,
  RecategoriseResponse
} from '../models/insights.model';


/**
 * Handles spending insights APIs.
 *
 * Mirrors:
 * SpendingInsightService endpoints exposed
 * through InsightController.
 */
@Injectable({
  providedIn: 'root'
})
export class InsightsService {


  private readonly backendBaseUrl =
    environment.backendBaseUrl;

  constructor(
    private http: HttpClient
  ) {}

  /**
   * GET /accounts/{accountId}/insights
   *
   * Retrieves spending analytics
   * for a specific month.
   */
  getInsights(
    accountId: number,
    year: number,
    month: number
  ): Observable<SpendingInsightResponse> {


    const params = new HttpParams()

      .set('year', year)

      .set('month', month);



    return this.http.get<SpendingInsightResponse>(

      `${this.backendBaseUrl}/accounts/${accountId}/insights`,

      {
        params
      }

    );

  }




  /**
   * PUT /accounts/{accountId}/transactions/{transactionId}/category
   *
   * Updates a transaction category.
   */
  recategoriseTransaction(
    accountId: number,
    transactionId: string,
    payload: RecategoriseRequest
  ): Observable<RecategoriseResponse> {


    return this.http.put<RecategoriseResponse>(

      `${this.backendBaseUrl}/accounts/${accountId}/transactions/${transactionId}/category`,

      payload

    );

  }

}