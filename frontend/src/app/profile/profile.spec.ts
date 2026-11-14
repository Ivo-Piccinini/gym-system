import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { provideRouter } from '@angular/router';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Profile } from './profile';
import { ProfileService } from './profile.service';
import { AuthService } from '../auth/auth.service';
import { UserProfile } from './profile.models';
import { AccessPassService } from '../access/access-pass.service';
import { AccessPass } from '../access/access-pass.models';

describe('Profile Component', () => {
  let component: Profile;
  let fixture: ComponentFixture<Profile>;
  let mockProfileService: {
    getProfile: ReturnType<typeof vi.fn>;
    updateProfile: ReturnType<typeof vi.fn>;
  };
  let mockAccessPassService: {
    getMyPass: ReturnType<typeof vi.fn>;
  };
  let mockAuthService: {
    username: ReturnType<typeof signal<string>>;
    userRoles: ReturnType<typeof signal<string[]>>;
    isAuthenticated: ReturnType<typeof signal<boolean>>;
    isAdmin: ReturnType<typeof signal<boolean>>;
    isProfessor: ReturnType<typeof signal<boolean>>;
    isClient: ReturnType<typeof signal<boolean>>;
    logout: ReturnType<typeof vi.fn>;
  };

  const mockPass: AccessPass = {
    publicId: 'pass-uuid-1',
    userPublicId: 'uuid-1234-5678',
    clientName: 'Lionel Messi',
    clientDni: '33445566',
    clientEmail: 'lionel@gym.com',
    planName: 'Plan Trimestral',
    accessCode: 'GYM-PASS-33445566-ABCD',
    qrData: '{"code":"GYM-PASS-33445566-ABCD"}',
    qrImageBase64: 'data:image/png;base64,mockImage',
    generatedBy: 'admin',
    generatedAt: '2026-11-14T19:00:00',
    validUntil: '2027-02-12',
    active: true
  };

  const initialProfile: UserProfile = {
    publicId: 'uuid-1234-5678',
    firstName: 'Lionel',
    lastName: 'Messi',
    email: 'lionel@gym.com',
    dni: '33445566',
    phone: '3415551010',
    birthDate: '1987-06-24',
    role: 'ROLE_CLIENT'
  };

  beforeEach(async () => {
    mockProfileService = {
      getProfile: vi.fn().mockReturnValue(of(initialProfile)),
      updateProfile: vi.fn()
    };

    mockAccessPassService = {
      getMyPass: vi.fn().mockReturnValue(of(mockPass))
    };

    mockAuthService = {
      username: signal('leomessi'),
      userRoles: signal(['ROLE_CLIENT']),
      isAuthenticated: signal(true),
      isAdmin: signal(false),
      isProfessor: signal(false),
      isClient: signal(true),
      logout: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [Profile],
      providers: [
        provideRouter([]),
        { provide: ProfileService, useValue: mockProfileService },
        { provide: AccessPassService, useValue: mockAccessPassService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Profile);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load profile data on init', () => {
    expect(component).toBeTruthy();
    expect(mockProfileService.getProfile).toHaveBeenCalled();
    expect(component['userProfile']()).toEqual(initialProfile);

   
    expect(component['profileForm'].value.phone).toBe('3415551010');
    expect(component['profileForm'].value.birthDate).toBe('1987-06-24');
  });

  it('should render readonly account fields and lock indicators', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const textContent = compiled.textContent;

    expect(textContent).toContain('33445566');
    expect(textContent).toContain('lionel@gym.com');
    expect(textContent).toContain('Socio / Cliente');
    expect(textContent).toContain('uuid-1234-5678');
  });

  it('should validate phone pattern and mark form as invalid on bad input', () => {
    const phoneControl = component['profileForm'].controls.phone;
    phoneControl.setValue('abc');
    expect(phoneControl.invalid).toBe(true);

    phoneControl.setValue('123456789');
    expect(phoneControl.valid).toBe(true);
  });

  it('should submit patch update and show success message', () => {
    const updatedProfile: UserProfile = {
      ...initialProfile,
      phone: '3419998877',
      birthDate: '1987-06-25'
    };

    mockProfileService.updateProfile.mockReturnValue(of(updatedProfile));

    component['profileForm'].patchValue({
      phone: '3419998877',
      birthDate: '1987-06-25'
    });
    component['profileForm'].markAsDirty();

    component['onSubmit']();

    expect(mockProfileService.updateProfile).toHaveBeenCalledWith({
      phone: '3419998877',
      birthDay: '1987-06-25',
      birthDate: '1987-06-25'
    });
    expect(component['userProfile']()).toEqual(updatedProfile);
    expect(component['successMessage']()).toContain('actualizados correctamente');
    expect(component['isSaving']()).toBe(false);
  });

  it('should handle error when update fails', () => {
    mockProfileService.updateProfile.mockReturnValue(
      throwError(() => ({ status: 400 }))
    );

    component['profileForm'].patchValue({
      phone: '3419998877'
    });
    component['profileForm'].markAsDirty();

    component['onSubmit']();

    expect(component['errorMessage']()).toContain('Los datos provistos son inválidos');
    expect(component['isSaving']()).toBe(false);
  });

  it('should reset form back to original profile values', () => {
    component['profileForm'].patchValue({
      phone: '0000000000'
    });
    component['profileForm'].markAsDirty();

    component['resetForm']();

    expect(component['profileForm'].value.phone).toBe('3415551010');
    expect(component['profileForm'].pristine).toBe(true);
  });

  it('should load client access pass and open/close QR modal', () => {
    expect(mockAccessPassService.getMyPass).toHaveBeenCalled();
    expect(component['accessPass']()).toEqual(mockPass);

    component['openQrModal']();
    expect(component['isQrModalOpen']()).toBe(true);

    component['closeQrModal']();
    expect(component['isQrModalOpen']()).toBe(false);
  });
});

