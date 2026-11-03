import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Subscription } from './membership.models';

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private readonly http = inject(HttpClient);
  // environment.apiUrl ya incluye "/api". Si el back cambia la ruta, se cambia solo acá.
  private readonly baseUrl = `${environment.apiUrl}/subscriptions`;

  /**
   * Contrata un plan para el cliente autenticado. El back calcula startDate y endDate.
   * @param planPublicId UUID del plan (MembershipPlan.publicId).
   * Errores esperables: 409 si ya tiene una membresía activa, 404 si el plan no existe.
   */
  subscribe(planPublicId: string): Observable<Subscription> {
    return this.http.post<Subscription>(`${this.baseUrl}/${planPublicId}`, null);
  }

  /** Suscripciones del cliente autenticado (activas, pendientes, vencidas, canceladas). */
  getMySubscriptions(): Observable<Subscription[]> {
    return this.http.get<Subscription[]>(`${this.baseUrl}/my-subscriptions`);
  }
}