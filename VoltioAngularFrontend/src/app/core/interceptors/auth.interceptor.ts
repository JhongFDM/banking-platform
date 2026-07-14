import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthStorageService } from '../auth/auth-storage.service';

/**
 * HTTP interceptor responsible for attaching the access token
 * to outgoing API requests.
 *
 * This replaces the Axios request interceptor:
 *
 * axios.interceptors.request.use((config) => {
 *    const authState = readStoredAuthState();
 *    ...
 * })
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {

  /*
   * Angular interceptors do not use constructor injection.
   * Instead, we retrieve services using inject().
   */
  const authStorage = inject(AuthStorageService);


  /*
   * Ask AuthStorageService for a valid token.
   *
   * This internally checks:
   * - Does a token exist?
   * - Is it expired?
   *
   * This replaces your React logic:
   *
   * const tokenFresh =
   *   authState.accessToken &&
   *   (!authState.expiresAt ||
   *    authState.expiresAt > Date.now());
   */
  const token = authStorage.getAccessToken();


  /*
   * If there is no valid token:
   *
   * - Do not modify the request.
   * - Let it continue normally.
   *
   * This is important because:
   * - login requests do not have tokens yet
   * - register requests do not have tokens yet
   */
  if (!token) {
    return next(req);
  }


  /*
   * HttpRequest objects in Angular are immutable.
   *
   * We cannot do:
   *
   * req.headers.Authorization = token ❌
   *
   * Instead we create a cloned request with the new header.
   */
  const authenticatedRequest = req.clone({

    setHeaders: {
      Authorization: `Bearer ${token}`
    }

  });


  /*
   * Continue the HTTP request chain.
   *
   * The backend receives:
   *
   * Authorization: Bearer eyJhbGciOi...
   */
  return next(authenticatedRequest);

};