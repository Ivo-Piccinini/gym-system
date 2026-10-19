export interface AuthRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  message: string;
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
}
