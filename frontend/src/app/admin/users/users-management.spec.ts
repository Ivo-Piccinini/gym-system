import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { UsersManagement } from './users-management';
import { AdminUserService } from './admin-user.service';
import { AuthService } from '../../auth/auth.service';
import { AdminUser } from './admin-users.models';
import { AccessPassService } from '../../access/access-pass.service';
import { AccessPass } from '../../access/access-pass.models';

describe('UsersManagement Component', () => {
  let component: UsersManagement;
  let fixture: ComponentFixture<UsersManagement>;
  let mockAdminUserService: any;
  let mockAuthService: any;
  let mockAccessPassService: any;

  const mockPass: AccessPass = {
    publicId: 'pass-uuid-1',
    userPublicId: 'uuid-1',
    clientName: 'Juan Perez',
    clientDni: '10000001',
    clientEmail: 'juan@gym.com',
    planName: 'Plan Trimestral',
    accessCode: 'GYM-PASS-10000001-XYZ1',
    qrData: '{"code":"GYM-PASS-10000001-XYZ1"}',
    qrImageBase64: 'data:image/png;base64,...',
    generatedBy: 'admin',
    generatedAt: '2026-11-14T19:00:00',
    validUntil: '2027-02-12',
    active: true
  };

  const mockUsers: AdminUser[] = [
    {
      publicId: 'uuid-1',
      username: '10000001',
      firstName: 'Juan',
      lastName: 'Perez',
      email: 'juan@gym.com',
      dni: '10000001',
      phone: '3415551111',
      birthDate: '1990-01-01',
      role: 'ROLE_CLIENT',
      enabled: true,
      hasActiveMembership: true
    },
    {
      publicId: 'uuid-2',
      username: 'prof.martin',
      firstName: 'Martin',
      lastName: 'Gomez',
      email: 'martin@gym.com',
      dni: '20000002',
      phone: '3415552222',
      birthDate: '1985-05-15',
      role: 'ROLE_PROFESSOR',
      enabled: false
    },
    {
      publicId: 'uuid-3',
      username: 'admin',
      firstName: 'Super',
      lastName: 'Admin',
      email: 'admin@gym.com',
      dni: '30000003',
      phone: null,
      birthDate: '1980-10-10',
      role: 'ROLE_ADMIN',
      enabled: true
    }
  ];

  beforeEach(async () => {
    mockAdminUserService = {
      getUsers: vi.fn().mockReturnValue(of(mockUsers)),
      createUser: vi.fn(),
      updateUser: vi.fn(),
      toggleUserStatus: vi.fn()
    };

    mockAuthService = {
      isAdmin: vi.fn().mockReturnValue(true),
      logout: vi.fn()
    };

    mockAccessPassService = {
      getPassByUser: vi.fn().mockReturnValue(of(mockPass)),
      generatePass: vi.fn().mockReturnValue(of(mockPass))
    };

    await TestBed.configureTestingModule({
      imports: [UsersManagement],
      providers: [
        provideRouter([]),
        { provide: AdminUserService, useValue: mockAdminUserService },
        { provide: AuthService, useValue: mockAuthService },
        { provide: AccessPassService, useValue: mockAccessPassService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UsersManagement);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load initial user list', () => {
    expect(component).toBeTruthy();
    expect(mockAdminUserService.getUsers).toHaveBeenCalled();
    expect((component as any).users().length).toBe(3);
    expect((component as any).totalCount()).toBe(3);
    expect((component as any).clientCount()).toBe(1);
    expect((component as any).professorCount()).toBe(1);
    expect((component as any).activeCount()).toBe(2);
    expect((component as any).inactiveCount()).toBe(1);
  });

  it('should filter users by search term', () => {
    (component as any).searchTerm.set('martin');
    fixture.detectChanges();

    const filtered = (component as any).filteredUsers();
    expect(filtered.length).toBe(1);
    expect(filtered[0].firstName).toBe('Martin');
  });

  it('should filter users by role and status', () => {
    (component as any).setRoleFilter('ROLE_CLIENT');
    fixture.detectChanges();
    expect((component as any).filteredUsers().length).toBe(1);

    (component as any).setRoleFilter('ALL');
    (component as any).setStatusFilter('INACTIVE');
    fixture.detectChanges();
    expect((component as any).filteredUsers().length).toBe(1);
    expect((component as any).filteredUsers()[0].username).toBe('prof.martin');
  });

  it('should open create modal with empty form and submit valid user', () => {
    (component as any).openCreateModal();
    expect((component as any).isUserModalOpen()).toBe(true);
    expect((component as any).editingUser()).toBeNull();

    (component as any).userForm.setValue({
      firstName: 'Lionel',
      lastName: 'Messi',
      dni: '10000010',
      email: 'leo@gym.com',
      phone: '3419998877',
      birthDate: '1987-06-24',
      role: 'ROLE_CLIENT'
    });

    const newUser: AdminUser = {
      publicId: 'uuid-4',
      username: '10000010',
      firstName: 'Lionel',
      lastName: 'Messi',
      dni: '10000010',
      email: 'leo@gym.com',
      phone: '3419998877',
      birthDate: '1987-06-24',
      role: 'ROLE_CLIENT',
      enabled: true
    };

    mockAdminUserService.createUser.mockReturnValue(of(newUser));

    (component as any).saveUser();

    expect(mockAdminUserService.createUser).toHaveBeenCalled();
    expect((component as any).isUserModalOpen()).toBe(false);
    expect((component as any).successMessage()).toContain('Lionel Messi');
  });

  it('should open edit modal with user data and update user', () => {
    const userToEdit = mockUsers[0];
    (component as any).openEditModal(userToEdit);

    expect((component as any).isUserModalOpen()).toBe(true);
    expect((component as any).editingUser()).toBe(userToEdit);
    expect((component as any).userForm.value.firstName).toBe('Juan');

    (component as any).userForm.patchValue({
      firstName: 'Juan Carlos'
    });

    const updatedUser = { ...userToEdit, firstName: 'Juan Carlos' };
    mockAdminUserService.updateUser.mockReturnValue(of(updatedUser));

    (component as any).saveUser();

    expect(mockAdminUserService.updateUser).toHaveBeenCalledWith('10000001', expect.objectContaining({
      firstName: 'Juan Carlos'
    }));
    expect((component as any).isUserModalOpen()).toBe(false);
  });

  it('should toggle user status via confirmation modal', () => {
    const user = mockUsers[0];
    (component as any).promptToggleStatus(user);

    expect((component as any).isConfirmModalOpen()).toBe(true);
    expect((component as any).userToToggle()).toBe(user);

    mockAdminUserService.toggleUserStatus.mockReturnValue(of(undefined));

    (component as any).confirmToggleStatus();

    expect(mockAdminUserService.toggleUserStatus).toHaveBeenCalledWith(user.publicId, false);
    expect((component as any).isConfirmModalOpen()).toBe(false);

   
    const updated = (component as any).users().find((u: AdminUser) => u.publicId === user.publicId);
    expect(updated?.enabled).toBe(false);
  });

  it('should handle error when toggle user status fails', () => {
    const user = mockUsers[0];
    (component as any).promptToggleStatus(user);

    mockAdminUserService.toggleUserStatus.mockReturnValue(throwError(() => ({
      error: { message: 'Error de servidor al modificar estado' }
    })));

    (component as any).confirmToggleStatus();

    expect((component as any).errorMessage()).toBe('Error de servidor al modificar estado');
    expect((component as any).isConfirmModalOpen()).toBe(false);
  });

  it('should open QR modal when pass exists for user', () => {
    const user = mockUsers[0]; // ROLE_CLIENT, enabled
    mockAccessPassService.getPassByUser.mockReturnValue(of(mockPass));

    (component as any).openQrModal(user);

    expect(mockAccessPassService.getPassByUser).toHaveBeenCalledWith(user.publicId);
    expect((component as any).isQrModalOpen()).toBe(true);
    expect((component as any).selectedUserPass()).toEqual(mockPass);
  });

  it('should generate QR pass when user has no existing pass (404)', () => {
    const user = mockUsers[0];
    mockAccessPassService.getPassByUser.mockReturnValue(throwError(() => ({ status: 404 })));
    mockAccessPassService.generatePass.mockReturnValue(of(mockPass));

    (component as any).openQrModal(user);

    expect(mockAccessPassService.getPassByUser).toHaveBeenCalledWith(user.publicId);
    expect(mockAccessPassService.generatePass).toHaveBeenCalledWith(user.publicId);
    expect((component as any).isQrModalOpen()).toBe(true);
    expect((component as any).selectedUserPass()).toEqual(mockPass);
  });

  it('should show error when opening QR for non-client or suspended user', () => {
    const adminUser = mockUsers[2];
    (component as any).openQrModal(adminUser);
    expect((component as any).errorMessage()).toContain('Solo los socios pueden poseer pases');

    const suspendedUser = mockUsers[1];
    (component as any).openQrModal(suspendedUser);
    expect((component as any).errorMessage()).toContain('usuario suspendido');

    const clientWithoutMembership: AdminUser = {
      ...mockUsers[0],
      hasActiveMembership: false
    };
    (component as any).openQrModal(clientWithoutMembership);
    expect((component as any).errorMessage()).toContain('no posee una membresía activa');
  });

  it('should regenerate QR pass', () => {
    const user = mockUsers[0];
    (component as any).activeQrUser.set(user);
    mockAccessPassService.generatePass.mockReturnValue(of({ ...mockPass, accessCode: 'NEW-CODE' }));

    (component as any).regenerateQrPass();

    expect(mockAccessPassService.generatePass).toHaveBeenCalledWith(user.publicId);
    expect((component as any).selectedUserPass()?.accessCode).toBe('NEW-CODE');
  });

  it('should close QR modal', () => {
    (component as any).isQrModalOpen.set(true);
    (component as any).selectedUserPass.set(mockPass);

    (component as any).closeQrModal();

    expect((component as any).isQrModalOpen()).toBe(false);
    expect((component as any).selectedUserPass()).toBeNull();
  });
});

