import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';


/**
 * Service responsible for account statement endpoints.
 *
 * Mirrors:
 *
 * StatementController.java
 *
 * Backend endpoint:
 *
 * GET /accounts/{accountId}/statements/{period}
 *
 */
@Injectable({
  providedIn: 'root'
})
export class StatementService {


  /**
   * Backend base URL.
   */
  private readonly backendBaseUrl =
    environment.backendBaseUrl;


  constructor(
    private http: HttpClient
  ) {}



  /**
   * GET /accounts/{accountId}/statements/{period}
   *
   * Downloads a monthly account statement PDF.
   *
   * Spring returns:
   *
   * ResponseEntity<byte[]>
   *
   * Angular receives:
   *
   * Blob
   *
   * Example period:
   *
   * "2026-01"
   *
   */
  getStatement(
    accountId: number,
    period: string
  ): Observable<Blob> {


    return this.http.get(

      `${this.backendBaseUrl}/accounts/${accountId}/statements/${period}`,

      {
        responseType: 'blob'
      }

    );

  }

}