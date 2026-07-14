import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AuthStorageService } from '../auth/auth-storage.service';


/**
 * Auth endpoints that should NOT trigger a session-expired logout.
 *
 * Example:
 *
 * User enters wrong password.
 *
 * Backend returns 401.
 *
 * We do NOT want:
 * "Your session expired"
 *
 * We want:
 * "Invalid username/password"
 */
function isAuthEndpoint(url: string): boolean {

  if (!url) {
    return false;
  }

  return /\/(api\/)?auth\/(login|register|refresh)(\/|$)/
    .test(url);

}


/**
 * Interceptor that handles expired sessions.
 *
 * This runs AFTER the backend responds.
 *
 * Flow:
 *
 * Request
 *    |
 *    v
 * Backend
 *    |
 *    v
 * 401 response
 *    |
 *    v
 * Check if we should logout
 */
export const sessionExpiredInterceptor: HttpInterceptorFn =
  (req, next) => {


    const authStorage = inject(AuthStorageService);


    return next(req).pipe(

      catchError((error: HttpErrorResponse) => {


        /*
         * Only handle 401 errors.
         *
         * 403 means:
         * - user is authenticated
         * - user does not have permission
         *
         * Therefore:
         * DO NOT logout on 403.
         */
        if (error.status !== 401) {

          return throwError(() => error);

        }


        const authState = authStorage.read();


        /*
         * Was there an active session?
         *
         * Equivalent to React:
         *
         * const hadSession =
         *    Boolean(authState.accessToken);
         */
        const hadSession =
          Boolean(authState.accessToken);



        /*
         * Find the token that was sent with THIS request.
         *
         * Example scenario:
         *
         * User A logs in
         * User A makes request
         * User A logs out
         * User B logs in
         * Old request returns 401
         *
         * We must NOT delete User B's token.
         */
        const authorizationHeader =
          req.headers.get('Authorization') || '';


        const sentToken =
          authorizationHeader.replace(
            /^Bearer\s+/,
            ''
          );


        /*
         * Confirm the failed request belongs
         * to the currently logged-in user.
         *
         * This prevents clearing a newer session.
         */
        const tokenStillCurrent =
          sentToken === authState.accessToken;



        /*
         * Determine whether this was login/register.
         *
         * A failed login is NOT a session expiration.
         */
        const isAuthRequest =
          isAuthEndpoint(req.url);



        /*
         * This matches your React condition exactly:
         *
         * status === 401
         * &&
         * hadSession
         * &&
         * tokenStillCurrent
         * &&
         * !isAuthEndpoint(requestUrl)
         */
        if (
          hadSession &&
          tokenStillCurrent &&
          !isAuthRequest
        ) {


          /*
           * Clear authentication state.
           *
           * This replaces:
           *
           * localStorage.removeItem(AUTH_STORAGE_KEY)
           */
          authStorage.clear();


          /*
           * Redirect user to login.
           *
           * Avoid redirect loops if already there.
           */
          if (
            !window.location.pathname.startsWith('/login')
          ) {

            window.location.replace(
              '/login?error=session_expired'
            );

          }

        }


        /*
         * Always pass the original error back.
         *
         * Components/services can still display
         * backend error messages.
         */
        return throwError(() => error);

      })

    );

};