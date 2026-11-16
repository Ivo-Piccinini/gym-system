import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ClassAgenda } from './class-agenda';
import { ClassService } from './class.service';
import { EnrollmentService } from './enrollment.service';
import { AuthService } from '../auth/auth.service';
import { Enrollment, GymClass } from './classes.models';

describe('ClassAgenda (agenda semanal sin fechas)', () => {
  let fixture: ComponentFixture<ClassAgenda>;
  let component: ClassAgenda;
  let mockClassService: { getClasses: ReturnType<typeof vi.fn> };
  let mockEnrollmentService: Record<string, ReturnType<typeof vi.fn>>;

  const spinning: GymClass = {
    externalId: 'class-1',
    activity: { externalId: 'act-1', name: 'Spinning', description: 'Cardio' },
    professor: { firstName: 'Carlos', lastName: 'Perez' },
    dayOfWeek: 'MONDAY',
    startTime: '10:00:00',
    endTime: '11:00:00',
    capacityMax: 15,
    enrolledCount: 5,
    availableSpots: 10
  };

  const otherMonday: GymClass = {
    ...spinning,
    externalId: 'class-2',
    activity: { externalId: 'act-2', name: 'Crossfit', description: 'Funcional' },
    startTime: '18:00:00',
    endTime: '19:00:00',
    availableSpots: 0,
    enrolledCount: 15
  };

  function setup(enrollments: Enrollment[] = []) {
    mockClassService = { getClasses: vi.fn().mockReturnValue(of([otherMonday, spinning])) };
    mockEnrollmentService = {
      getMyEnrollments: vi.fn().mockReturnValue(of(enrollments)),
      enroll: vi.fn().mockReturnValue(of({ externalId: 'e-new', enrollmentDate: '2026-11-14' })),
      cancel: vi.fn().mockReturnValue(of(undefined))
    };
    const mockAuthService = {
      isAdmin: vi.fn().mockReturnValue(false),
      isProfessor: vi.fn().mockReturnValue(false),
      isClient: vi.fn().mockReturnValue(true),
      logout: vi.fn()
    };

    TestBed.configureTestingModule({
      imports: [ClassAgenda],
      providers: [
        provideRouter([]),
        { provide: ClassService, useValue: mockClassService },
        { provide: EnrollmentService, useValue: mockEnrollmentService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    });
    fixture = TestBed.createComponent(ClassAgenda);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('muestra una columna por día (lunes a sábado), sin selector de fecha ni navegación por semanas', () => {
    setup();
    const el: HTMLElement = fixture.nativeElement;

    const titles = Array.from(el.querySelectorAll('.day-title span')).map((s) => s.textContent?.trim());
    expect(titles).toEqual(['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado']);
    expect(el.querySelector('input[type="date"]')).toBeNull();
    expect(el.querySelector('.date-nav')).toBeNull();
  });

  it('ordena por horario las clases de cada día y muestra los cupos libres', () => {
    setup();
    const el: HTMLElement = fixture.nativeElement;

    // Solo las tarjetas (el filtro de disciplinas también lista los nombres, en orden alfabético)
    const names = Array.from(el.querySelectorAll('.class-card .class-name')).map((n) => n.textContent?.trim());
    expect(names).toEqual(['Spinning', 'Crossfit']); // 10:00 antes que 18:00

    const text = el.textContent ?? '';
    expect(text).toContain('10 de 15 lugares');
    expect(text).toContain('Completo');
  });

  it('al inscribirse el cupo baja en uno y aparece el estado "Inscripto"', () => {
    setup();
    mockClassService.getClasses.mockReturnValue(
      of([otherMonday, { ...spinning, availableSpots: 9, enrolledCount: 6 }])
    );
    mockEnrollmentService['getMyEnrollments'].mockReturnValue(
      of([{ externalId: 'e-new', enrollmentDate: '2026-11-14', gymClass: spinning }])
    );

    (component as unknown as { enroll(c: GymClass): void }).enroll(spinning);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('9 de 15 lugares');
    expect(text).toContain('Inscripto');
  });
});
