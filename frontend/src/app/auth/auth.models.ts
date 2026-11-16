export interface AuthRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  message: string;
  requiresPasswordChange?: boolean;
}

export interface DecodedToken {
  sub: string;
  authorities?: string[];
  exp: number;
  iat?: number;
}

export interface UserSession {
  username: string;
  roles: string[];
  token: string;
  requiresPasswordChange?: boolean;
}

export interface PasswordChangeRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface UsernameChangeRequest {
  newUsername: string;
}
