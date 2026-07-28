import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  NotificationEventRequest,
  NotificationDecisionResponse
} from '../models/notification.model';



/**
 * Handles notification evaluation APIs.
 *
 * Mirrors:
 * NotificationController
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {


  private readonly backendBaseUrl =
    environment.backendBaseUrl;



  constructor(
    private http: HttpClient
  ) {}



  /**
   * POST /notifications/evaluate
   *
   * Evaluates whether a notification
   * should be generated for an event.
   */
  evaluateNotification(
    payload: NotificationEventRequest
  ): Observable<NotificationDecisionResponse> {


    return this.http.post<NotificationDecisionResponse>(

      `${this.backendBaseUrl}/notifications/evaluate`,

      payload

    );

  }

}