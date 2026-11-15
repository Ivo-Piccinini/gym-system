import { Component, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { map, startWith } from 'rxjs';
import { extractApiError } from '../routines/routine.utils';
import { ClassAdminService } from './class-admin.service';
import {
  ApiDay,
  ClassActivity,
  ClassPayload,
  ClassProfessor,
  DAY_LABELS,
  DAY_ORDER,
  GymClass
} from './classes.models';
import { findProfessorOverlap, integerValidator, timeRangeValidator } from './class-form.validators';
import { ClassTimeField } from './class-time-field';
import { addMinutes, formatDuration } from './class-time.utils';

@Component({
  selector: 'app-class-form',
  standalone: true,
  imports: [ReactiveFormsModule, ClassTimeField],
  templateUrl: './class-form.html',
  styleUrls: ['./class-modal.css', './class-form.css'],
  host: { '(document:keydown.escape)': 'close()' }
})
export class ClassForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly classAdminService = inject(ClassAdminService);

  /** Clase a editar; null = alta de una clase nueva. */
  readonly gymClass = input<GymClass | null>(null);
  readonly activities = input<ClassActivity[]>([]);
  readonly professors = input<ClassProfessor[]>([]);
  /** Todas las clases actuales: sirven para avisar de superposiciones antes de enviar. */
  readonly existingClasses = input<GymClass[]>([]);

  readonly saved = output<GymClass>();
  readonly closed = output<void>();

  protected readonly isSaving = signal<boolean>(false);
  protected readonly serverError = signal<string | null>(null);

  protected readonly days = DAY_ORDER.map((value) => ({ value, label: DAY_LABELS[value] }));

  protected readonly form = this.fb.nonNullable.group(
    {
      activityId: ['', [Validators.required]],
      professorId: ['', [Validators.required]],
      dayOfWeek: ['' as ApiDay | '', [Validators.required]],
      startTime: ['', [Validators.required]],
      endTime: ['', [Validators.required]],
      capacityMax: [20, [Validators.required, Validators.min(1), Validators.max(50), integerValidator]]
    },
    { validators: [timeRangeValidator] }
  );

  /** Valores del formulario como signal (incluye los controles deshabilitados). */
  private readonly value = toSignal(
    this.form.valueChanges.pipe(
      startWith(null),
      map(() => this.form.getRawValue())
    ),
    { initialValue: this.form.getRawValue() }
  );

  /** Disciplinas para elegir; al editar se asegura que la actual de la clase esté en la lista. */
  protected readonly activityOptions = computed<ClassActivity[]>(() => {
    const list = this.activities();
    const current = this.gymClass()?.activity;
    return current?.externalId && !list.some((a) => a.externalId === current.externalId)
      ? [current, ...list]
      : list;
  });

  /** Profesores para elegir; al editar se asegura que el actual de la clase esté en la lista. */
  protected readonly professorOptions = computed<ClassProfessor[]>(() => {
    const list = this.professors();
    const current = this.gymClass()?.professor;
    return current?.publicId && !list.some((p) => p.publicId === current.publicId)
      ? [current, ...list]
      : list;
  });

  private readonly selectedProfessor = computed<ClassProfessor | undefined>(() =>
    this.professorOptions().find((p) => p.publicId === this.value().professorId)
  );

  /** Clase del mismo profesor que se pisa con el horario elegido (validación visual). */
  protected readonly overlapMessage = computed<string | null>(() => {
    const v = this.value();
    const clash = findProfessorOverlap(
      this.existingClasses(),
      this.selectedProfessor(),
      v.dayOfWeek,
      v.startTime,
      v.endTime,
      this.gymClass()?.externalId
    );
    if (!clash) {
      return null;
    }
    return (
      `${this.professorLabel(this.selectedProfessor())} ya tiene ${clash.activity?.name ?? 'otra clase'} ` +
      `el ${DAY_LABELS[clash.dayOfWeek]} de ${clash.startTime.slice(0, 5)} a ${clash.endTime.slice(0, 5)}. ` +
      'Elegí otro horario.'
    );
  });

  protected readonly duration = computed<string | null>(() => {
    const v = this.value();
    return v.startTime && v.endTime ? formatDuration(v.startTime, v.endTime) : null;
  });

  constructor() {
    // Al elegir el inicio, el fin se completa solo (+1 hora) o se corrige si quedó antes del inicio.
    this.form.controls.startTime.valueChanges.pipe(takeUntilDestroyed()).subscribe((start) => {
      const end = this.form.controls.endTime.value;
      if (start && (!end || end <= start)) {
        this.form.controls.endTime.setValue(addMinutes(start, 60) ?? '');
      }
    });
  }

  ngOnInit(): void {
    const current = this.gymClass();
    if (current) {
      this.form.patchValue({
        activityId: current.activity?.externalId ?? '',
        professorId: current.professor?.publicId ?? '',
        dayOfWeek: current.dayOfWeek,
        startTime: current.startTime.slice(0, 5),
        endTime: current.endTime.slice(0, 5),
        capacityMax: current.capacityMax
      });
    }
  }

  protected professorLabel(professor?: ClassProfessor): string {
    return [professor?.firstName, professor?.lastName].filter(Boolean).join(' ') || 'El profesor';
  }

  protected close(): void {
    if (!this.isSaving()) {
      this.closed.emit();
    }
  }

  protected submit(): void {
    this.serverError.set(null);
    if (this.form.invalid || this.overlapMessage() || this.isSaving()) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const current = this.gymClass();
    const activity = this.activityOptions().find((a) => a.externalId === v.activityId);
    const professor = this.professorOptions().find((p) => p.publicId === v.professorId);

    if (!activity || !professor || !v.dayOfWeek) {
      this.serverError.set('Elegí una disciplina y un profesor válidos.');
      return;
    }

    const payload: ClassPayload = {
      ...(current ? { externalId: current.externalId } : {}),
      activity,
      professor,
      dayOfWeek: v.dayOfWeek,
      startTime: `${v.startTime.slice(0, 5)}:00`,
      endTime: `${v.endTime.slice(0, 5)}:00`,
      capacityMax: Number(v.capacityMax)
    };

    this.isSaving.set(true);
    const request$ = current
      ? this.classAdminService.updateClass(current.externalId, payload)
      : this.classAdminService.createClass(payload);

    request$.subscribe({
      next: (saved) => {
        this.isSaving.set(false);
        this.saved.emit(saved);
      },
      error: (err) => {
        this.isSaving.set(false);
        // 409: el back detectó la superposición horaria del profesor.
        this.serverError.set(
          err.status === 409
            ? err.error?.message || 'El profesor ya tiene una clase asignada en ese día y rango horario.'
            : extractApiError(err, 'No se pudo guardar la clase. Intentá nuevamente.')
        );
      }
    });
  }
}
