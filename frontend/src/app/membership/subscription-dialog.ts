import { Component, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';
import { addDays, toIsoDate } from '../classes/date.utils';
import {
  MembershipPlan,
  PAYMENT_METHOD_LABELS,
  Payment,
  PaymentMethod,
  SUBSCRIPTION_STATUS_LABELS,
  Subscription
} from './membership.models';
import { durationLabel, formatDate, formatPrice } from './membership.utils';
import { PaymentService } from './payment.service';
import { PaymentSimulation } from './payment-simulation';
import { SubscriptionService } from './subscription.service';

type DialogView = 'confirm' | 'payment' | 'success' | 'conflict';

export const CONFLICT_MESSAGE =
  'Ya tenés una membresía activa. Vas a poder contratar un nuevo plan cuando finalice la actual.';

@Component({
  selector: 'app-subscription-dialog',
  standalone: true,
  imports: [PaymentSimulation],
  templateUrl: './subscription-dialog.html',
  styleUrls: ['./subscription-dialog.css'],
  host: { '(document:keydown.escape)': 'close()' }
})
export class SubscriptionDialog implements OnInit {
  private readonly subscriptionService = inject(SubscriptionService);
  private readonly paymentService = inject(PaymentService);
  private readonly authService = inject(AuthService);

  readonly plan = input.required<MembershipPlan>();
  /** Si viene una suscripción PENDING, el modal arranca directo en el paso de pago. */
  readonly pendingSubscription = input<Subscription | null>(null);

  /** Se emite al cerrar. true = hay datos desactualizados y conviene refrescar la pantalla. */
  readonly closed = output<boolean>();
  /** Se emite apenas el back confirma la suscripción (queda PENDING hasta que se paga). */
  readonly subscribed = output<Subscription>();
  /** Se emite cuando el back registra el pago (la suscripción pasa a ACTIVE). */
  readonly paid = output<Payment>();

  protected readonly view = signal<DialogView>('confirm');
  protected readonly isSubmitting = signal<boolean>(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly result = signal<Subscription | null>(null);

  protected readonly isPaying = signal<boolean>(false);
  protected readonly paymentError = signal<string | null>(null);
  protected readonly payment = signal<Payment | null>(null);

  private needsRefresh = false;

  // El back arranca la suscripción hoy; la fecha final es solo una estimación hasta que él responda.
  protected readonly startDate = toIsoDate(new Date());
  protected readonly estimatedEndDate = computed(() =>
    toIsoDate(addDays(new Date(), this.plan().durationDays))
  );

  protected readonly conflictMessage = CONFLICT_MESSAGE;

  ngOnInit(): void {
    const pending = this.pendingSubscription();
    if (pending) {
      this.result.set(pending);
      this.view.set('payment');
    }
  }

  // --- paso 1: suscripción ---
  protected confirm(): void {
    if (this.isSubmitting()) {
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.subscriptionService.subscribe(this.plan().publicId).subscribe({
      next: (subscription) => {
        this.isSubmitting.set(false);
        // Desde acá hay una suscripción PENDING: aunque cierre sin pagar, la pantalla debe refrescarse.
        this.needsRefresh = true;
        this.result.set(subscription);
        this.view.set('payment');
        this.subscribed.emit(subscription);
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        this.handleError(err);
      }
    });
  }

  // --- paso 2: pago ---
  protected pay(method: PaymentMethod): void {
    const subscription = this.result();
    if (!subscription || this.isPaying()) {
      return;
    }

    this.isPaying.set(true);
    this.paymentError.set(null);

    this.paymentService
      .createPayment({
        subscriptionId: subscription.publicId,
        amount: this.plan().price,
        paymentDate: toIsoDate(new Date()),
        method
      })
      .subscribe({
        next: (payment) => {
          this.isPaying.set(false);
          this.needsRefresh = true;
          this.payment.set(payment);
          this.view.set('success');
          this.paid.emit(payment);
        },
        error: (err: HttpErrorResponse) => {
          this.isPaying.set(false);
          this.handlePaymentError(err);
        }
      });
  }

  protected close(): void {
    // No se puede cerrar mientras la request está en vuelo: se perdería el resultado.
    if (this.isSubmitting() || this.isPaying()) {
      return;
    }
    this.closed.emit(this.needsRefresh);
  }

  private handleError(err: HttpErrorResponse): void {
    switch (err.status) {
      case 401:
        this.authService.logout();
        break;
      case 409:
        // Ya tiene una membresía activa: la pantalla de planes debe volver a consultarlo.
        this.needsRefresh = true;
        this.view.set('conflict');
        break;
      case 404:
        this.needsRefresh = true;
        this.errorMessage.set('Este plan ya no está disponible. Cerrá esta ventana para ver los planes actualizados.');
        break;
      case 403:
        this.errorMessage.set('Tu cuenta no tiene permisos para contratar una membresía.');
        break;
      default:
        this.errorMessage.set('No pudimos registrar la suscripción. Intentá nuevamente en unos minutos.');
    }
  }

  private handlePaymentError(err: HttpErrorResponse): void {
    switch (err.status) {
      case 401:
        this.authService.logout();
        break;
      case 400:
        this.paymentError.set('No pudimos registrar el pago. Revisá los datos e intentá nuevamente.');
        break;
      case 404:
        this.needsRefresh = true;
        this.paymentError.set('No encontramos tu suscripción. Cerrá esta ventana para ver tus datos actualizados.');
        break;
      case 403:
        this.paymentError.set('Tu cuenta no tiene permisos para registrar pagos.');
        break;
      default:
        this.paymentError.set('No pudimos registrar el pago. Intentá nuevamente en unos minutos.');
    }
  }

  // --- helpers de vista ---
  protected readonly formatPrice = formatPrice;
  protected readonly formatDate = formatDate;
  protected readonly durationLabel = durationLabel;

  protected readonly activeLabel = SUBSCRIPTION_STATUS_LABELS.ACTIVE;

  protected methodLabel(payment: Payment): string {
    return PAYMENT_METHOD_LABELS[payment.method] ?? payment.method;
  }
}