import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { catchError, throwError } from 'rxjs';

import { mapApiError } from '../errors/api-error.mapper';

import { environment } from '../../../environments/environment';


/**
 * Shape of the login request.
 *
 * Adjust fields to match your backend.
 */
export interface LoginRequest {

  username: string;

  password: string;

}


/**
 * Shape returned by your backend after login/register.
 *
 * Update this based on the actual API response.
 */
export interface AuthResponse {

  accessToken: string;

  refreshToken?: string;

  expiresAt?: number;

  customerId?: string;

}


@Injectable({
  providedIn: 'root'
})
export class AuthApiService {


  /*
   * Base backend URL.
   *
   * This replaces:
   *
   * axios.create({
   *    baseURL: mergedBackendBaseUrl
   * })
   */
  private readonly backendBaseUrl =
    environment.backendBaseUrl;



  constructor(
    private http: HttpClient
  ) {}



  /**
   * Registers a new user.
   *
   * React equivalent:
   *
   * registerUser(payload)
   */
    register(
    payload: unknown
    ): Observable<AuthResponse> {

    return this.http.post<AuthResponse>(

    `${this.backendBaseUrl}/api/auth/register`,

    payload

    ).pipe(

    catchError(error => {

      return throwError(() => mapApiError(error));

    })

  );

}



  /**
   * Logs a user in.
   *
   * React equivalent:
   *
   * loginUser(payload)
   */
login(
  payload: LoginRequest
): Observable<AuthResponse> {

  return this.http.post<AuthResponse>(

    `${this.backendBaseUrl}/api/auth/login`,

    payload

  ).pipe(

    catchError(error => {

      return throwError(() => mapApiError(error));

    })

  );

}


}