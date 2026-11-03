import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { MembershipPlan, Payment, Subscription } from './membership.models';
import {
  durationLabel,
  formatDate,
  formatPrice,
  isCurrentlyActive,
  isPendingPayment,
  pricePerDay,
  pricePerMonth
} from './membership.utils';
import { MembershipPlanService } from './membership-plan.service';
import { SubscriptionDialog } from './subscription-dialog';
import { SubscriptionService } from './subscription.service';

interface PlanCard {
  plan: MembershipPlan;
  priceLabel: string;
  durationLabel: string;
  perDayLabel: string;
  perMonthLabel: string;
  isBestValue: boolean;
  isCurrent: boolean;
  isPending: boolean;
}

@Component({
  selector: 'app-membership-plans',
  standalone: true,
  imports: [RouterLink, SubscriptionDialog],
  templateUrl: './membership-plans.html',
  styleUrls: ['./membership-plans.css']
})
export class MembershipPlans implements OnInit {
  private readonly planService = inject(MembershipPlanService);
  private readonly subscriptionService = inject(SubscriptionService);
  protected readonly authService = inject(AuthService);

  // --- estado ---
  protected readonly plans = signal<MembershipPlan[]>([]);
  protected readonly isLoading = signal<boolean>(true);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly subscriptions = signal<Subscription[]>([]);
  protected readonly isCheckingSubscriptions = signal<boolean>(true);

  protected readonly selectedPlan = signal<MembershipPlan | null>(null);
  /** Suscripción PENDING que se está pagando (null si el modal arranca desde la confirmación del plan). */
  protected readonly selectedPending = signal<Subscription | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  // --- derivados ---
  /** Membresía vigente del cliente (si tiene una, no puede contratar otra). */
  protected readonly activeSubscription = computed(
    () => this.subscriptions().find((s) => isCurrentlyActive(s)) ?? null
  );

  protected readonly activePlan = computed(() => {
    const active = this.activeSubscription();
    return active ? (this.plans().find((p) => p.id === active.planId) ?? null) : null;
  });

  /** Suscripción creada que todavía no se pagó: hay que completarla antes de contratar otro plan. */
  protected readonly pendingSubscription = computed(
    () => this.subscriptions().find((s) => isPendingPayment(s)) ?? null
  );

  protected readonly pendingPlan = computed(() => {
    const pending = this.pendingSubscription();
    return pending ? (this.plans().find((p) => p.id === pending.planId) ?? null) : null;
  });

  /** Hay una membresía vigente o un pago pendiente: no se puede contratar otro plan. */
  protected readonly isBlocked = computed(
    () => !!this.activeSubscription() || !!this.pendingSubscription()
  );

  /** Mientras se verifica no se habilita contratar; si la verificación falla, el 409 del back protege igual. */
  protected readonly canSubscribe = computed(
    () => !this.isCheckingSubscriptions() && !this.isBlocked()
  );

  protected readonly cards = computed<PlanCard[]>(() => {
    const plans = [...this.plans()].sort((a, b) => a.durationDays - b.durationDays);
    const bestValueId = this.bestValuePlanId(plans);
    const currentId = this.activePlan()?.id;
    const pendingId = this.pendingPlan()?.id;

    return plans.map((plan) => ({
      plan,
      priceLabel: formatPrice(plan.price),
      durationLabel: durationLabel(plan.durationDays),
      perDayLabel: formatPrice(pricePerDay(plan)),
      perMonthLabel: formatPrice(pricePerMonth(plan)),
      isBestValue: plan.id === bestValueId,
      isCurrent: plan.id === currentId,
      isPending: plan.id === pendingId && plan.id !== currentId
    }));
  });

  ngOnInit(): void {
    this.fetchPlans(true);
    this.fetchSubscriptions();
  }

  // --- carga de datos ---
  protected retry(): void {
    this.fetchPlans(true);
  }

  private fetchPlans(showSpinner: boolean): void {
    if (showSpinner) {
      this.isLoading.set(true);
    }
    this.errorMessage.set(null);

    this.planService.getPlans().subscribe({
      next: (plans) => {
        this.plans.set(plans);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 401) {
          this.authService.logout();
        } else {
          this.errorMessage.set(
            'No se pudieron cargar los planes de membresía. Por favor, reintente más tarde.'
          );
        }
      }
    });
  }

  private fetchSubscriptions(): void {
    this.isCheckingSubscriptions.set(true);

    this.subscriptionService.getMySubscriptions().subscribe({
      next: (subscriptions) => {
        this.subscriptions.set(subscriptions);
        this.isCheckingSubscriptions.set(false);
      },
      error: (err) => {
        this.isCheckingSubscriptions.set(false);
        if (err.status === 401) {
          this.authService.logout();
        }
        // Cualquier otro error no bloquea la pantalla: si ya tiene membresía activa, el back responde 409 al contratar.
      }
    });
  }

  // --- acciones ---
  protected openDialog(plan: MembershipPlan): void {
    if (!this.canSubscribe()) {
      return;
    }
    this.successMessage.set(null);
    this.selectedPending.set(null);
    this.selectedPlan.set(plan);
  }

  /** Reabre el modal directo en el paso de pago de la suscripción PENDING. */
  protected openPendingPayment(): void {
    const pending = this.pendingSubscription();
    const plan = this.pendingPlan();
    if (!pending || !plan) {
      return;
    }
    this.successMessage.set(null);
    this.selectedPending.set(pending);
    this.selectedPlan.set(plan);
  }

  protected onAction(card: PlanCard): void {
    if (card.isPending) {
      this.openPendingPayment();
    } else {
      this.openDialog(card.plan);
    }
  }

  protected onPaid(payment: Payment): void {
    const plan = this.selectedPlan();
    this.successMessage.set(
      `Registramos tu pago de ${formatPrice(payment.amount)}${plan ? ` y tu plan ${plan.name} ya está activo` : ''}.`
    );
  }

  protected onDialogClosed(needsRefresh: boolean): void {
    this.selectedPlan.set(null);
    this.selectedPending.set(null);
    if (needsRefresh) {
      this.fetchSubscriptions();
      this.fetchPlans(false);
    }
  }

  // --- helpers de vista ---
  protected formatDate = formatDate;

  /** Tarjeta bloqueada: hay algo vigente o pendiente y esta no es esa tarjeta. */
  protected isLocked(card: PlanCard): boolean {
    return this.isBlocked() && !card.isCurrent && !card.isPending;
  }

  protected buttonLabel(card: PlanCard): string {
    if (card.isPending) {
      return 'Completar pago';
    }
    if (card.isCurrent) {
      return 'Plan vigente';
    }
    return this.isBlocked() ? 'No disponible' : 'Contratar plan';
  }

  /** El de menor valor por día, solo si realmente hay diferencia entre planes. */
  private bestValuePlanId(plans: MembershipPlan[]): number | null {
    if (plans.length < 2) {
      return null;
    }
    const best = plans.reduce((min, p) => (pricePerDay(p) < pricePerDay(min) ? p : min));
    const hasDifference = plans.some((p) => pricePerDay(p) > pricePerDay(best));
    return hasDifference ? best.id : null;
  }
}