import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  CreateStandingOrderRequest,
  StandingOrderResponse,
  StandingOrderListResponse,
  CancelStandingOrderResponse
} from '../models/standing-order.model';



/**
 * Handles standing order APIs.
 *
 * Mirrors:
 * StandingOrderController
 */
@Injectable({
  providedIn: 'root'
})
export class StandingOrderService {


  private readonly backendBaseUrl =
    environment.backendBaseUrl;

  constructor(
    private http: HttpClient
  ) {}


  /**
   * POST /accounts/{accountId}/standing-orders
   *
   * Creates a new standing order.
   */
  createStandingOrder(
    accountId: number,
    payload: CreateStandingOrderRequest
  ): Observable<StandingOrderResponse> {


    return this.http.post<StandingOrderResponse>(

      `${this.backendBaseUrl}/accounts/${accountId}/standing-orders`,

      payload

    );

  }




  /**
   * GET /accounts/{accountId}/standing-orders
   *
   * Retrieves all standing orders
   * belonging to an account.
   */
  listStandingOrders(
    accountId: number
  ): Observable<StandingOrderListResponse> {


    return this.http.get<StandingOrderListResponse>(

      `${this.backendBaseUrl}/accounts/${accountId}/standing-orders`

    );

  }




  /**
   * DELETE /standing-orders/{standingOrderId}
   *
   * Cancels a standing order.
   */
  cancelStandingOrder(
    standingOrderId: string
  ): Observable<CancelStandingOrderResponse> {


    return this.http.delete<CancelStandingOrderResponse>(

      `${this.backendBaseUrl}/standing-orders/${standingOrderId}`

    );

  }

}