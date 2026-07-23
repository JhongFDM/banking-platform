import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(Auth);
  const router = inject(Router);

  if (authService.userRole() === 'ADMIN') {
    return true; 
  }

  router.navigate(['/home/dashboard']);
  return false; 
};