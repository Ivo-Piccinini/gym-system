import { Component, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { map, startWith } from 'rxjs';
import { ExerciseAdminService } from './exercise-admin.service';
import {
  EXERCISE_DESCRIPTION_MIN,
  EXERCISE_NAME_MIN,
  EXERCISE_TEXT_MAX,
  ExercisePayload
} from './exercise-bank.models';
import { findDuplicateName } from './exercise-bank.utils';
import { trimmedMinLength } from './exercise-form.validators';
import { extractApiError } from './routine.utils';
import { Exercise, MUSCLE_GROUP_LABELS, MUSCLE_GROUPS, MuscleGroup } from './routines.models';

@Component({
  selector: 'app-exercise-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './exercise-form.html',
  styleUrls: ['./exercise-modal.css', './exercise-form.css'],
  host: { '(document:keydown.escape)': 'close()' }
})
export class ExerciseForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly exerciseAdminService = inject(ExerciseAdminService);

  /** Ejercicio a editar; null = alta de uno nuevo. */
  readonly exercise = input<Exercise | null>(null);
  /** Todos los ejercicios actuales: sirven para avisar de nombres repetidos antes de enviar. */
  readonly existingExercises = input<Exercise[]>([]);

  readonly saved = output<Exercise>();
  readonly closed = output<void>();

  protected readonly isSaving = signal<boolean>(false);
  protected readonly serverError = signal<string | null>(null);

  protected readonly textMax = EXERCISE_TEXT_MAX;
  protected readonly nameMin = EXERCISE_NAME_MIN;
  protected readonly descriptionMin = EXERCISE_DESCRIPTION_MIN;

  protected readonly muscleGroups = MUSCLE_GROUPS.map((value) => ({
    value,
    label: MUSCLE_GROUP_LABELS[value]
  }));

  protected readonly form = this.fb.nonNullable.group({
    name: [
      '',
      [
        Validators.required,
        trimmedMinLength(EXERCISE_NAME_MIN),
        Validators.maxLength(EXERCISE_TEXT_MAX)
      ]
    ],
    descripcion: [
      '',
      [
        Validators.required,
        trimmedMinLength(EXERCISE_DESCRIPTION_MIN),
        Validators.maxLength(EXERCISE_TEXT_MAX)
      ]
    ],
    muscleGroup: ['' as MuscleGroup | '', [Validators.required]]
  });

  private readonly value = toSignal(
    this.form.valueChanges.pipe(
      startWith(null),
      map(() => this.form.getRawValue())
    ),
    { initialValue: this.form.getRawValue() }
  );

  /** Aviso visual de nombre repetido (el back también responde 409). */
  protected readonly duplicateMessage = computed<string | null>(() => {
    const clash = findDuplicateName(
      this.existingExercises(),
      this.value().name,
      this.exercise()?.idPublic
    );
    return clash ? `Ya existe un ejercicio llamado "${clash.name}". Elegí otro nombre.` : null;
  });

  ngOnInit(): void {
    const current = this.exercise();
    if (current) {
      this.form.patchValue({
        name: current.name,
        descripcion: current.descripcion,
        muscleGroup: current.muscleGroup
      });
    }
  }

  protected close(): void {
    if (!this.isSaving()) {
      this.closed.emit();
    }
  }

  protected submit(): void {
    this.serverError.set(null);
    if (this.form.invalid || this.duplicateMessage() || this.isSaving()) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    if (!v.muscleGroup) {
      return;
    }

    const payload: ExercisePayload = {
      name: v.name.trim(),
      descripcion: v.descripcion.trim(),
      muscle_group: v.muscleGroup,
      enabled: true
    };

    const current = this.exercise();
    this.isSaving.set(true);
    const request$ = current
      ? this.exerciseAdminService.updateExercise(current.idPublic, payload)
      : this.exerciseAdminService.createExercise(payload);

    request$.subscribe({
      next: (saved) => {
        this.isSaving.set(false);
        this.saved.emit(saved);
      },
      error: (err) => {
        this.isSaving.set(false);
        // 409: el back detectó que el ejercicio ya existe.
        this.serverError.set(
          err.status === 409
            ? err.error?.message || 'Ya existe un ejercicio con ese nombre.'
            : extractApiError(err, 'No se pudo guardar el ejercicio. Intentá nuevamente.')
        );
      }
    });
  }
}
