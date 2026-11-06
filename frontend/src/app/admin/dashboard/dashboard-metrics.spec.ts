import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { DashboardMetricsComponent } from './dashboard-metrics';
import { PaymentService } from '../../membership/payment.service';
import { ClassService } from '../../classes/class.service';
import { AuditService } from '../audit/audit.service';
import { AuthService } from '../../auth/auth.service';
import { Payment } from '../../membership/membership.models';
import { GymClass } from '../../classes/classes.models';
import { AuditLog } from '../audit/audit.models';

describe('DashboardMetricsComponent', () => {
  let component: DashboardMetricsComponent;
  let fixture: ComponentFixture<DashboardMetricsComponent>;

  const mockPayments: Payment[] = [
    { id: 1, publicId: 'p1', subscriptionId: 1, amount: 20000, paymentDate: '2026-11-01', method: 'EFECTIVO' },
    { id: 2, publicId: 'p2', subscriptionId: 2, amount: 25000, paymentDate: '2026-11-02', method: 'TARJETA' }
  ];

  const mockClasses: GymClass[] = [
    {
      externalId: 'c1',
      activity: { externalId: 'a1', name: 'Spinning', description: 'Bici' },
      activityName: 'Spinning',
      dayOfWeek: 'MONDAY',
      startTime: '08:00:00',
      endTime: '09:00:00',
      capacityMax: 20,
      enrolledCount: 15,
      availableSpots: 5
    },
    {
      externalId: 'c2',
      activity: { externalId: 'a2', name: 'Crossfit', description: 'Fuerza' },
      activityName: 'Crossfit',
      dayOfWeek: 'WEDNSEDAY',
      startTime: '18:00:00',
      endTime: '19:30:00',
      capacityMax: 20,
      enrolledCount: 5,
      availableSpots: 15
    }
  ];

  const mockAuditLogs: AuditLog[] = [
    {
      id: 1,
      action: 'UPDATE_PLAN',
      performedBy: 'admin',
      timestamp: '2026-11-05T12:00:00',
      details: '[{"name":"Pase Libre","oldPrice":20000,"newPrice":25000}]'
    },
    {
      id: 2,
      action: 'PROCESS_PAYMENT',
      performedBy: 'admin',
      timestamp: '2026-11-05T11:00:00',
      details: '[{"amount":25000}]'
    },
    {
      id: 3,
      action: 'USER_REGISTRATION',
      performedBy: 'admin',
      timestamp: '2026-11-05T10:00:00',
      details: '[{"username":"socio1"}]'
    }
  ];

  let paymentServiceMock: { getAllPayments: ReturnType<typeof vi.fn> };
  let classServiceMock: { getClasses: ReturnType<typeof vi.fn> };
  let auditServiceMock: { getAuditLogs: ReturnType<typeof vi.fn> };
  let authServiceMock: { isAdmin: ReturnType<typeof vi.fn>; username: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    paymentServiceMock = {
      getAllPayments: vi.fn().mockReturnValue(of(mockPayments))
    };
    classServiceMock = {
      getClasses: vi.fn().mockReturnValue(of(mockClasses))
    };
    auditServiceMock = {
      getAuditLogs: vi.fn().mockReturnValue(of(mockAuditLogs))
    };
    authServiceMock = {
      isAdmin: vi.fn().mockReturnValue(true),
      username: vi.fn().mockReturnValue('admin')
    };

    await TestBed.configureTestingModule({
      imports: [DashboardMetricsComponent],
      providers: [
        provideRouter([]),
        { provide: PaymentService, useValue: paymentServiceMock },
        { provide: ClassService, useValue: classServiceMock },
        { provide: AuditService, useValue: auditServiceMock },
        { provide: AuthService, useValue: authServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardMetricsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debe crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  it('debe calcular KPIs correctamente tras cargar los datos', () => {
    // Total Revenue: 20000 + 25000 = 45000
    expect((component as any).totalRevenue()).toBe(45000);
    expect((component as any).totalPaymentsCount()).toBe(2);

    // Ocupación: (15 + 5) / (20 + 20) = 20/40 = 50%
    expect((component as any).averageOccupancyRate()).toBe(50);

    // Auditoría: 3 logs en total, 1 cambio de plan/precio
    expect((component as any).totalAuditLogsCount()).toBe(3);
    expect((component as any).priceChangesCount()).toBe(1);
  });

  it('permite alternar el tipo de gráfico de cobros entre línea y barras', () => {
    expect((component as any).revenueChartType()).toBe('line');
    component.toggleRevenueChartType('bar');
    expect((component as any).revenueChartType()).toBe('bar');
    component.toggleRevenueChartType('line');
    expect((component as any).revenueChartType()).toBe('line');
  });

  it('filtra logs de auditoría por categoría de Precios y Planes', () => {
    component.setCategoryFilter('PRICE_CHANGES');
    const filtered = (component as any).filteredAuditLogs();
    expect(filtered.length).toBe(1);
    expect(filtered[0].action).toBe('UPDATE_PLAN');
  });

  it('filtra logs de auditoría por término de búsqueda', () => {
    (component as any).auditSearchTerm.set('socio1');
    const filtered = (component as any).filteredAuditLogs();
    expect(filtered.length).toBe(1);
    expect(filtered[0].action).toBe('USER_REGISTRATION');
  });

  it('abre y cierra el modal de inspección forense', () => {
    expect((component as any).isModalOpen()).toBe(false);
    component.openInspector(mockAuditLogs[0]);
    expect((component as any).isModalOpen()).toBe(true);
    expect((component as any).selectedLogForModal()).toEqual(mockAuditLogs[0]);

    component.closeInspector();
    expect((component as any).isModalOpen()).toBe(false);
    expect((component as any).selectedLogForModal()).toBeNull();
  });

  it('formatea correctamente detalles de cambio de precio JSON', () => {
    const raw = '[{"name":"Pase Mensual","oldPrice":18000,"newPrice":22000}]';
    const info = component.getPriceChangeInfo(raw);
    expect(info).toEqual({
      name: 'Pase Mensual',
      oldPrice: 18000,
      newPrice: 22000,
      price: undefined
    });
  });

  it('maneja error cuando falla la carga de datos', () => {
    paymentServiceMock.getAllPayments.mockReturnValue(throwError(() => new Error('Error de conexión')));
    component.loadAllData();
    expect((component as any).errorMessage()).toContain('Ocurrió un error al cargar las métricas');
  });
});
