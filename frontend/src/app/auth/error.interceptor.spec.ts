import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { errorInterceptor } from './error.interceptor';
import { AuthService } from './auth.service';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let mockAuthService: {
    logout: ReturnType<typeof vi.fn>;
    isAuthenticated: ReturnType<typeof vi.fn>;
  };
  let mockRouter: {
    navigate: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    mockAuthService = {
      logout: vi.fn(),
      isAuthenticated: vi.fn()
    };
    mockRouter = {
      navigate: vi.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter }
      ]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debe ejecutar logout al interceptar un error 401 Unauthorized fuera de login', () => {
    let errorCaught = false;

    http.get('/api/users/me').subscribe({
      next: () => {},
      error: (err) => {
        errorCaught = true;
        expect(err.status).toBe(401);
      }
    });

    const req = httpMock.expectOne('/api/users/me');
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(errorCaught).toBe(true);
    expect(mockAuthService.logout).toHaveBeenCalled();
  });

  it('no debe ejecutar logout si el error 401 proviene del login', () => {
    let errorCaught = false;

    http.post('/api/auth/login', {}).subscribe({
      next: () => {},
      error: (err) => {
        errorCaught = true;
        expect(err.status).toBe(401);
      }
    });

    const req = httpMock.expectOne('/api/auth/login');
    req.flush('Credenciales incorrectas', { status: 401, statusText: 'Unauthorized' });

    expect(errorCaught).toBe(true);
    expect(mockAuthService.logout).not.toHaveBeenCalled();
  });

  it('debe navegar a /perfil al recibir 403 Forbidden estando autenticado', () => {
    mockAuthService.isAuthenticated.mockReturnValue(true);
    let errorCaught = false;

    http.get('/api/admin/users').subscribe({
      next: () => {},
      error: (err) => {
        errorCaught = true;
        expect(err.status).toBe(403);
      }
    });

    const req = httpMock.expectOne('/api/admin/users');
    req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });

    expect(errorCaught).toBe(true);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/perfil']);
  });

  it('debe ejecutar logout al recibir 403 Forbidden sin estar autenticado', () => {
    mockAuthService.isAuthenticated.mockReturnValue(false);
    let errorCaught = false;

    http.get('/api/admin/users').subscribe({
      next: () => {},
      error: (err) => {
        errorCaught = true;
        expect(err.status).toBe(403);
      }
    });

    const req = httpMock.expectOne('/api/admin/users');
    req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });

    expect(errorCaught).toBe(true);
    expect(mockAuthService.logout).toHaveBeenCalled();
  });

  it('debe propagar el error 409 Conflict sin cerrar sesion', () => {
    let errorCaught = false;

    http.post('/api/membership/subscriptions', {}).subscribe({
      next: () => {},
      error: (err) => {
        errorCaught = true;
        expect(err.status).toBe(409);
      }
    });

    const req = httpMock.expectOne('/api/membership/subscriptions');
    req.flush('Conflicto de membresia', { status: 409, statusText: 'Conflict' });

    expect(errorCaught).toBe(true);
    expect(mockAuthService.logout).not.toHaveBeenCalled();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });
});
