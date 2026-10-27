import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UpdateProfileRequest, UserProfile } from './profile.models';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/users/me`;

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(this.baseUrl);
  }

  updateProfile(data: UpdateProfileRequest): Observable<UserProfile> {
    return this.http.patch<UserProfile>(this.baseUrl, data);
  }
}
