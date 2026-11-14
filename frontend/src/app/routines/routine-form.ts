import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { AdminUserService } from '../admin/users/admin-user.service';
import { AdminUser } from '../admin/users/admin-users.models';
import { ProfileService } from '../profile/profile.service';
import { ExerciseService } from './exercise.service';
import { PartialSaveError, RoutineService } from './routine.service';
import {
  Exercise,
  MUSCLE_GROUPS,
  MUSCLE_GROUP_LABELS,
  MuscleGroup,
  ROUTINE_TYPES,
  ROUTINE_TYPE_LABELS,
  Routine,
  RoutineLine,
  RoutinePayload,
  RoutineType
} from './routines.models';
import {
  dateRangeValidator,
  integerValidator,
  minArrayLength,
  uniqueExercises
} from './routine-form.validators';
import { extractApiError, todayIso, toDateInput } from './routine.utils';

interface ClientOption {
  publicId: string;
  firstName: string;
  lastName: string;
  dni: string;
}

/** Renglón del FormArray: un ejercicio maestro con series, repeticiones y peso. */
function createExerciseRow(fb: FormBuilder, line?: RoutineLine) {
  return fb.nonNullable.group({
    publicId: fb.control<string | null>(line?.publicId ?? null),
    muscleGroup: fb.nonNullable.control<MuscleGroup | ''>(line?.exercise.muscleGroup ?? ''),
    exerciseId: fb.nonNullable.control(line?.exercise.idPublic ?? '', [Validators.required]),
    series: fb.nonNullable.control(line?.series ?? 3, [
      Validators.required,
      Validators.min(1),
      Validators.max(20),
      integerValidator
    ]),
    reps: fb.nonNullable.control(line?.reps ?? 10, [
      Validators.required,
      Validators.min(1),
      Validators.max(100),
      integerValidator
    ]),
    weight: fb.nonNullable.control(line?.weight ?? 0, [
      Validators.required,
      Validators.min(0),
      Validators.max(999)
    ])
  });
}

type ExerciseRowForm = ReturnType<typeof createExerciseRow>;

@Component({
  selector: 'app-routine-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './routine-form.html',
  styleUrls: ['./routine-form.css']
})
export class RoutineForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly routineService = inject(RoutineService);
  private readonly exerciseService = inject(ExerciseService);
  private readonly adminUserService = inject(AdminUserService);
  private readonly profileService = inject(ProfileService);
  protected readonly authService = inject(AuthService);

  protected readonly routeId = this.route.snapshot.paramMap.get('id');
  protected readonly isEditMode = this.routeId !== null;

  protected readonly isLoading = signal<boolean>(true);
  protected readonly isSaving = signal<boolean>(false);
  protected readonly loadError = signal<string | null>(null);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly submitAttempted = signal<boolean>(false);

  private currentUserId = '';
  private originalRoutine: Routine | null = null;

  protected readonly catalog = signal<Exercise[]>([]);
  protected readonly clients = signal<ClientOption[]>([]);

  protected readonly routineTypes = ROUTINE_TYPES;
  protected readonly typeLabels = ROUTINE_TYPE_LABELS;
  protected readonly muscleLabels = MUSCLE_GROUP_LABELS;

  /** Ejercicios maestros clasificados por grupo muscular (alimenta el selector dinámico). */
  protected readonly catalogByGroup = computed(() => {
    const grouped: Partial<Record<MuscleGroup, Exercise[]>> = {};
    for (const exercise of this.catalog()) {
      (grouped[exercise.muscleGroup] ??= []).push(exercise);
    }
    for (const list of Object.values(grouped)) {
      list.sort((a, b) => a.name.localeCompare(b.name, 'es'));
    }
    return grouped;
  });

  protected readonly groupsWithExercises = computed(() =>
    MUSCLE_GROUPS.filter((group) => (this.catalogByGroup()[group]?.length ?? 0) > 0)
  );

  protected readonly form = this.fb.nonNullable.group(
    {
      name: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(60)]],
      clientId: ['', [Validators.required]],
      typeRoutine: ['HYPERTROFY' as RoutineType, [Validators.required]],
      startDate: [todayIso(), [Validators.required]],
      endDate: [''],
      exercises: this.fb.array<ExerciseRowForm>([], [minArrayLength(1), uniqueExercises])
    },
    { validators: [dateRangeValidator] }
  );

  protected get exercisesArray(): FormArray<ExerciseRowForm> {
    return this.form.controls.exercises;
  }

  ngOnInit(): void {
    // Si una edición anterior quedó a medias, el mensaje llega por history.state.
    const flashError = history.state?.['error'];
    if (typeof flashError === 'string') {
      this.errorMessage.set(flashError);
    }
    this.loadInitialData();
  }

  protected loadInitialData(): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    forkJoin({
      exercises: this.exerciseService.getExercises(),
      clients: this.adminUserService.getUsers(undefined, 'ROLE_CLIENT'),
      me: this.profileService.getProfile(),
      routine: this.routeId ? this.routineService.getRoutine(this.routeId) : of(null)
    }).subscribe({
      next: ({ exercises, clients, routine, me }) => {
        this.currentUserId = me.publicId;
        this.originalRoutine = routine;
        this.setCatalog(exercises, routine);
        this.setClients(clients, routine);

        this.exercisesArray.clear();
        if (routine) {
          this.populateForm(routine);
        } else {
          this.addExercise();
        }

        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 401) {
          this.authService.logout();
        } else if (err.status === 404) {
          this.loadError.set('La rutina que intentás editar no existe o ya fue dada de baja.');
        } else {
          this.loadError.set(
            extractApiError(err, 'No se pudieron cargar los datos del formulario. Intentá nuevamente.')
          );
        }
      }
    });
  }

  private setCatalog(exercises: Exercise[], routine: Routine | null): void {
    const merged = [...exercises];
    // Un ejercicio de la rutina que hoy esté dado de baja no viene en el catálogo: se agrega para no perderlo al editar.
    for (const line of routine?.exercises ?? []) {
      if (!merged.some((e) => e.idPublic === line.exercise.idPublic)) {
        merged.push(line.exercise);
      }
    }
    this.catalog.set(merged);
  }

  private setClients(users: AdminUser[], routine: Routine | null): void {
    // El back ignora el filtro "enabled" cuando se filtra por rol, así que se filtra acá.
    const options: ClientOption[] = users
      .filter((user) => user.enabled)
      .map((user) => ({
        publicId: user.publicId,
        firstName: user.firstName,
        lastName: user.lastName,
        dni: user.dni
      }));

    const current = routine?.client;
    if (current && !options.some((o) => o.publicId === current.publicId)) {
      options.push({
        publicId: current.publicId,
        firstName: current.firstName,
        lastName: current.lastName,
        dni: current.dni ?? ''
      });
    }

    options.sort((a, b) => `${a.lastName} ${a.firstName}`.localeCompare(`${b.lastName} ${b.firstName}`, 'es'));
    this.clients.set(options);
  }

  private populateForm(routine: Routine): void {
    this.form.patchValue({
      name: routine.name,
      clientId: routine.client?.publicId ?? '',
      typeRoutine: routine.typeRoutine ?? 'HYPERTROFY',
      startDate: toDateInput(routine.startDate),
      endDate: toDateInput(routine.endDate)
    });

    for (const line of routine.exercises ?? []) {
      this.exercisesArray.push(createExerciseRow(this.fb, line));
    }
    this.form.markAsPristine();
  }

  protected addExercise(): void {
    this.exercisesArray.push(createExerciseRow(this.fb));
    this.exercisesArray.markAsDirty();
  }

  protected removeExercise(index: number): void {
    this.exercisesArray.removeAt(index);
    this.exercisesArray.markAsDirty();
    this.exercisesArray.updateValueAndValidity();
  }

  /** Al cambiar el grupo muscular se limpia el ejercicio elegido (ya no pertenece al grupo). */
  protected onGroupChange(index: number): void {
    this.exercisesArray.at(index).controls.exerciseId.reset('');
  }

  protected exercisesFor(group: MuscleGroup | ''): Exercise[] {
    return group ? (this.catalogByGroup()[group] ?? []) : [];
  }

  protected isTaken(exerciseId: string, currentIndex: number): boolean {
    return this.exercisesArray.controls.some(
      (row, index) => index !== currentIndex && row.controls.exerciseId.value === exerciseId
    );
  }

  protected totalSeries(): number {
    return this.exercisesArray.controls.reduce(
      (acc, row) => acc + (Number(row.controls.series.value) || 0),
      0
    );
  }

  protected showError(control: { invalid: boolean; touched: boolean }): boolean {
    return control.invalid && (control.touched || this.submitAttempted());
  }

  protected onSubmit(): void {
    this.submitAttempted.set(true);
    this.errorMessage.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Revisá los campos marcados antes de guardar la rutina.');
      return;
    }

    const payload = this.buildPayload();
    this.isSaving.set(true);

    const request$ = this.routeId
      ? this.routineService.updateRoutine(this.routeId, payload, this.originalRoutine?.exercises ?? [])
      : this.routineService.createRoutine(payload);

    request$.subscribe({
      next: (saved) => {
        this.isSaving.set(false);
        const verb = this.routeId ? 'actualizada' : 'creada';
        this.router.navigate(['/profesor/rutinas'], {
          state: { success: `Rutina "${saved.name}" ${verb} con éxito.` }
        });
      },
      error: (err) => {
        this.isSaving.set(false);
        if (err instanceof PartialSaveError) {
          // La rutina quedó guardada pero faltan ejercicios: se abre en edición para completarlos.
          this.router.navigate(['/profesor/rutinas', err.routineId, 'editar'], {
            state: {
              error:
                'La rutina se guardó, pero algunos ejercicios no se pudieron cargar. Revisalos y guardá nuevamente. ' +
                extractApiError(err.detail, '')
            }
          });
          return;
        }
        if (err.status === 401) {
          this.authService.logout();
          return;
        }
        this.errorMessage.set(
          extractApiError(err, `Ocurrió un error inesperado al ${this.routeId ? 'actualizar' : 'crear'} la rutina.`)
        );
      }
    });
  }

  /** Carga estructurada para POST/PUT /api/routines con el array anidado de ejercicios. */
  private buildPayload(): RoutinePayload {
    const value = this.form.getRawValue();

    return {
      name: value.name.trim(),
      clientId: value.clientId,
      professorId: this.originalRoutine?.professor?.publicId ?? this.currentUserId,
      typeRoutine: value.typeRoutine,
      startDate: value.startDate,
      endDate: value.endDate || null,
      exercises: value.exercises.map((row) => ({
        ...(row.publicId ? { publicId: row.publicId } : {}),
        exerciseId: row.exerciseId,
        series: Number(row.series),
        reps: Number(row.reps),
        weight: Number(row.weight)
      }))
    };
  }
}