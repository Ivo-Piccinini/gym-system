import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ExerciseForm } from './exercise-form';
import { ExerciseAdminService } from './exercise-admin.service';
import { Exercise } from './routines.models';

describe('ExerciseForm', () => {
  let fixture: ComponentFixture<ExerciseForm>;
  let mockAdminService: Record<string, ReturnType<typeof vi.fn>>;

  const existing: Exercise[] = [
    { idPublic: 'e1', name: 'Press de Banca', descripcion: 'Empuje horizontal con barra.', muscleGroup: 'PECHO' }
  ];

  const created: Exercise = {
    idPublic: 'e-new',
    name: 'Remo con barra',
    descripcion: 'Tracción horizontal para espalda.',
    muscleGroup: 'ESPALDA'
  };

  beforeEach(() => {
    mockAdminService = {
      createExercise: vi.fn().mockReturnValue(of(created)),
      updateExercise: vi.fn().mockReturnValue(of(created))
    };

    TestBed.configureTestingModule({
      imports: [ExerciseForm],
      providers: [{ provide: ExerciseAdminService, useValue: mockAdminService }]
    });
    fixture = TestBed.createComponent(ExerciseForm);
    fixture.componentRef.setInput('existingExercises', existing);
  });

  const el = (): HTMLElement => fixture.nativeElement;

  function init(exercise: Exercise | null = null): void {
    fixture.componentRef.setInput('exercise', exercise);
    fixture.detectChanges();
  }

  function fill(id: string, value: string, eventName = 'input'): void {
    const control = el().querySelector(`#${id}`) as HTMLInputElement;
    control.value = value;
    control.dispatchEvent(new Event(eventName));
    fixture.detectChanges();
  }

  function submit(): void {
    (el().querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  it('exige nombre, descripción y grupo muscular: no envía y muestra los errores', () => {
    init();
    submit();

    expect(mockAdminService['createExercise']).not.toHaveBeenCalled();
    const text = el().textContent ?? '';
    expect(text).toContain('El nombre es obligatorio.');
    expect(text).toContain('La descripción es obligatoria.');
    expect(text).toContain('Elegí un grupo muscular.');
  });

  it('valida el largo mínimo del nombre y de la descripción', () => {
    init();
    fill('exercise-name', 'ab');
    fill('exercise-description', 'corta');
    submit();

    const text = el().textContent ?? '';
    expect(text).toContain('al menos 3 caracteres');
    expect(text).toContain('al menos 10 caracteres');
    expect(mockAdminService['createExercise']).not.toHaveBeenCalled();
  });

  it('avisa si el nombre ya existe (sin distinguir mayúsculas) y bloquea el envío', () => {
    init();
    fill('exercise-name', 'PRESS DE BANCA');
    fill('exercise-description', 'Una descripción válida y larga.');
    fill('exercise-muscle', 'PECHO', 'change');

    expect(el().textContent).toContain('Ya existe un ejercicio llamado "Press de Banca"');
    const submitButton = el().querySelector('button[type="submit"]') as HTMLButtonElement;
    expect(submitButton.disabled).toBe(true);
  });

  it('envía el alta con el formato que espera el back', () => {
    init();
    const saved = vi.fn();
    fixture.componentInstance.saved.subscribe(saved);

    fill('exercise-name', '  Remo con barra ');
    fill('exercise-description', 'Tracción horizontal para espalda.');
    fill('exercise-muscle', 'ESPALDA', 'change');
    submit();

    expect(mockAdminService['createExercise']).toHaveBeenCalledWith({
      name: 'Remo con barra',
      descripcion: 'Tracción horizontal para espalda.',
      muscle_group: 'ESPALDA',
      enabled: true
    });
    expect(saved).toHaveBeenCalledWith(created);
  });

  it('muestra el mensaje del back cuando responde 409 (el ejercicio ya existe)', () => {
    mockAdminService['createExercise'].mockReturnValue(
      throwError(() => ({ status: 409, error: { message: 'Ya existe ese ejercicio' } }))
    );
    init();
    fill('exercise-name', 'Remo con barra');
    fill('exercise-description', 'Tracción horizontal para espalda.');
    fill('exercise-muscle', 'ESPALDA', 'change');
    submit();

    expect(el().querySelector('.modal-alert')?.textContent).toContain('Ya existe ese ejercicio');
  });

  it('en edición precarga los datos y usa PUT con el id del ejercicio', () => {
    init(existing[0]);

    expect((el().querySelector('#exercise-name') as HTMLInputElement).value).toBe('Press de Banca');
    expect(el().textContent).toContain('Editar ejercicio');

    fill('exercise-description', 'Empuje horizontal con barra, agarre medio.');
    submit();

    expect(mockAdminService['updateExercise']).toHaveBeenCalledWith('e1', {
      name: 'Press de Banca',
      descripcion: 'Empuje horizontal con barra, agarre medio.',
      muscle_group: 'PECHO',
      enabled: true
    });
    expect(mockAdminService['createExercise']).not.toHaveBeenCalled();
  });
});
