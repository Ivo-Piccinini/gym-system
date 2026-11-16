import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { roleGuard } from './role.guard';
import { AuthService } from './auth.service';

describe('roleGuard', () => {
  let mockAuthService: {
    isAuthenticated: ReturnType<typeof vi.fn>;
    requiresPasswordChange: ReturnType<typeof vi.fn>;
    hasAnyRole: ReturnType<typeof vi.fn>;
  };
  let router: Router;

  const mockRoute = (data: Record<string, unknown> = {}): ActivatedRouteSnapshot => {
    return { data } as unknown as ActivatedRouteSnapshot;
  };

  const mockState = (): RouterStateSnapshot => {
    return { url: '/admin/usuarios' } as RouterStateSnapshot;
  };

  beforeEach(() => {
    mockAuthService = {
      isAuthenticated: vi.fn(),
      requiresPasswordChange: vi.fn(),
      hasAnyRole: vi.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService }
      ]
    });

    router = TestBed.inject(Router);
  });

  it('debe redirigir a /login si el usuario no esta autenticado', () => {
    mockAuthService.isAuthenticated.mockReturnValue(false);
    mockAuthService.requiresPasswordChange.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(mockRoute({ roles: ['ROLE_ADMIN'] }), mockState())
    ) as UrlTree;

    expect(result instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(result)).toBe('/login');
  });

  it('debe redirigir a /cambiar-credenciales si tiene clave temporal pendiente', () => {
    mockAuthService.isAuthenticated.mockReturnValue(true);
    mockAuthService.requiresPasswordChange.mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(mockRoute({ roles: ['ROLE_ADMIN'] }), mockState())
    ) as UrlTree;

    expect(result instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(result)).toBe('/cambiar-credenciales');
  });

  it('debe permitir la navegacion si el usuario tiene el rol requerido', () => {
    mockAuthService.isAuthenticated.mockReturnValue(true);
    mockAuthService.requiresPasswordChange.mockReturnValue(false);
    mockAuthService.hasAnyRole.mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(mockRoute({ roles: ['ROLE_ADMIN'] }), mockState())
    );

    expect(result).toBe(true);
    expect(mockAuthService.hasAnyRole).toHaveBeenCalledWith(['ROLE_ADMIN']);
  });

  it('debe redirigir a /perfil si el usuario carece del rol necesario', () => {
    mockAuthService.isAuthenticated.mockReturnValue(true);
    mockAuthService.requiresPasswordChange.mockReturnValue(false);
    mockAuthService.hasAnyRole.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(mockRoute({ roles: ['ROLE_ADMIN'] }), mockState())
    ) as UrlTree;

    expect(result instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(result)).toBe('/perfil');
  });

  it('debe permitir la navegacion si la ruta no restringe roles', () => {
    mockAuthService.isAuthenticated.mockReturnValue(true);
    mockAuthService.requiresPasswordChange.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(mockRoute(), mockState())
    );

    expect(result).toBe(true);
  });
});
