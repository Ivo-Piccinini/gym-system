import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Payment, PaymentRequest } from './membership.models';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly http = inject(HttpClient);
  // environment.apiUrl ya incluye "/api". Si el back cambia la ruta, se cambia solo acá.
  private readonly baseUrl = `${environment.apiUrl}/payments`;

  /**
   * Registra un pago de una suscripción del cliente autenticado (ROLE_CLIENT).
   * El back deja la suscripción en ACTIVE al registrar el pago.
   * Errores esperables: 400 (datos inválidos), 404 (suscripción inexistente), 403 (sin permisos).
   */
  createPayment(request: PaymentRequest): Observable<Payment> {
    return this.http.post<Payment>(this.baseUrl, request);
  }

  /** Historial de pagos del cliente autenticado (ROLE_CLIENT). */
  getMyPayments(): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.baseUrl}/my-payments`);
  }
}