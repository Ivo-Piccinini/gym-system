import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { UserService } from './user.service';
import { environment } from '../../environments/environment';
import { AuthResponse, PasswordChangeRequest, UsernameChangeRequest } from './auth.models';

describe('UserService', () => {
  let service: UserService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        UserService
      ]
    });

    service = TestBed.inject(UserService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should send PATCH request to change password', () => {
    const request: PasswordChangeRequest = {
      oldPassword: 'oldPassword123',
      newPassword: 'newPassword123',
      confirmPassword: 'newPassword123'
    };

    service.changePassword(request).subscribe();

    const req = httpTesting.expectOne(`${environment.apiUrl}/users/change-password`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(request);
    req.flush(null);
  });

  it('should send PATCH request to change username and receive auth response', () => {
    const request: UsernameChangeRequest = {
      newUsername: 'nuevo_usuario'
    };
    const mockResponse: AuthResponse = {
      token: 'new-jwt-token',
      message: 'Nombre de usuario cambiado con éxito.',
      requiresPasswordChange: false
    };

    service.changeUsername(request).subscribe((res) => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/users/change-username`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });
});

