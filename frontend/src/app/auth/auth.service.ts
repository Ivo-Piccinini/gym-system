import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthRequest, AuthResponse, DecodedToken, UserSession } from './auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly TOKEN_KEY = 'gym_auth_token';

  readonly currentUser = signal<UserSession | null>(this.getStoredSession());
  readonly isAuthenticated = computed(() => this.currentUser() !== null);
  readonly userRoles = computed(() => this.currentUser()?.roles ?? []);
  readonly username = computed(() => this.currentUser()?.username ?? '');

  readonly isAdmin = computed(() => this.hasRole('ROLE_ADMIN'));
  readonly isProfessor = computed(() => this.hasRole('ROLE_PROFESSOR'));
  readonly isClient = computed(() => this.hasRole('ROLE_CLIENT'));

  login(credentials: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, credentials).pipe(
      tap((response) => {
        this.setSession(response.token);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    const token = localStorage.getItem(this.TOKEN_KEY);
    if (!token) {
      return null;
    }

    const decoded = this.decodeToken(token);
    if (!decoded || this.isTokenExpired(decoded)) {
      this.logout();
      return null;
    }

    return token;
  }

  hasRole(role: string): boolean {
    return this.userRoles().includes(role);
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.some((role) => this.hasRole(role));
  }

  getDefaultRedirectUrl(): string {
    return '/perfil';
  }

  private setSession(token: string): void {
    const decoded = this.decodeToken(token);
    if (!decoded || this.isTokenExpired(decoded)) {
      throw new Error('Token inválido o expirado');
    }

    localStorage.setItem(this.TOKEN_KEY, token);

    const session: UserSession = {
      username: decoded.sub,
      roles: decoded.authorities ?? [],
      token
    };

    this.currentUser.set(session);
  }

  private getStoredSession(): UserSession | null {
    const token = localStorage.getItem(this.TOKEN_KEY);
    if (!token) {
      return null;
    }

    const decoded = this.decodeToken(token);
    if (!decoded || this.isTokenExpired(decoded)) {
      localStorage.removeItem(this.TOKEN_KEY);
      return null;
    }

    return {
      username: decoded.sub,
      roles: decoded.authorities ?? [],
      token
    };
  }

  private decodeToken(token: string): DecodedToken | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        return null;
      }
      const payloadBase64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const payloadJson = decodeURIComponent(
        atob(payloadBase64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(payloadJson) as DecodedToken;
    } catch {
      return null;
    }
  }

  private isTokenExpired(decoded: DecodedToken): boolean {
    if (!decoded.exp) {
      return false;
    }
    const currentTimestampSeconds = Math.floor(Date.now() / 1000);
    return decoded.exp < currentTimestampSeconds;
  }
}
