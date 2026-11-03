import { Component, computed, inject, input, output, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';
import { addDays, toIsoDate } from '../classes/date.utils';
import {
  MembershipPlan,
  SUBSCRIPTION_STATUS_LABELS,
  Subscription
} from './membership.models';
import { durationLabel, formatDate, formatPrice } from './membership.utils';
import { SubscriptionService } from './subscription.service';

type DialogView = 'confirm' | 'success' | 'conflict';

export const CONFLICT_MESSAGE =
  'Ya tenés una membresía activa. Vas a poder contratar un nuevo plan cuando finalice la actual.';

@Component({
  selector: 'app-subscription-dialog',
  standalone: true,
  templateUrl: './subscription-dialog.html',
  styleUrls: ['./subscription-dialog.css'],
  host: { '(document:keydown.escape)': 'close()' }
})
export class SubscriptionDialog {
  private readonly subscriptionService = inject(SubscriptionService);
  private readonly authService = inject(AuthService);

  readonly plan = input.required<MembershipPlan>();

  /** Se emite al cerrar. true = hay datos desactualizados y conviene refrescar la pantalla. */
  readonly closed = output<boolean>();
  /** Se emite apenas el back confirma la suscripción. */
  readonly subscribed = output<Subscription>();

  protected readonly view = signal<DialogView>('confirm');
  protected readonly isSubmitting = signal<boolean>(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly result = signal<Subscription | null>(null);

  private needsRefresh = false;

  // El back arranca la suscripción hoy; la fecha final es solo una estimación hasta que él responda.
  protected readonly startDate = toIsoDate(new Date());
  protected readonly estimatedEndDate = computed(() =>
    toIsoDate(addDays(new Date(), this.plan().durationDays))
  );

  protected readonly conflictMessage = CONFLICT_MESSAGE;

  protected confirm(): void {
    if (this.isSubmitting()) {
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.subscriptionService.subscribe(this.plan().publicId).subscribe({
      next: (subscription) => {
        this.isSubmitting.set(false);
        this.needsRefresh = true;
        this.result.set(subscription);
        this.view.set('success');
        this.subscribed.emit(subscription);
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting.set(false);
        this.handleError(err);
      }
    });
  }

  protected close(): void {
    // No se puede cerrar mientras la request está en vuelo: se perdería el resultado.
    if (this.isSubmitting()) {
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

  // --- helpers de vista ---
  protected readonly formatPrice = formatPrice;
  protected readonly formatDate = formatDate;
  protected readonly durationLabel = durationLabel;

  protected statusLabel(subscription: Subscription): string {
    return SUBSCRIPTION_STATUS_LABELS[subscription.status] ?? subscription.status;
  }
}