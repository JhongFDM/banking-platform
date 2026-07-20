import { HttpInterceptorFn } from '@angular/common/http';

import { generateIdempotencyKey } from '../utils/idempotency';

export const idempotencyInterceptor: HttpInterceptorFn = (req, next) => {

  // Only POST requests
  if (req.method !== 'POST') {
    return next(req);
  }

  // Only money movement endpoints
  const requiresIdempotency =

    req.url.includes('/deposit') ||

    req.url.includes('/withdraw') ||

    req.url.includes('/transfer');

  if (!requiresIdempotency) {
    return next(req);
  }

  const cloned = req.clone({

    setHeaders: {

      'Idempotency-Key': generateIdempotencyKey()

    }

  });

  return next(cloned);

};