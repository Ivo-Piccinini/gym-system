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
  private readonly baseUrl = `${environment.apiUrl}/payments`;

  /**
   * Registra un pago de una suscripción del cliente autenticado (ROLE_CLIENT).
   * Tarjeta: el back aprueba y activa la suscripción de inmediato.
   * Efectivo: el back deja el pago y la suscripción en PENDING hasta la validación del admin.
   */
  createPayment(request: PaymentRequest): Observable<Payment> {
    return this.http.post<Payment>(this.baseUrl, request);
  }

  /** Historial de pagos del cliente autenticado (ROLE_CLIENT). */
  getMyPayments(): Observable<Payment[]> {
    return this.http.get<Payment[]>(`${this.baseUrl}/my-payments`);
  }

  /** Historial global de pagos (ROLE_ADMIN). */
  getAllPayments(): Observable<Payment[]> {
    return this.http.get<Payment[]>(this.baseUrl);
  }

  /**
   * Valida y aprueba un pago en efectivo recibido en recepción (ROLE_ADMIN).
   * El back pasa el pago a APPROVED y activa la membresía del socio.
   */
  validateCashPayment(publicId: string): Observable<Payment> {
    return this.http.patch<Payment>(`${this.baseUrl}/${publicId}/validate`, {});
  }

  /**
   * Rechaza o anula un pago en efectivo pendiente (ROLE_ADMIN).
   */
  rejectCashPayment(publicId: string): Observable<Payment> {
    return this.http.patch<Payment>(`${this.baseUrl}/${publicId}/reject`, {});
  }
}
