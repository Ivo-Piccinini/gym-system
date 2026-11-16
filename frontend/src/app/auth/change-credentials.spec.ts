import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ChangeCredentials } from './change-credentials';
import { AuthService } from './auth.service';
import { UserService } from './user.service';
import { AuthResponse } from './auth.models';

describe('ChangeCredentials Component', () => {
  let component: ChangeCredentials;
  let fixture: ComponentFixture<ChangeCredentials>;
  let mockAuthService: {
    username: ReturnType<typeof signal<string>>;
    logout: ReturnType<typeof vi.fn>;
    clearPasswordChangeRequirement: ReturnType<typeof vi.fn>;
    getDefaultRedirectUrl: ReturnType<typeof vi.fn>;
  };
  let mockUserService: {
    changePassword: ReturnType<typeof vi.fn>;
    changeUsername: ReturnType<typeof vi.fn>;
  };
  let mockRouter: {
    navigateByUrl: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    mockAuthService = {
      username: signal('12345678'),
      logout: vi.fn(),
      clearPasswordChangeRequirement: vi.fn(),
      getDefaultRedirectUrl: vi.fn().mockReturnValue('/perfil')
    };

    mockUserService = {
      changePassword: vi.fn(),
      changeUsername: vi.fn()
    };

    mockRouter = {
      navigateByUrl: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [ChangeCredentials],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: UserService, useValue: mockUserService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ChangeCredentials);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create with invalid initial form', () => {
    expect(component).toBeTruthy();
    expect(component['credentialsForm'].valid).toBe(false);
  });

  it('should detect password mismatch', () => {
    component['credentialsForm'].setValue({
      oldPassword: '12345678',
      newPassword: 'newPassword123',
      confirmPassword: 'differentPassword',
      newUsername: 'juanperez'
    });

    expect(component['credentialsForm'].hasError('passwordMismatch')).toBe(true);
    expect(component['credentialsForm'].valid).toBe(false);
  });

  it('should reject new username identical to current username', () => {
    component['credentialsForm'].setValue({
      oldPassword: '12345678',
      newPassword: 'newPassword123',
      confirmPassword: 'newPassword123',
      newUsername: '12345678'
    });

    component['onSubmit']();

    expect(component['errorMessage']()).toContain('debe ser diferente al actual');
    expect(mockUserService.changePassword).not.toHaveBeenCalled();
  });

  it('should successfully change password and username and navigate to redirect url', () => {
    vi.useFakeTimers();

    component['credentialsForm'].setValue({
      oldPassword: '12345678',
      newPassword: 'newPassword123',
      confirmPassword: 'newPassword123',
      newUsername: 'juan_perez'
    });

    const mockAuthResponse: AuthResponse = {
      token: 'new-token-123',
      message: 'Nombre de usuario cambiado con éxito.',
      requiresPasswordChange: false
    };

    mockUserService.changePassword.mockReturnValue(of(undefined));
    mockUserService.changeUsername.mockReturnValue(of(mockAuthResponse));

    component['onSubmit']();

    expect(mockUserService.changePassword).toHaveBeenCalledWith({
      oldPassword: '12345678',
      newPassword: 'newPassword123',
      confirmPassword: 'newPassword123'
    });

    expect(mockUserService.changeUsername).toHaveBeenCalledWith({
      newUsername: 'juan_perez'
    });

    expect(mockAuthService.clearPasswordChangeRequirement).toHaveBeenCalledWith(
      'new-token-123',
      'juan_perez'
    );
    expect(component['successMessage']()).toContain('actualizadas exitosamente');

    vi.advanceTimersByTime(1600);
    expect(mockRouter.navigateByUrl).toHaveBeenCalledWith('/perfil');

    vi.useRealTimers();
  });

  it('should display error when password change fails', () => {
    component['credentialsForm'].setValue({
      oldPassword: 'wrongPassword',
      newPassword: 'newPassword123',
      confirmPassword: 'newPassword123',
      newUsername: 'juan_perez'
    });

    mockUserService.changePassword.mockReturnValue(
      throwError(() => ({ status: 400, error: { message: 'La contraseña actual es incorrecta.' } }))
    );

    component['onSubmit']();

    expect(component['errorMessage']()).toContain('La contraseña actual es incorrecta');
    expect(mockUserService.changeUsername).not.toHaveBeenCalled();
  });

  it('should call authService.logout when clicking logout', () => {
    component['logout']();
    expect(mockAuthService.logout).toHaveBeenCalled();
  });
});

