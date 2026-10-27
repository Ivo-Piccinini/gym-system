import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'login', component: class DummyComponent {} }])
      ]
    });

    service = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('should be created and start without active user', () => {
    expect(service).toBeTruthy();
    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should login and set session upon successful response', () => {
    const futureExp = Math.floor(Date.now() / 1000) + 3600;
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({ sub: 'admin', authorities: ['ROLE_ADMIN'], exp: futureExp }));
    const mockToken = `${header}.${payload}.signature`;

    service.login({ username: 'admin', password: 'password123' }).subscribe((response) => {
      expect(response.token).toBe(mockToken);
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ token: mockToken, message: 'Autenticación exitosa' });

    expect(service.isAuthenticated()).toBe(true);
    expect(service.username()).toBe('admin');
    expect(service.isAdmin()).toBe(true);
    expect(service.getToken()).toBe(mockToken);
  });

  it('should logout and clear session', () => {
    const futureExp = Math.floor(Date.now() / 1000) + 3600;
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({ sub: 'cliente1', authorities: ['ROLE_CLIENT'], exp: futureExp }));
    const mockToken = `${header}.${payload}.signature`;

    localStorage.setItem('gym_auth_token', mockToken);
    expect(service.getToken()).toBe(mockToken);

    service.logout();

    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
    expect(localStorage.getItem('gym_auth_token')).toBeNull();
  });
});
