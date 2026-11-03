import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import {
  MembershipPlan,
  PAYMENT_METHOD_LABELS,
  Payment,
  PaymentMethod,
  Subscription
} from './membership.models';
import { MembershipPlanService } from './membership-plan.service';
import { formatPrice, formatShortDate } from './membership.utils';
import { PaymentService } from './payment.service';
import { SubscriptionService } from './subscription.service';

interface PaymentRow {
  key: string;
  receipt: string;
  dateLabel: string;
  planName: string;
  method: PaymentMethod;
  methodLabel: string;
  amountLabel: string;
}

@Component({
  selector: 'app-payment-history',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './payment-history.html',
  styleUrls: ['./payment-history.css']
})
export class PaymentHistory implements OnInit {
  private readonly paymentService = inject(PaymentService);
  private readonly subscriptionService = inject(SubscriptionService);
  private readonly planService = inject(MembershipPlanService);
  protected readonly authService = inject(AuthService);

  protected readonly rows = signal<PaymentRow[]>([]);
  protected readonly total = signal<number>(0);
  protected readonly isLoading = signal<boolean>(true);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly summary = computed(() => {
    const count = this.rows().length;
    const noun = count === 1 ? 'pago' : 'pagos';
    return `Registraste ${count} ${noun} por un total de ${formatPrice(this.total())}.`;
  });

  ngOnInit(): void {
    this.load();
  }

  protected retry(): void {
    this.load();
  }

  private load(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    // Los pagos son lo único imprescindible: si fallan suscripciones o planes, igual se muestra el historial.
    forkJoin({
      payments: this.paymentService.getMyPayments(),
      subscriptions: this.subscriptionService
        .getMySubscriptions()
        .pipe(catchError(() => of<Subscription[]>([]))),
      plans: this.planService.getPlans().pipe(catchError(() => of<MembershipPlan[]>([])))
    }).subscribe({
      next: ({ payments, subscriptions, plans }) => {
        this.rows.set(this.buildRows(payments, subscriptions, plans));
        this.total.set(payments.reduce((sum, p) => sum + p.amount, 0));
        this.isLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading.set(false);
        switch (err.status) {
          case 401:
            this.authService.logout();
            break;
          case 403:
            this.errorMessage.set('El historial de pagos está disponible solo para cuentas de cliente.');
            break;
          default:
            this.errorMessage.set(
              'No se pudo cargar el historial de pagos. Por favor, reintente más tarde.'
            );
        }
      }
    });
  }

  private buildRows(
    payments: Payment[],
    subscriptions: Subscription[],
    plans: MembershipPlan[]
  ): PaymentRow[] {
    // Más recientes primero; a igual fecha, el de id mayor.
    const sorted = [...payments].sort(
      (a, b) => b.paymentDate.localeCompare(a.paymentDate) || b.id - a.id
    );

    return sorted.map((payment) => {
      // Payment.subscriptionId es el id numérico de la suscripción, no el UUID.
      const subscription = subscriptions.find((s) => s.id === payment.subscriptionId);
      const plan = subscription ? plans.find((p) => p.id === subscription.planId) : undefined;

      return {
        key: payment.publicId ?? String(payment.id),
        receipt: this.receiptNumber(payment),
        dateLabel: formatShortDate(payment.paymentDate),
        planName: plan?.name ?? `Suscripción #${payment.subscriptionId}`,
        method: payment.method,
        methodLabel: PAYMENT_METHOD_LABELS[payment.method] ?? payment.method,
        amountLabel: formatPrice(payment.amount)
      };
    });
  }

  private receiptNumber(payment: Payment): string {
    return payment.publicId
      ? `#${payment.publicId.slice(0, 8).toUpperCase()}`
      : `#${String(payment.id).padStart(6, '0')}`;
  }
}