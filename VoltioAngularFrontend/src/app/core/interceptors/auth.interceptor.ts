import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthStorageService } from '../auth/auth-storage.service';

/**
 * HTTP interceptor responsible for attaching the access token
 * to outgoing API requests.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authStorage = inject(AuthStorageService);

  const token = authStorage.getAccessToken();


  /*
   * If there is no valid token:
   *
   * - Do not modify the request.
   * - Let it continue normally.
   * - login requests do not have tokens yet
   * - register requests do not have tokens yet
   */
  if (!token) {
    return next(req);
  }


  const authenticatedRequest = req.clone({

    setHeaders: {
      Authorization: `Bearer ${token}`
    }

  });


  return next(authenticatedRequest);

};