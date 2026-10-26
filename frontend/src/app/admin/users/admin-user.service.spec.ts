import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { AdminUserService } from './admin-user.service';
import { environment } from '../../../environments/environment';
import { AdminUser, CreateUserRequest, UpdateUserRequest } from './admin-users.models';

describe('AdminUserService', () => {
  let service: AdminUserService;
  let httpTesting: HttpTestingController;

  const mockUser: AdminUser = {
    publicId: '3b29c910-482a-4a27-beaa-f5cf3d99432e',
    username: 'carlos30',
    firstName: 'Carlos',
    lastName: 'Tevez',
    email: 'carlos@gym.com',
    dni: '30111222',
    phone: '3415556677',
    birthDate: '1984-02-05',
    role: 'ROLE_CLIENT',
    enabled: true
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        AdminUserService
      ]
    });

    service = TestBed.inject(AdminUserService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should list users with optional filters', () => {
    service.getUsers(true, 'ROLE_CLIENT').subscribe((users) => {
      expect(users.length).toBe(1);
      expect(users[0]).toEqual(mockUser);
    });

    const req = httpTesting.expectOne((request) => {
      return (
        request.url === `${environment.apiUrl}/users` &&
        request.params.get('enabled') === 'true' &&
        request.params.get('role') === 'ROLE_CLIENT'
      );
    });

    expect(req.request.method).toBe('GET');
    req.flush([mockUser]);
  });

  it('should create a new user via POST /api/users/register', () => {
    const createReq: CreateUserRequest = {
      firstName: 'Lionel',
      lastName: 'Messi',
      email: 'leo@gym.com',
      dni: '10000010',
      phone: '3419999999',
      birthDate: '1987-06-24',
      role: 'ROLE_CLIENT'
    };

    service.createUser(createReq).subscribe((created) => {
      expect(created.email).toBe('leo@gym.com');
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/users/register`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(createReq);
    req.flush({ ...mockUser, ...createReq });
  });

  it('should update an existing user via PUT /api/users/{username}', () => {
    const updateReq: UpdateUserRequest = {
      publicId: mockUser.publicId,
      firstName: 'Carlos',
      lastName: 'Alberto',
      email: 'carlos.alberto@gym.com',
      dni: '30111222',
      phone: '3410001111',
      birthDate: '1984-02-05',
      role: 'ROLE_PROFESSOR'
    };

    service.updateUser('carlos30', updateReq).subscribe((updated) => {
      expect(updated.lastName).toBe('Alberto');
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/users/carlos30`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateReq);
    req.flush({ ...mockUser, ...updateReq });
  });

  it('should toggle user status via PATCH /api/users/{publicId}/status', () => {
    service.toggleUserStatus(mockUser.publicId, false).subscribe();

    const req = httpTesting.expectOne(
      (r) =>
        r.url === `${environment.apiUrl}/users/${mockUser.publicId}/status` &&
        r.params.get('enabled') === 'false'
    );
    expect(req.request.method).toBe('PATCH');
    req.flush(null);
  });

  it('should delete a user via DELETE /api/users/{username}', () => {
    service.deleteUser('carlos30').subscribe();

    const req = httpTesting.expectOne(`${environment.apiUrl}/users/carlos30`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
