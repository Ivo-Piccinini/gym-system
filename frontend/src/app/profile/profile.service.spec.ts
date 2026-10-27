import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { ProfileService } from './profile.service';
import { environment } from '../../environments/environment';
import { UpdateProfileRequest, UserProfile } from './profile.models';

describe('ProfileService', () => {
  let service: ProfileService;
  let httpTesting: HttpTestingController;

  const mockProfile: UserProfile = {
    publicId: '3b29c910-482a-4a27-beaa-f5cf3d99432e',
    firstName: 'Carlos',
    lastName: 'Tevez',
    email: 'carlos@gym.com',
    dni: '30111222',
    phone: '3415556677',
    birthDate: '1984-02-05',
    role: 'ROLE_CLIENT'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ProfileService
      ]
    });

    service = TestBed.inject(ProfileService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get profile via GET /api/users/me', () => {
    service.getProfile().subscribe((profile) => {
      expect(profile).toEqual(mockProfile);
      expect(profile.firstName).toBe('Carlos');
      expect(profile.dni).toBe('30111222');
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/users/me`);
    expect(req.request.method).toBe('GET');
    req.flush(mockProfile);
  });

  it('should update profile via PATCH /api/users/me', () => {
    const patchData: UpdateProfileRequest = {
      phone: '3419990000',
      birthDay: '1984-02-10'
    };

    const updatedProfile: UserProfile = {
      ...mockProfile,
      phone: '3419990000',
      birthDate: '1984-02-10'
    };

    service.updateProfile(patchData).subscribe((profile) => {
      expect(profile.phone).toBe('3419990000');
      expect(profile.birthDate).toBe('1984-02-10');
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/users/me`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(patchData);
    req.flush(updatedProfile);
  });
});
