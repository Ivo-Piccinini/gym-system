import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { MembershipPlans } from './membership-plans';
import { MembershipPlanService } from './membership-plan.service';
import { SubscriptionService } from './subscription.service';
import { PaymentService } from './payment.service';
import { AuthService } from '../auth/auth.service';
import { MembershipPlan } from './membership.models';

describe('MembershipPlans Component', () => {
  let component: MembershipPlans;
  let fixture: ComponentFixture<MembershipPlans>;
  let mockPlanService: any;
  let mockSubscriptionService: any;
  let mockPaymentService: any;
  let mockAuthService: any;

  const mockPlans: MembershipPlan[] = [
    {
      id: 1,
      publicId: 'plan-1',
      name: 'Plan Mensual',
      price: 15000,
      durationDays: 30
    },
    {
      id: 2,
      publicId: 'plan-2',
      name: 'Plan Trimestral',
      price: 40000,
      durationDays: 90
    }
  ];

  beforeEach(async () => {
    mockPlanService = {
      getPlans: vi.fn().mockReturnValue(of(mockPlans))
    };

    mockSubscriptionService = {
      getMySubscriptions: vi.fn().mockReturnValue(of([])),
      subscribe: vi.fn()
    };

    mockPaymentService = {
      getMyPayments: vi.fn().mockReturnValue(of([]))
    };

    mockAuthService = {
      isAdmin: vi.fn().mockReturnValue(false),
      isProfessor: vi.fn().mockReturnValue(false),
      isClient: vi.fn().mockReturnValue(true),
      logout: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [MembershipPlans],
      providers: [
        provideRouter([]),
        { provide: MembershipPlanService, useValue: mockPlanService },
        { provide: SubscriptionService, useValue: mockSubscriptionService },
        { provide: PaymentService, useValue: mockPaymentService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();
  });

  function createComponent() {
    fixture = TestBed.createComponent(MembershipPlans);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('debe crearse correctamente', () => {
    createComponent();
    expect(component).toBeTruthy();
  });

  it('debe mostrar el banner de modo administrador y deshabilitar botones de contratación si el usuario es ADMIN', () => {
    mockAuthService.isAdmin.mockReturnValue(true);
    mockAuthService.isClient.mockReturnValue(false);
    createComponent();

    const banner = fixture.nativeElement.querySelector('.admin-mode-banner');
    expect(banner).toBeTruthy();
    expect(banner.textContent).toContain('Modo Administrador');

    const buttons = fixture.nativeElement.querySelectorAll('.plan-action');
    expect(buttons.length).toBe(2);
    buttons.forEach((btn: HTMLButtonElement) => {
      expect(btn.disabled).toBe(true);
      expect(btn.textContent).toContain('Solo para socios (Admin)');
    });

    expect((component as any).canSubscribe()).toBe(false);
  });

  it('debe permitir la contratación cuando el usuario es CLIENT y no tiene bloqueos', () => {
    mockAuthService.isAdmin.mockReturnValue(false);
    mockAuthService.isClient.mockReturnValue(true);
    createComponent();

    const banner = fixture.nativeElement.querySelector('.admin-mode-banner');
    expect(banner).toBeNull();

    const buttons = fixture.nativeElement.querySelectorAll('.plan-action');
    expect(buttons.length).toBe(2);
    expect(buttons[0].disabled).toBe(false);
    expect(buttons[0].textContent).toContain('Contratar plan');

    expect((component as any).canSubscribe()).toBe(true);
  });

  it('debe bloquear la apertura del modal de suscripción si el usuario es ADMIN', () => {
    mockAuthService.isAdmin.mockReturnValue(true);
    mockAuthService.isClient.mockReturnValue(false);
    createComponent();

    const cards = (component as any).cards();
    (component as any).onAction(cards[0]);

    expect((component as any).selectedPlan()).toBeNull();
  });
});
