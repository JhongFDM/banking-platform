import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  SavingsGoalRequest,
  SavingsGoalResponse
} from '../models/savings-goal.model';

import { mapSavingsGoalError } from '../errors/savings-goal-error.mapper';

/**
 * Angular service responsible for communicating
 * with the Savings Goal backend endpoints.
 *
 * Mirrors SavingsGoalController.java
 */
@Injectable({
  providedIn: 'root'
})
export class SavingsGoalService {

  private readonly backendBaseUrl =
    environment.backendBaseUrl;

  constructor(
    private http: HttpClient
  ) {}

  /**
   * POST /api/goals/accounts/{accountId}
   */
  createGoal(
    accountId: number,
    request: SavingsGoalRequest
  ): Observable<SavingsGoalResponse> {

    return this.http.post<SavingsGoalResponse>(
      `${this.backendBaseUrl}/api/goals/accounts/${accountId}`,
      request
    ).pipe(
      catchError(error =>
        throwError(() => mapSavingsGoalError(error))
      )
    );

  }

  /**
   * GET /api/goals/accounts/{accountId}
   */
  getGoal(
    accountId: number
  ): Observable<SavingsGoalResponse> {

    return this.http.get<SavingsGoalResponse>(
      `${this.backendBaseUrl}/api/goals/accounts/${accountId}`
    ).pipe(
      catchError(error =>
        throwError(() => mapSavingsGoalError(error))
      )
    );

  }

  /**
   * GET /api/goals/customers/{customerId}
   */
  getCustomerGoals(
    customerId: number
  ): Observable<SavingsGoalResponse[]> {

    return this.http.get<SavingsGoalResponse[]>(
      `${this.backendBaseUrl}/api/goals/customers/${customerId}`
    ).pipe(
      catchError(error =>
        throwError(() => mapSavingsGoalError(error))
      )
    );

  }

  /**
   * PUT /api/goals/accounts/{accountId}/goals/{goalId}
   */
  updateGoal(
    accountId: number,
    goalId: number,
    request: SavingsGoalRequest
  ): Observable<SavingsGoalResponse> {

    return this.http.put<SavingsGoalResponse>(
      `${this.backendBaseUrl}/api/goals/accounts/${accountId}/goals/${goalId}`,
      request
    ).pipe(
      catchError(error =>
        throwError(() => mapSavingsGoalError(error))
      )
    );

  }

  /**
   * DELETE /api/goals/accounts/{accountId}/goals/{goalId}
   */
  deleteGoal(
    accountId: number,
    goalId: number
  ): Observable<void> {

    return this.http.delete<void>(
      `${this.backendBaseUrl}/api/goals/accounts/${accountId}/goals/${goalId}`
    ).pipe(
      catchError(error =>
        throwError(() => mapSavingsGoalError(error))
      )
    );

  }

}