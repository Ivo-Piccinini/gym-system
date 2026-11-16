import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, PasswordChangeRequest, UsernameChangeRequest } from './auth.models';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly http = inject(HttpClient);

  changePassword(request: PasswordChangeRequest): Observable<void> {
    return this.http.patch<void>(`${environment.apiUrl}/users/change-password`, request);
  }

  changeUsername(request: UsernameChangeRequest): Observable<AuthResponse> {
    return this.http.patch<AuthResponse>(`${environment.apiUrl}/users/change-username`, request);
  }
}

