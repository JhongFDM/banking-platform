import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth';

export const defaultRouteGuard: CanActivateFn = () => {
  const authService = inject(Auth);
  const router = inject(Router);

  if (authService.userRole() === 'ADMIN') {
    return router.createUrlTree(['/home/all-accounts']);
  }

  return router.createUrlTree(['/home/dashboard']);
};