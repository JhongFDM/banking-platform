import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  SavingsGoalRequest,
  SavingsGoalResponse
} from '../models/savings-goal.model';

/**
 * Angular service responsible for communicating
 * with the Savings Goal backend endpoints.
 *
 * Mirrors:
 *
 * SavingsGoalController.java
 */
@Injectable({
  providedIn: 'root'
})
export class SavingsGoalService {

  /**
   * Backend base URL.
   */
  private readonly backendBaseUrl = environment.backendBaseUrl;

  constructor(
    private http: HttpClient
  ) {}


  /**
   * POST /api/goals/accounts/{accountId}
   *
   * Creates a savings goal for an account.
   */
  createGoal(
    accountId: number,
    request: SavingsGoalRequest
  ): Observable<SavingsGoalResponse> {

    return this.http.post<SavingsGoalResponse>(

      `${this.backendBaseUrl}/api/goals/accounts/${accountId}`,

      request

    );

  }


  /**
   * GET /api/goals/accounts/{accountId}
   *
   * Retrieves the savings goal associated
   * with an account.
   */
  getGoal(
    accountId: number
  ): Observable<SavingsGoalResponse> {

    return this.http.get<SavingsGoalResponse>(

      `${this.backendBaseUrl}/api/goals/accounts/${accountId}`

    );

  }


  /**
   * GET /api/goals/customers/{customerId}
   *
   * Returns every savings goal belonging
   * to a customer.
   */
  getCustomerGoals(
    customerId: number
  ): Observable<SavingsGoalResponse[]> {

    return this.http.get<SavingsGoalResponse[]>(

      `${this.backendBaseUrl}/api/goals/customers/${customerId}`

    );

  }


  /**
   * PUT /api/goals/accounts/{accountId}/goals/{goalId}
   *
   * Updates an existing savings goal.
   */
  updateGoal(
    accountId: number,
    goalId: number,
    request: SavingsGoalRequest
  ): Observable<SavingsGoalResponse> {

    return this.http.put<SavingsGoalResponse>(

      `${this.backendBaseUrl}/api/goals/accounts/${accountId}/goals/${goalId}`,

      request

    );

  }


  /**
   * DELETE /api/goals/accounts/{accountId}/goals/{goalId}
   *
   * Soft deletes a savings goal.
   *
   * Backend returns:
   * 204 No Content
   */
  deleteGoal(
    accountId: number,
    goalId: number
  ): Observable<void> {

    return this.http.delete<void>(

      `${this.backendBaseUrl}/api/goals/accounts/${accountId}/goals/${goalId}`

    );

  }

}