import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../auth/auth.service';
import { extractApiError } from '../routines/routine.utils';
import { MembershipPlan } from './membership.models';
import { MembershipPlanService } from './membership-plan.service';
import { durationLabel, formatPrice, pricePerMonth } from './membership.utils';
import { NavbarComponent } from '../shared/navbar/navbar';

@Component({
  selector: 'app-plan-management',
  standalone: true,
  imports: [ReactiveFormsModule, NavbarComponent],
  templateUrl: './plan-management.html',
  styleUrls: ['./plan-management.css']
})
export class PlanManagement implements OnInit {
  private readonly planService = inject(MembershipPlanService);
  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly authService = inject(AuthService);

  protected readonly plans = signal<MembershipPlan[]>([]);
  protected readonly isLoading = signal<boolean>(true);
  protected readonly isSaving = signal<boolean>(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly formError = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  protected readonly isFormOpen = signal<boolean>(false);
  protected readonly editingPlan = signal<MembershipPlan | null>(null);

  protected readonly sortedPlans = computed(() =>
    [...this.plans()].sort((a, b) => a.durationDays - b.durationDays)
  );

  protected readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(/\S/)]],
    price: [0, [Validators.required, Validators.min(0.01)]],
    durationDays: [0, [Validators.required, Validators.min(1), Validators.pattern(/^\d+$/)]]
  });

  private successTimer: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    this.loadPlans();
  }

  protected loadPlans(): void {
    this.isLoading.set(true);
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
        } else if (err.status !== 403) {
          this.errorMessage.set(extractApiError(err, 'No se pudieron cargar los planes. Intentá nuevamente.'));
        }
      }
    });
  }

  protected openCreate(): void {
    this.editingPlan.set(null);
    this.form.reset({ name: '', price: 0, durationDays: 0 });
    this.openForm();
  }

  protected openEdit(plan: MembershipPlan): void {
    this.editingPlan.set(plan);
    this.form.reset({ name: plan.name, price: plan.price, durationDays: plan.durationDays });
    this.openForm();
  }

  protected closeForm(): void {
    if (this.isSaving()) {
      return;
    }
    this.isFormOpen.set(false);
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const payload = {
      name: raw.name.trim(),
      price: Number(raw.price),
      durationDays: Number(raw.durationDays)
    };
    const editing = this.editingPlan();

    this.isSaving.set(true);
    this.formError.set(null);

    const request$ = editing
      ? this.planService.updatePlan(editing.publicId, payload)
      : this.planService.createPlan(payload);

    request$.subscribe({
      next: (saved) => {
        this.isSaving.set(false);
        this.isFormOpen.set(false);
        this.plans.update((current) =>
          editing
            ? current.map((p) => (p.publicId === saved.publicId ? saved : p))
            : [...current, saved]
        );
        this.showSuccess(
          `Plan "${saved.name}" ${editing ? 'actualizado' : 'creado'} correctamente. El cambio quedó registrado en la auditoría.`
        );
      },
      error: (err) => {
        this.isSaving.set(false);
        if (err.status === 401) {
          this.authService.logout();
        } else if (err.status !== 403) {
          this.formError.set(extractApiError(err, 'No se pudo guardar el plan. Intentá nuevamente.'));
        }
      }
    });
  }

  protected showError(controlName: 'name' | 'price' | 'durationDays'): boolean {
    const control = this.form.controls[controlName];
    return control.invalid && (control.touched || control.dirty);
  }

  protected formatPrice = formatPrice;
  protected durationLabel = durationLabel;

  protected monthlyLabel(plan: MembershipPlan): string {
    return formatPrice(pricePerMonth(plan));
  }

  private openForm(): void {
    this.formError.set(null);
    this.isFormOpen.set(true);
  }

  private showSuccess(msg: string): void {
    if (this.successTimer) {
      clearTimeout(this.successTimer);
    }
    this.successMessage.set(msg);
    this.successTimer = setTimeout(() => this.successMessage.set(null), 5000);
  }
}