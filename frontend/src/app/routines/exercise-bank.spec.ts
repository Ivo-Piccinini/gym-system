import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ExerciseBank } from './exercise-bank';
import { ExerciseAdminService } from './exercise-admin.service';
import { ExerciseService } from './exercise.service';
import { AuthService } from '../auth/auth.service';
import { Exercise } from './routines.models';

describe('ExerciseBank', () => {
  let fixture: ComponentFixture<ExerciseBank>;
  let component: ExerciseBank;
  let mockExerciseService: { getExercises: ReturnType<typeof vi.fn> };
  let mockAdminService: Record<string, ReturnType<typeof vi.fn>>;

  const mockExercises: Exercise[] = [
    { idPublic: 'e1', name: 'Press de Banca', descripcion: 'Empuje horizontal con barra.', muscleGroup: 'PECHO' },
    { idPublic: 'e2', name: 'Sentadilla', descripcion: 'Flexión de rodillas con carga.', muscleGroup: 'CUADRICEPS' },
    { idPublic: 'e3', name: 'Curl de Bíceps', descripcion: 'Flexión de codo con mancuernas.', muscleGroup: 'BICEPS' }
  ];

  beforeEach(() => {
    mockExerciseService = { getExercises: vi.fn().mockReturnValue(of(mockExercises)) };
    mockAdminService = {
      createExercise: vi.fn(),
      updateExercise: vi.fn(),
      deleteExercise: vi.fn().mockReturnValue(of(undefined))
    };
    const mockAuthService = {
      isAdmin: vi.fn().mockReturnValue(false),
      isProfessor: vi.fn().mockReturnValue(true),
      isClient: vi.fn().mockReturnValue(false),
      logout: vi.fn()
    };

    TestBed.configureTestingModule({
      imports: [ExerciseBank],
      providers: [
        provideRouter([]),
        { provide: ExerciseService, useValue: mockExerciseService },
        { provide: ExerciseAdminService, useValue: mockAdminService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    });
    fixture = TestBed.createComponent(ExerciseBank);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  const el = (): HTMLElement => fixture.nativeElement;
  const names = () =>
    Array.from(el().querySelectorAll('.exercise-card .exercise-name')).map((n) => n.textContent?.trim());

  function type(selector: string, value: string): void {
    const input = el().querySelector(selector) as HTMLInputElement;
    input.value = value;
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
  }

  it('lista los ejercicios del back ordenados por nombre', () => {
    expect(mockExerciseService.getExercises).toHaveBeenCalled();
    expect(names()).toEqual(['Curl de Bíceps', 'Press de Banca', 'Sentadilla']);
  });

  it('busca en tiempo real por nombre, sin distinguir mayúsculas ni tildes', () => {
    type('#exercise-search', 'BICEPS');
    expect(names()).toEqual(['Curl de Bíceps']);

    type('#exercise-search', 'sent');
    expect(names()).toEqual(['Sentadilla']);

    type('#exercise-search', '');
    expect(names()).toHaveLength(3);
  });

  it('filtra por grupo muscular y muestra la cantidad en el selector', () => {
    const select = el().querySelector('#exercise-group') as HTMLSelectElement;
    expect(select.textContent).toContain('Pecho (1)');
    expect(select.textContent).toContain('Gemelos (0)');
    expect(select.querySelectorAll('option').length).toBe(12); // "Todos" + los 11 del back

    select.value = 'PECHO';
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(names()).toEqual(['Press de Banca']);
  });

  it('combina búsqueda y grupo muscular', () => {
    const select = el().querySelector('#exercise-group') as HTMLSelectElement;
    select.value = 'PECHO';
    select.dispatchEvent(new Event('change'));
    type('#exercise-search', 'sentadilla');

    expect(names()).toEqual([]);
    expect(el().querySelector('.state-title')?.textContent).toContain('No hay ejercicios para mostrar');
  });

  it('abre el formulario de alta con el botón "Nuevo ejercicio"', () => {
    const button = Array.from(el().querySelectorAll('button')).find((b) =>
      b.textContent?.includes('Nuevo ejercicio')
    ) as HTMLButtonElement;
    button.click();
    fixture.detectChanges();

    expect(el().querySelector('app-exercise-form')).toBeTruthy();
    expect(el().textContent).toContain('Nuevo ejercicio');
  });

  it('elimina un ejercicio tras confirmar y recarga el listado', () => {
    (component as unknown as { askDelete(e: Exercise): void }).askDelete(mockExercises[0]);
    fixture.detectChanges();
    expect(el().textContent).toContain('¿Eliminar este ejercicio?');

    mockExerciseService.getExercises.mockClear();
    (component as unknown as { confirmDelete(): void }).confirmDelete();

    expect(mockAdminService['deleteExercise']).toHaveBeenCalledWith('e1');
    expect(mockExerciseService.getExercises).toHaveBeenCalled();
  });
});
