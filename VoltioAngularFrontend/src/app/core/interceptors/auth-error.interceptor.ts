import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AuthStorageService } from '../auth/auth-storage.service';


/**
 * HTTP interceptor responsible for handling authentication failures.
 *
 * Rules as per ticket:
 * - 401 responses clear auth state only if the request used the current token.
 * - 403 responses do NOT clear auth state.
 * - Login/register failures do NOT trigger logout behaviour.
 */
export const authErrorInterceptor: HttpInterceptorFn = (req, next) => {

  const authStorage = inject(AuthStorageService);

  const tokenAtRequestStart = authStorage.getAccessToken();


  return next(req).pipe(

    catchError(error => {


      if (
        error.status === 401 &&
        tokenAtRequestStart &&
        tokenAtRequestStart === authStorage.getAccessToken()
      ) {

        authStorage.clear();

      }


      /*
       * - 403 is permission denied, not session expiry.
       * - login/register 401 responses should remain normal errors.
       */
      return throwError(() => error);

    })

  );

};