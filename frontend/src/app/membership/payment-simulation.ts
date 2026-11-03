import { Component, input, output, signal } from '@angular/core';
import {
  MembershipPlan,
  PAYMENT_METHOD_LABELS,
  PaymentMethod
} from './membership.models';
import { durationLabel, formatPrice } from './membership.utils';

interface MethodOption {
  value: PaymentMethod;
  label: string;
  hint: string;
}

/**
 * Paso de pago dentro del modal de suscripción. Es presentacional: no llama al back,
 * solo emite el método elegido y quien lo usa (SubscriptionDialog) registra el pago.
 */
@Component({
  selector: 'app-payment-simulation',
  standalone: true,
  templateUrl: './payment-simulation.html',
  styleUrls: ['./payment-simulation.css']
})
export class PaymentSimulation {
  readonly plan = input.required<MembershipPlan>();
  readonly isSubmitting = input<boolean>(false);
  readonly errorMessage = input<string | null>(null);

  /** Método elegido y confirmado por el cliente. */
  readonly confirmed = output<PaymentMethod>();
  /** El cliente decide pagar después (la suscripción queda pendiente). */
  readonly postponed = output<void>();

  protected readonly method = signal<PaymentMethod | null>(null);

  protected readonly options: MethodOption[] = [
    {
      value: 'EFECTIVO',
      label: PAYMENT_METHOD_LABELS.EFECTIVO,
      hint: 'Abonás en recepción'
    },
    {
      value: 'TARJETA',
      label: PAYMENT_METHOD_LABELS.TARJETA,
      hint: 'Débito o crédito'
    }
  ];

  protected select(method: PaymentMethod): void {
    if (this.isSubmitting()) {
      return;
    }
    this.method.set(method);
  }

  protected confirm(): void {
    const method = this.method();
    if (!method || this.isSubmitting()) {
      return;
    }
    this.confirmed.emit(method);
  }

  protected postpone(): void {
    if (this.isSubmitting()) {
      return;
    }
    this.postponed.emit();
  }

  // --- helpers de vista ---
  protected readonly formatPrice = formatPrice;
  protected readonly durationLabel = durationLabel;

  protected methodLabel(method: PaymentMethod): string {
    return PAYMENT_METHOD_LABELS[method];
  }
}