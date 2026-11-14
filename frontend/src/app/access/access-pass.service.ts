import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AccessPass } from './access-pass.models';

@Injectable({
  providedIn: 'root'
})
export class AccessPassService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/access`;

  generatePass(userPublicId: string): Observable<AccessPass> {
    return this.http.post<AccessPass>(`${this.baseUrl}/generate/${userPublicId}`, {});
  }

  getPassByUser(userPublicId: string): Observable<AccessPass> {
    return this.http.get<AccessPass>(`${this.baseUrl}/user/${userPublicId}`);
  }

  getMyPass(): Observable<AccessPass> {
    return this.http.get<AccessPass>(`${this.baseUrl}/my-pass`);
  }
}
