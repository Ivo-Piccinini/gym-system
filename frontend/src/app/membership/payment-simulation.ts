import { Component, DestroyRef, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
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

type CardBrand = 'VISA' | 'MASTERCARD' | 'AMEX';

const BRAND_LABELS: Record<CardBrand, string> = {
  VISA: 'Visa',
  MASTERCARD: 'Mastercard',
  AMEX: 'American Express'
};

const onlyDigits = (value: string): string => value.replace(/\D/g, '');

function detectBrand(digits: string): CardBrand | null {
  if (/^4/.test(digits)) {
    return 'VISA';
  }
  if (/^(5[1-5]|2(2[2-9][1-9]|2[3-9]|[3-6]|7[01]|720))/.test(digits)) {
    return 'MASTERCARD';
  }
  if (/^3[47]/.test(digits)) {
    return 'AMEX';
  }
  return null;
}

/** Algoritmo de Luhn: detecta números de tarjeta mal tipeados. */
function passesLuhn(digits: string): boolean {
  let sum = 0;
  let double = false;
  for (let i = digits.length - 1; i >= 0; i--) {
    let n = Number(digits[i]);
    if (double) {
      n *= 2;
      if (n > 9) {
        n -= 9;
      }
    }
    sum += n;
    double = !double;
  }
  return sum % 10 === 0;
}

function cardNumberValidator(control: AbstractControl): ValidationErrors | null {
  const digits = onlyDigits(control.value ?? '');
  if (!digits) {
    return { required: true };
  }
  const expected = detectBrand(digits) === 'AMEX' ? 15 : 16;
  if (digits.length !== expected) {
    return { length: true };
  }
  return passesLuhn(digits) ? null : { luhn: true };
}

function expiryValidator(control: AbstractControl): ValidationErrors | null {
  const value: string = control.value ?? '';
  if (!value) {
    return { required: true };
  }
  const match = /^(0[1-9]|1[0-2])\/(\d{2})$/.exec(value);
  if (!match) {
    return { format: true };
  }
  const month = Number(match[1]);
  const year = 2000 + Number(match[2]);
  const now = new Date();
  const currentMonthIndex = now.getFullYear() * 12 + now.getMonth() + 1;
  const expiryIndex = year * 12 + month;
  // La tarjeta vale hasta fin del mes de vencimiento.
  if (expiryIndex < currentMonthIndex) {
    return { expired: true };
  }
  if (expiryIndex - currentMonthIndex > 20 * 12) {
    return { format: true };
  }
  return null;
}

function cvvValidator(control: AbstractControl): ValidationErrors | null {
  const value: string = control.value ?? '';
  if (!value) {
    return { required: true };
  }
  const number = control.parent?.get('number')?.value ?? '';
  const expected = detectBrand(onlyDigits(number)) === 'AMEX' ? 4 : 3;
  return /^\d+$/.test(value) && value.length === expected ? null : { length: true };
}

/**
 * Paso de pago dentro del modal de suscripción. Es presentacional: no llama al back,
 * solo emite el método elegido y quien lo usa (SubscriptionDialog) registra el pago.
 * Con tarjeta se pide y valida el formulario de la tarjeta, pero esos datos nunca se emiten,
 * se envían ni se guardan: viven solo en este formulario mientras el modal está abierto.
 */
@Component({
  selector: 'app-payment-simulation',
  standalone: true,
  imports: [ReactiveFormsModule],
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
  protected readonly brand = signal<CardBrand | null>(null);

  protected readonly cardForm = new FormGroup({
    holder: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^(?=.*\S)[A-ZÁÉÍÓÚÑÜ'.\- ]{3,40}$/)]
    }),
    number: new FormControl('', { nonNullable: true, validators: [cardNumberValidator] }),
    expiry: new FormControl('', { nonNullable: true, validators: [expiryValidator] }),
    cvv: new FormControl('', { nonNullable: true, validators: [cvvValidator] })
  });

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

  constructor() {
    // El largo del CVV depende de la marca (Amex usa 4 dígitos), así que se revalida al cambiar el número.
    this.cardForm.controls.number.valueChanges
      .pipe(takeUntilDestroyed(inject(DestroyRef)))
      .subscribe((value) => {
        this.brand.set(detectBrand(onlyDigits(value)));
        this.cardForm.controls.cvv.updateValueAndValidity({ emitEvent: false });
      });
  }

  protected select(method: PaymentMethod): void {
    if (this.isSubmitting()) {
      return;
    }
    this.method.set(method);
  }

  protected canConfirm(): boolean {
    const method = this.method();
    if (!method || this.isSubmitting()) {
      return false;
    }
    return method !== 'TARJETA' || this.cardForm.valid;
  }

  protected confirm(): void {
    const method = this.method();
    if (!method || this.isSubmitting()) {
      return;
    }
    if (method === 'TARJETA' && this.cardForm.invalid) {
      this.cardForm.markAllAsTouched();
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

  // --- formato de los campos de la tarjeta ---
  protected onNumberInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = onlyDigits(input.value);
    const max = detectBrand(digits) === 'AMEX' ? 15 : 16;
    const trimmed = digits.slice(0, max);
    // Amex se agrupa 4-6-5; el resto 4-4-4-4.
    const formatted =
      detectBrand(trimmed) === 'AMEX'
        ? [trimmed.slice(0, 4), trimmed.slice(4, 10), trimmed.slice(10)].filter(Boolean).join(' ')
        : (trimmed.match(/.{1,4}/g) ?? []).join(' ');
    input.value = formatted;
    this.cardForm.controls.number.setValue(formatted);
  }

  protected onExpiryInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = onlyDigits(input.value).slice(0, 4);
    const formatted = digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits;
    input.value = formatted;
    this.cardForm.controls.expiry.setValue(formatted);
  }

  protected onCvvInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = onlyDigits(input.value).slice(0, 4);
    input.value = digits;
    this.cardForm.controls.cvv.setValue(digits);
  }

  protected onHolderInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const upper = input.value.toUpperCase();
    input.value = upper;
    this.cardForm.controls.holder.setValue(upper);
  }

  protected showError(name: 'holder' | 'number' | 'expiry' | 'cvv'): boolean {
    const control = this.cardForm.controls[name];
    return control.invalid && (control.touched || control.dirty);
  }

  protected numberError(): string {
    const errors = this.cardForm.controls.number.errors;
    if (errors?.['luhn']) {
      return 'El número de tarjeta no es válido. Revisalo.';
    }
    if (errors?.['length']) {
      return `El número debe tener ${this.brand() === 'AMEX' ? 15 : 16} dígitos.`;
    }
    return 'Ingresá el número de tu tarjeta.';
  }

  protected expiryError(): string {
    const errors = this.cardForm.controls.expiry.errors;
    if (errors?.['expired']) {
      return 'La tarjeta está vencida.';
    }
    if (errors?.['format']) {
      return 'Usá el formato MM/AA con una fecha válida.';
    }
    return 'Ingresá el vencimiento.';
  }

  protected cvvLength(): number {
    return this.brand() === 'AMEX' ? 4 : 3;
  }

  // --- helpers de vista ---
  protected readonly formatPrice = formatPrice;
  protected readonly durationLabel = durationLabel;

  protected methodLabel(method: PaymentMethod): string {
    return PAYMENT_METHOD_LABELS[method];
  }

  protected brandLabel(brand: CardBrand): string {
    return BRAND_LABELS[brand];
  }
}