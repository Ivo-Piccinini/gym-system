import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuditLog } from './audit.models';

@Injectable({
  providedIn: 'root'
})
export class AuditService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/audit/logs`;

  /**
   * Obtiene la bitácora completa de eventos de auditoría forense (ROLE_ADMIN).
   */
  getAuditLogs(): Observable<AuditLog[]> {
    return this.http.get<AuditLog[]>(this.baseUrl);
  }
}
