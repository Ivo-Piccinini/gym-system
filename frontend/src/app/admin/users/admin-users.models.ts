export type UserRole = 'ROLE_ADMIN' | 'ROLE_PROFESSOR' | 'ROLE_CLIENT';

export interface AdminUser {
  publicId: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  dni: string;
  phone: string | null;
  birthDate: string | null;
  role: UserRole;
  enabled: boolean;
}

export interface CreateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  dni: string;
  phone?: string | null;
  birthDate?: string | null;
  role: UserRole;
}

export interface UpdateUserRequest {
  publicId: string;
  firstName: string;
  lastName: string;
  email: string;
  dni: string;
  phone?: string | null;
  birthDate?: string | null;
  role: UserRole;
}

export interface UserFilters {
  searchTerm: string;
  role: 'ALL' | UserRole;
  status: 'ALL' | 'ACTIVE' | 'INACTIVE';
}
