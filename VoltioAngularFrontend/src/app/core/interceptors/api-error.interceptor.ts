import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

import { mapApiError } from '../errors/api-error.mapper';

/**
 * Global API error interceptor.
 *
 * Converts HttpErrorResponse objects into the
 * application's standard ApiError format.
 *
 * This replaces having catchError(mapApiError)
 * inside every service method.
 */
export const apiErrorInterceptor: HttpInterceptorFn = (

  req,

  next

) => {

  return next(req).pipe(

    catchError((error: HttpErrorResponse) => {

      /*
       * Convert the backend error into the
       * application's standard error object.
       */
      const mappedError = mapApiError(error);

      /*
       * Pass the mapped error back to
       * the component.
       */
      return throwError(() => mappedError);

    })

  );

};