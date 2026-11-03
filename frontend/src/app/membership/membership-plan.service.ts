import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { MembershipPlan } from './membership.models';

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
}