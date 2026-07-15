import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

import {
  AuthApiService,
  LoginRequest,
  AuthResponse
} from '../services/auth-api.service';

import { AuthStorageService } from './auth-storage.service';


/**
 * Application-level authentication service.
 *
 * Responsibilities:
 * - Login users
 * - Register users
 * - Store authentication state
 * - Logout users
 * - Provide authentication status
 *
 * This sits between components and AuthApiService.
 *
 * Flow:
 *
 * Login Component
 *        |
 *        v
 * AuthService
 *        |
 *        v
 * AuthApiService
 *        |
 *        v
 * Spring Boot backend
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {


  constructor(
    private authApi: AuthApiService,
    private authStorage: AuthStorageService
  ) {}



  /**
   * Logs a user into the application.
   *
   * This calls the backend through AuthApiService,
   * then stores the returned token locally.
   *
   * React equivalent:
   *
   * loginUser(payload)
   * +
   * localStorage.setItem(...)
   */
  login(
    credentials: LoginRequest
  ): Observable<AuthResponse> {


    return this.authApi.login(credentials)
      .pipe(

        tap((response: AuthResponse) => {


          /*
           * Save authentication information.
           *
           * This allows:
           * - auth.interceptor to attach the token
           * - session-expired interceptor to clear it
           */
        this.authStorage.write({

            accessToken: response.accessToken,

            expiresAt: response.expiresAt ?? null,

            refreshToken: response.refreshToken ?? null,

            customerId: response.customerId ?? null

});


        })

      );

  }



  /**
   * Registers a new user.
   *
   * After registration succeeds, store the
   * returned authentication state if the backend
   * automatically logs the user in.
   *
   * If your backend only returns user information
   * and not a token, remove the storage step.
   */
  register(
    payload: unknown
  ): Observable<AuthResponse> {


    return this.authApi.register(payload)
      .pipe(

        tap((response: AuthResponse) => {


          /*
           * Store token if registration
           * returns an authenticated session.
           */
          if (response.accessToken) {

            this.authStorage.write({

                accessToken: response.accessToken,

                expiresAt: response.expiresAt ?? null,

                refreshToken: response.refreshToken ?? null,

                customerId: response.customerId ?? null

            });

          }


        })

      );

  }



  /**
   * Logs the user out.
   *
   * Clears:
   * - access token
   * - refresh token
   * - expiration information
   * - any other stored auth state
   *
   * React equivalent:
   *
   * localStorage.removeItem(AUTH_STORAGE_KEY)
   */
  logout(): void {


    this.authStorage.clear();


  }



  /**
   * Returns whether a user currently has
   * a valid authentication token.
   *
   * Useful for:
   * - route guards
   * - showing/hiding UI
   */
  isAuthenticated(): boolean {


    return this.authStorage.hasFreshToken();


  }



  /**
   * Returns the current authentication state.
   *
   * Useful when components need user information.
   */
  getAuthState() {


    return this.authStorage.read();


  }



  /**
   * Returns the current user's access token.
   *
   * Most API calls should NOT use this directly.
   *
   * The interceptor handles adding tokens automatically.
   */
  getAccessToken(): string | null {


    return this.authStorage.getAccessToken();


  }

}