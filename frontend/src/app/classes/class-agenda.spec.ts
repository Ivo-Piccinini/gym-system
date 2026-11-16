import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ClassAgenda } from './class-agenda';
import { ClassService } from './class.service';
import { EnrollmentService } from './enrollment.service';
import { AuthService } from '../auth/auth.service';
import { GymClass } from './classes.models';

describe('ClassAgenda Component', () => {
  let component: ClassAgenda;
  let fixture: ComponentFixture<ClassAgenda>;
  let mockClassService: any;
  let mockEnrollmentService: any;
  let mockAuthService: any;

  const mockClasses: GymClass[] = [
    {
      externalId: 'class-1',
      activity: { externalId: 'act-1', name: 'Spinning', description: 'Cardio' },
      activityName: 'Spinning',
      professor: { firstName: 'Carlos', lastName: 'Perez' },
      dayOfWeek: 'MONDAY',
      startTime: '10:00:00',
      endTime: '11:00:00',
      capacityMax: 15,
      availableSpots: 10,
      enrolledCount: 5
    }
  ];

  beforeEach(async () => {
    mockClassService = {
      getClasses: vi.fn().mockReturnValue(of(mockClasses))
    };

    mockEnrollmentService = {
      getMyEnrollments: vi.fn().mockReturnValue(of([])),
      enroll: vi.fn().mockReturnValue(of({ externalId: 'e-new', enrollmentDate: '2026-11-14' })),
      cancel: vi.fn().mockReturnValue(of(undefined))
    };

    mockAuthService = {
      isAdmin: vi.fn().mockReturnValue(false),
      isProfessor: vi.fn().mockReturnValue(false),
      isClient: vi.fn().mockReturnValue(true),
      logout: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [ClassAgenda],
      providers: [
        provideRouter([]),
        { provide: ClassService, useValue: mockClassService },
        { provide: EnrollmentService, useValue: mockEnrollmentService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();
  });

  function createComponent() {
    fixture = TestBed.createComponent(ClassAgenda);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('debe crearse correctamente', () => {
    createComponent();
    expect(component).toBeTruthy();
  });

  it('debe mostrar el banner de modo administrador cuando el usuario es ADMIN', () => {
    mockAuthService.isAdmin.mockReturnValue(true);
    mockAuthService.isClient.mockReturnValue(false);
    createComponent();

    const banner = fixture.nativeElement.querySelector('.admin-mode-banner');
    expect(banner).toBeTruthy();
    expect(banner.textContent).toContain('Modo Administrador');
  });

  it('no debe mostrar el banner de administrador cuando el usuario es socio (CLIENT)', () => {
    mockAuthService.isAdmin.mockReturnValue(false);
    mockAuthService.isClient.mockReturnValue(true);
    createComponent();

    const banner = fixture.nativeElement.querySelector('.admin-mode-banner');
    expect(banner).toBeNull();
  });

  it('debe mostrar la etiqueta de solo lectura y no botones de inscripcion si es ADMIN', () => {
    mockAuthService.isAdmin.mockReturnValue(true);
    mockAuthService.isClient.mockReturnValue(false);
    createComponent();

    const adminTag = fixture.nativeElement.querySelector('.admin-view-tag');
    expect(adminTag).toBeTruthy();
    expect(adminTag.textContent).toContain('Solo lectura (Admin)');

    const actionButtons = fixture.nativeElement.querySelectorAll('.card-action');
    expect(actionButtons.length).toBe(0);
  });

  it('debe bloquear el llamado a enroll() si el usuario es administrador', () => {
    mockAuthService.isAdmin.mockReturnValue(true);
    mockAuthService.isClient.mockReturnValue(false);
    createComponent();

    (component as any).enroll(mockClasses[0]);

    expect(mockEnrollmentService.enroll).not.toHaveBeenCalled();
    const feedback = (component as any).feedback();
    expect(feedback).toBeTruthy();
    expect(feedback.kind).toBe('warning');
    expect(feedback.message).toContain('Los administradores no pueden inscribirse a clases');
  });

  it('debe permitir la inscripción normalmente si el usuario es cliente', () => {
    mockAuthService.isAdmin.mockReturnValue(false);
    mockAuthService.isClient.mockReturnValue(true);
    createComponent();

    (component as any).enroll(mockClasses[0]);

    expect(mockEnrollmentService.enroll).toHaveBeenCalledWith('class-1');
  });
});
