import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import {apiErrorInterceptor} from './core/interceptors/api-error.interceptor';
import {authInterceptor} from './core/interceptors/auth.interceptor'
import {sessionExpiredInterceptor} from './core/interceptors/session-expired.interceptor'
import {authErrorInterceptor} from './core/interceptors/auth-error.interceptor'
import {idempotencyInterceptor} from './core/interceptors/idempotency.interceptor'

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([
        authInterceptor,
        sessionExpiredInterceptor,
        apiErrorInterceptor,
        authErrorInterceptor,
        idempotencyInterceptor
      ])
    )
  ]
};
