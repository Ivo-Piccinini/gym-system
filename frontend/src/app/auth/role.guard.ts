import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from './auth.service';

export const roleGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  _state: RouterStateSnapshot
) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  if (authService.requiresPasswordChange()) {
    return router.createUrlTree(['/cambiar-credenciales']);
  }

  const expectedRoles: string[] = Array.isArray(route.data?.['roles'])
    ? route.data['roles']
    : route.data?.['role']
      ? [route.data['role']]
      : [];

  if (expectedRoles.length > 0 && !authService.hasAnyRole(expectedRoles)) {
    return router.createUrlTree(['/perfil']);
  }

  return true;
};

