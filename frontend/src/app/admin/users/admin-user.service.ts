import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AdminUser, CreateUserRequest, UpdateUserRequest } from './admin-users.models';

@Injectable({
  providedIn: 'root'
})
export class AdminUserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/users`;

  getUsers(enabled?: boolean, role?: string): Observable<AdminUser[]> {
    let params = new HttpParams();
    if (enabled !== undefined) {
      params = params.set('enabled', enabled.toString());
    }
    if (role) {
      params = params.set('role', role);
    }
    return this.http.get<AdminUser[]>(this.baseUrl, { params });
  }

  getUserByUsername(username: string): Observable<AdminUser> {
    return this.http.get<AdminUser>(`${this.baseUrl}/${encodeURIComponent(username)}`);
  }

  createUser(request: CreateUserRequest): Observable<AdminUser> {
    return this.http.post<AdminUser>(`${this.baseUrl}/register`, request);
  }

  updateUser(username: string, request: UpdateUserRequest): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.baseUrl}/${encodeURIComponent(username)}`, request);
  }

  toggleUserStatus(publicId: string, enabled: boolean): Observable<void> {
    const params = new HttpParams().set('enabled', enabled.toString());
    return this.http.patch<void>(`${this.baseUrl}/${encodeURIComponent(publicId)}/status`, null, { params });
  }

  deleteUser(username: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${encodeURIComponent(username)}`);
  }
}
