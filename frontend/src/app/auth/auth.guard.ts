import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  if (authService.requiresPasswordChange()) {
    if (state.url !== '/cambiar-credenciales') {
      return router.createUrlTree(['/cambiar-credenciales']);
    }
  }

  return true;
};
