import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ClassAgenda } from './class-agenda';
import { ClassService } from './class.service';
import { EnrollmentService } from './enrollment.service';
import { AuthService } from '../auth/auth.service';
import { ProfileService } from '../profile/profile.service';
import { GymClass } from './classes.models';

describe('ClassAgenda: resaltado de "Mi clase" para el profesor', () => {
  let fixture: ComponentFixture<ClassAgenda>;
  let mockProfileService: { getProfile: ReturnType<typeof vi.fn> };
  let roles: { admin: boolean; professor: boolean; client: boolean };

  const base: GymClass = {
    externalId: 'class-1',
    activity: { externalId: 'act-1', name: 'Spinning', description: 'Cardio' },
    professor: { publicId: 'prof-1', firstName: 'Carlos', lastName: 'Perez' },
    dayOfWeek: 'MONDAY',
    startTime: '10:00:00',
    endTime: '11:00:00',
    capacityMax: 15,
    availableSpots: 10,
    enrolledCount: 5
  };

  const mine = base;
  const someoneElses: GymClass = {
    ...base,
    externalId: 'class-2',
    activity: { externalId: 'act-2', name: 'Crossfit', description: 'Funcional' },
    professor: { publicId: 'prof-2', firstName: 'Ana', lastName: 'Gomez' },
    startTime: '18:00:00',
    endTime: '19:00:00'
  };

  function setup(profileResult = of({ publicId: 'prof-1' })) {
    mockProfileService = { getProfile: vi.fn().mockReturnValue(profileResult) };

    TestBed.configureTestingModule({
      imports: [ClassAgenda],
      providers: [
        provideRouter([]),
        { provide: ClassService, useValue: { getClasses: vi.fn().mockReturnValue(of([mine, someoneElses])) } },
        {
          provide: EnrollmentService,
          useValue: { getMyEnrollments: vi.fn().mockReturnValue(of([])), enroll: vi.fn(), cancel: vi.fn() }
        },
        { provide: ProfileService, useValue: mockProfileService },
        {
          provide: AuthService,
          useValue: {
            isAdmin: vi.fn(() => roles.admin),
            isProfessor: vi.fn(() => roles.professor),
            isClient: vi.fn(() => roles.client),
            logout: vi.fn()
          }
        }
      ]
    });
    fixture = TestBed.createComponent(ClassAgenda);
    fixture.detectChanges();
  }

  const el = (): HTMLElement => fixture.nativeElement;
  const cardOf = (discipline: string): HTMLElement =>
    Array.from(el().querySelectorAll<HTMLElement>('.class-card')).find((c) =>
      c.textContent?.includes(discipline)
    ) as HTMLElement;

  beforeEach(() => {
    roles = { admin: false, professor: false, client: false };
  });

  it('resalta con "Mi clase" solo las clases que dicta el profesor logueado', () => {
    roles.professor = true;
    setup();

    expect(mockProfileService.getProfile).toHaveBeenCalled();
    expect(cardOf('Spinning').classList).toContain('mine');
    expect(cardOf('Spinning').querySelector('.mine-tag')?.textContent).toContain('Mi clase');
    expect(cardOf('Crossfit').classList).not.toContain('mine');
    expect(cardOf('Crossfit').querySelector('.mine-tag')).toBeNull();
    expect(el().querySelectorAll('.mine-tag')).toHaveLength(1);
  });

  it('el resaltado convive con el filtro por disciplina', () => {
    roles.professor = true;
    setup();

    const select = el().querySelector('#discipline-filter') as HTMLSelectElement;
    select.value = 'Spinning';
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(el().querySelectorAll('.class-card')).toHaveLength(1);
    expect(el().querySelectorAll('.mine-tag')).toHaveLength(1);
  });

  it('no resalta nada para un socio y ni siquiera pide el perfil', () => {
    roles.client = true;
    setup();

    expect(mockProfileService.getProfile).not.toHaveBeenCalled();
    expect(el().querySelector('.mine-tag')).toBeNull();
    expect(el().querySelector('.class-card.mine')).toBeNull();
  });

  it('no resalta nada para un administrador', () => {
    roles.admin = true;
    setup();

    expect(mockProfileService.getProfile).not.toHaveBeenCalled();
    expect(el().querySelector('.mine-tag')).toBeNull();
  });

  it('si no se puede cargar el perfil, la agenda se ve igual y sin resaltado', () => {
    roles.professor = true;
    setup(throwError(() => ({ status: 500 })) as never);

    expect(el().querySelectorAll('.class-card')).toHaveLength(2);
    expect(el().querySelector('.mine-tag')).toBeNull();
  });
});
