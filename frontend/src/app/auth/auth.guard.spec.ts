import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { authGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('authGuard', () => {
  let mockAuthService: {
    isAuthenticated: ReturnType<typeof vi.fn>;
    requiresPasswordChange: ReturnType<typeof vi.fn>;
  };
  let router: Router;

  beforeEach(() => {
    mockAuthService = {
      isAuthenticated: vi.fn(),
      requiresPasswordChange: vi.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService }
      ]
    });

    router = TestBed.inject(Router);
  });

  it('should allow access if user is authenticated and does not require password change', () => {
    mockAuthService.isAuthenticated.mockReturnValue(true);
    mockAuthService.requiresPasswordChange.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() => authGuard({} as any, { url: '/perfil' } as any));

    expect(result).toBe(true);
  });

  it('should redirect to /cambiar-credenciales if user is authenticated but requires password change', () => {
    mockAuthService.isAuthenticated.mockReturnValue(true);
    mockAuthService.requiresPasswordChange.mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() => authGuard({} as any, { url: '/perfil' } as any)) as UrlTree;

    expect(result instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(result)).toBe('/cambiar-credenciales');
  });

  it('should redirect to /login if user is not authenticated', () => {
    mockAuthService.isAuthenticated.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() => authGuard({} as any, { url: '/perfil' } as any)) as UrlTree;

    expect(result instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(result)).toBe('/login');
  });
});
