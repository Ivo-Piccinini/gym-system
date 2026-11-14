import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { MembershipPlan } from './membership.models';

/** Body de POST/PUT /api/memberships (MembershipRequestDTO). */
export interface MembershipPlanPayload {
  name: string;
  price: number;
  durationDays: number;
}

@Injectable({
  providedIn: 'root'
})
export class MembershipPlanService {
  private readonly http = inject(HttpClient);
  // environment.apiUrl ya incluye "/api". Si el back cambia la ruta, se cambia solo acá.
  private readonly baseUrl = `${environment.apiUrl}/memberships`;

  /** Lista los planes de membresía disponibles (ADMIN, PROFESSOR y CLIENT). */
  getPlans(): Observable<MembershipPlan[]> {
    return this.http.get<MembershipPlan[]>(this.baseUrl);
  }

  /** Alta de plan (solo ADMIN). El back la registra en la auditoría. */
  createPlan(payload: MembershipPlanPayload): Observable<MembershipPlan> {
    return this.http.post<MembershipPlan>(this.baseUrl, payload);
  }

  /** Modificación de un plan existente por su UUID (solo ADMIN). El back la registra en la auditoría. */
  updatePlan(publicId: string, payload: MembershipPlanPayload): Observable<MembershipPlan> {
    return this.http.put<MembershipPlan>(`${this.baseUrl}/${encodeURIComponent(publicId)}`, payload);
  }
}