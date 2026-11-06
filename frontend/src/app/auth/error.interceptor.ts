import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        const isLoginRequest = req.url.includes('/api/auth/login');
        if (!isLoginRequest) {
          authService.logout();
        }
      } else if (error.status === 403) {
        if (authService.isAuthenticated()) {
          router.navigate(['/perfil']);
        } else {
          authService.logout();
        }
      }

      return throwError(() => error);
    })
  );
};

