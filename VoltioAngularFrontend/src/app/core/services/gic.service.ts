import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  CreateGicRequest,
  GicResponse,
  RedeemGicResponse
} from '../models/gic.model';

/**
 * Angular service responsible for
 * communicating with the GIC endpoints.
 *
 * Mirrors the Spring GicController.
 */
@Injectable({
  providedIn: 'root'
})
export class GicService {

  /**
   * Base backend URL.
   */
  private readonly backendBaseUrl =
    environment.backendBaseUrl;

  constructor(
    private http: HttpClient
  ) {}

  /**
   * POST /accounts/{accountId}/gic
   *
   * Opens a new GIC investment.
   *
   * Mirrors:
   * GicController.createGic(...)
   */
  createGic(
    accountId: number,
    payload: CreateGicRequest
  ): Observable<GicResponse> {

    return this.http.post<GicResponse>(

      `${this.backendBaseUrl}/accounts/${accountId}/gic`,

      payload

    );

  }

  /**
   * GET /accounts/{accountId}/gic
   *
   * Returns every GIC belonging
   * to the account.
   *
   * Mirrors:
   * GicController.getGics(...)
   */
  getGics(
    accountId: number
  ): Observable<GicResponse[]> {

    return this.http.get<GicResponse[]>(

      `${this.backendBaseUrl}/accounts/${accountId}/gic`

    );

  }

  /**
   * POST /accounts/{accountId}/gic/{gicId}/redeem
   *
   * Redeems a GIC investment.
   *
   * Mirrors:
   * GicController.redeemGic(...)
   *
   * The backend expects no request body,
   * so an empty object is sent.
   */
  redeemGic(
    accountId: number,
    gicId: string
  ): Observable<RedeemGicResponse> {

    return this.http.post<RedeemGicResponse>(

      `${this.backendBaseUrl}/accounts/${accountId}/gic/${gicId}/redeem`,

      {}

    );

  }

}