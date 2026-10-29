import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminUserService } from './admin-user.service';
import { AuthService } from '../../auth/auth.service';
import { AdminUser, CreateUserRequest, UpdateUserRequest, UserRole } from './admin-users.models';

@Component({
  selector: 'app-users-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './users-management.html',
  styleUrls: ['./users-management.css']
})
export class UsersManagement implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly adminUserService = inject(AdminUserService);
  protected readonly authService = inject(AuthService);

  protected readonly users = signal<AdminUser[]>([]);
  protected readonly isLoading = signal<boolean>(true);
  protected readonly isSaving = signal<boolean>(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  protected readonly searchTerm = signal<string>('');
  protected readonly selectedRole = signal<'ALL' | UserRole>('ALL');
  protected readonly selectedStatus = signal<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');

  protected readonly isUserModalOpen = signal<boolean>(false);
  protected readonly editingUser = signal<AdminUser | null>(null);

  protected readonly isConfirmModalOpen = signal<boolean>(false);
  protected readonly userToToggle = signal<AdminUser | null>(null);

  protected readonly userForm = this.fb.group({
    firstName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]],
    lastName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
    dni: ['', [Validators.required, Validators.pattern(/^[0-9]{8}$/)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.pattern(/^[0-9+\-\s]{7,15}$/)]],
    birthDate: ['', [Validators.required]],
    role: ['ROLE_CLIENT' as UserRole, [Validators.required]]
  });

  protected readonly totalCount = computed(() => this.users().length);
  protected readonly clientCount = computed(() => this.users().filter((u) => u.role === 'ROLE_CLIENT').length);
  protected readonly professorCount = computed(() => this.users().filter((u) => u.role === 'ROLE_PROFESSOR').length);
  protected readonly activeCount = computed(() => this.users().filter((u) => u.enabled).length);
  protected readonly inactiveCount = computed(() => this.users().filter((u) => !u.enabled).length);

  protected readonly filteredUsers = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const roleFilter = this.selectedRole();
    const statusFilter = this.selectedStatus();

    return this.users().filter((user) => {
      if (roleFilter !== 'ALL' && user.role !== roleFilter) {
        return false;
      }

      if (statusFilter === 'ACTIVE' && !user.enabled) {
        return false;
      }
      if (statusFilter === 'INACTIVE' && user.enabled) {
        return false;
      }

      if (!term) {
        return true;
      }

      const fullName = `${user.firstName} ${user.lastName}`.toLowerCase();
      const username = (user.username ?? '').toLowerCase();
      const email = user.email.toLowerCase();
      const dni = user.dni.toLowerCase();

      return (
        fullName.includes(term) ||
        username.includes(term) ||
        email.includes(term) ||
        dni.includes(term)
      );
    });
  });

  ngOnInit(): void {
    this.loadUsers();
  }

  protected loadUsers(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.adminUserService.getUsers().subscribe({
      next: (data) => {
        this.users.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 401) {
          this.authService.logout();
        } else {
          this.errorMessage.set('Error al cargar la lista de usuarios desde el servidor.');
        }
      }
    });
  }

  protected openCreateModal(): void {
    this.editingUser.set(null);
    this.userForm.reset({
      firstName: '',
      lastName: '',
      dni: '',
      email: '',
      phone: '',
      birthDate: '',
      role: 'ROLE_CLIENT'
    });
    this.userForm.get('dni')?.enable();
    this.errorMessage.set(null);
    this.isUserModalOpen.set(true);
  }

  protected openEditModal(user: AdminUser): void {
    this.editingUser.set(user);
    this.userForm.reset({
      firstName: user.firstName,
      lastName: user.lastName,
      dni: user.dni,
      email: user.email,
      phone: user.phone ?? '',
      birthDate: user.birthDate ?? '',
      role: user.role
    });
    this.userForm.get('dni')?.enable();
    this.errorMessage.set(null);
    this.isUserModalOpen.set(true);
  }

  protected closeUserModal(): void {
    this.isUserModalOpen.set(false);
    this.editingUser.set(null);
  }

  protected saveUser(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    const formVal = this.userForm.getRawValue();
    this.isSaving.set(true);
    this.errorMessage.set(null);

    const currentUser = this.editingUser();

    if (currentUser) {
      const updateReq: UpdateUserRequest = {
        publicId: currentUser.publicId,
        firstName: formVal.firstName!,
        lastName: formVal.lastName!,
        dni: formVal.dni!,
        email: formVal.email!,
        phone: formVal.phone || null,
        birthDate: formVal.birthDate || null,
        role: formVal.role!
      };

      const usernameTarget = currentUser.username || currentUser.dni;

      this.adminUserService.updateUser(usernameTarget, updateReq).subscribe({
        next: (updated) => {
          this.isSaving.set(false);
          this.closeUserModal();
          this.showSuccess(`Usuario "${updated.firstName} ${updated.lastName}" actualizado con éxito.`);
          this.loadUsers();
        },
        error: (err) => {
          this.isSaving.set(false);
          this.handleFormError(err, 'actualizar');
        }
      });
    } else {
      const createReq: CreateUserRequest = {
        firstName: formVal.firstName!,
        lastName: formVal.lastName!,
        dni: formVal.dni!,
        email: formVal.email!,
        phone: formVal.phone || null,
        birthDate: formVal.birthDate || null,
        role: formVal.role!
      };

      this.adminUserService.createUser(createReq).subscribe({
        next: (created) => {
          this.isSaving.set(false);
          this.closeUserModal();
          this.showSuccess(`Usuario "${created.firstName} ${created.lastName}" registrado correctamente.`);
          this.loadUsers();
        },
        error: (err) => {
          this.isSaving.set(false);
          this.handleFormError(err, 'registrar');
        }
      });
    }
  }

  protected promptToggleStatus(user: AdminUser): void {
    this.userToToggle.set(user);
    this.isConfirmModalOpen.set(true);
  }

  protected cancelToggleStatus(): void {
    this.userToToggle.set(null);
    this.isConfirmModalOpen.set(false);
  }

  protected confirmToggleStatus(): void {
    const user = this.userToToggle();
    if (!user) return;

    const newStatus = !user.enabled;
    this.isSaving.set(true);

    this.adminUserService.toggleUserStatus(user.publicId, newStatus).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.cancelToggleStatus();
        const actionLabel = newStatus ? 'activada' : 'desactivada';
        this.showSuccess(`Cuenta de "${user.firstName} ${user.lastName}" ${actionLabel} correctamente.`);
        this.users.update((current) =>
          current.map((u) => (u.publicId === user.publicId ? { ...u, enabled: newStatus } : u))
        );
      },
      error: (err) => {
        this.isSaving.set(false);
        this.cancelToggleStatus();
        this.errorMessage.set(
          err.error?.message || 'No fue posible modificar el estado de la cuenta. Intente nuevamente.'
        );
      }
    });
  }

  protected updateSearch(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.searchTerm.set(target.value);
  }

  protected setRoleFilter(role: 'ALL' | UserRole): void {
    this.selectedRole.set(role);
  }

  protected setStatusFilter(status: 'ALL' | 'ACTIVE' | 'INACTIVE'): void {
    this.selectedStatus.set(status);
  }

  private showSuccess(msg: string): void {
    this.successMessage.set(msg);
    setTimeout(() => {
      this.successMessage.set(null);
    }, 4500);
  }

  private handleFormError(err: any, action: string): void {
    if (err.status === 409 || (err.error && typeof err.error.message === 'string' && err.error.message.includes('existe'))) {
      this.errorMessage.set(err.error?.message || 'Ya existe un usuario con este Email o DNI.');
    } else if (err.error?.message) {
      this.errorMessage.set(err.error.message);
    } else {
      this.errorMessage.set(`Ocurrió un error inesperado al ${action} el usuario.`);
    }
  }

  protected getRoleBadgeClass(role: UserRole): string {
    switch (role) {
      case 'ROLE_ADMIN':
        return 'badge-admin';
      case 'ROLE_PROFESSOR':
        return 'badge-professor';
      case 'ROLE_CLIENT':
      default:
        return 'badge-client';
    }
  }

  protected getRoleLabel(role: UserRole): string {
    switch (role) {
      case 'ROLE_ADMIN':
        return 'Administrador';
      case 'ROLE_PROFESSOR':
        return 'Profesor';
      case 'ROLE_CLIENT':
        return 'Socio';
      default:
        return role;
    }
  }
}
