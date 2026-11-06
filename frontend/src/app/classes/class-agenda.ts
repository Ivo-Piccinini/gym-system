import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { EMPTY, catchError, switchMap, tap } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { ClassFilters } from './class-filters';
import { ClassService } from './class.service';
import {
  ApiDay,
  ClassStatus,
  DAY_LABELS,
  DAY_ORDER,
  Enrollment,
  GymClass,
  STATUS_LABELS
} from './classes.models';
import { addDays, parseIsoDate, startOfWeek, toIsoDate } from './date.utils';
import { EnrollmentService } from './enrollment.service';
import {
  Feedback,
  feedbackForCancelError,
  feedbackForEnrollmentError,
  HttpErrorLike
} from './enrollment-feedback';

@Component({
  selector: 'app-class-agenda',
  standalone: true,
  imports: [RouterLink, ClassFilters],
  templateUrl: './class-agenda.html',
  styleUrls: ['./class-agenda.css']
})
export class ClassAgenda {
  private readonly classService = inject(ClassService);
  private readonly enrollmentService = inject(EnrollmentService);
  protected readonly authService = inject(AuthService);

  // --- estado: clases ---
  protected readonly classes = signal<GymClass[]>([]);
  protected readonly isLoading = signal<boolean>(true);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly selectedDate = signal<string>(toIsoDate(new Date())); // "YYYY-MM-DD"
  protected readonly discipline = signal<string>('');
  protected readonly day = signal<ApiDay | ''>('');

  // --- estado: inscripciones ---
  protected readonly enrollments = signal<Enrollment[]>([]);
  /** externalId de la clase con una acción en curso (deshabilita los botones). */
  protected readonly pendingClassId = signal<string | null>(null);
  protected readonly feedback = signal<Feedback | null>(null);

  // --- derivados ---
  protected readonly disciplines = computed(() =>
    [
      ...new Set(
        this.classes()
          .map((c) => this.disciplineOf(c))
          .filter((name): name is string => !!name)
      )
    ].sort((a, b) => a.localeCompare(b))
  );

  /** Lunes a sábado de la semana de la fecha elegida. */
  protected readonly weekDays = computed(() => {
    const monday = startOfWeek(parseIsoDate(this.selectedDate()));
    return DAY_ORDER.map((apiDay, i) => {
      const date = addDays(monday, i);
      return { apiDay, label: DAY_LABELS[apiDay], date, iso: toIsoDate(date) };
    });
  });

  /** Día (en términos del back) de la fecha elegida; undefined si cae domingo. */
  private readonly selectedApiDay = computed<ApiDay | undefined>(
    () => DAY_ORDER[(parseIsoDate(this.selectedDate()).getDay() + 6) % 7]
  );

  /** Agenda final: aplica filtro de día y disciplina y ordena cada día por horario. */
  protected readonly agenda = computed(() => {
    const discipline = this.discipline();
    const dayFilter = this.day();
    const all = this.classes();

    return this.weekDays()
      .filter((d) => !dayFilter || d.apiDay === dayFilter)
      .map((d) => ({
        ...d,
        classes: all
          .filter(
            (c) => c.dayOfWeek === d.apiDay && (!discipline || this.disciplineOf(c) === discipline)
          )
          .sort((a, b) => a.startTime.localeCompare(b.startTime))
      }));
  });

  protected readonly totalVisible = computed(() =>
    this.agenda().reduce((sum, d) => sum + d.classes.length, 0)
  );

  /**
   * Inscripciones vigentes por clase. El back guarda siempre la fecha de hoy en la inscripción
   * (y cuenta los cupos por esa fecha), así que "inscripto" = inscripción de hoy a esa clase.
   */
  private readonly enrollmentByClass = computed(() => {
    const today = toIsoDate(new Date());
    return new Map(
      this.enrollments()
        .filter((e) => e.enrollmentDate === today && !!e.gymClass?.externalId)
        .map((e) => [e.gymClass.externalId, e] as const)
    );
  });

  constructor() {
    // Recarga cuando cambia la fecha; switchMap cancela la request anterior si cambia rápido.
    toObservable(this.selectedDate)
      .pipe(
        tap(() => {
          // El spinner a pantalla completa solo en la primera carga; después la grilla
          // queda visible mientras se actualizan los datos.
          if (this.classes().length === 0) {
            this.isLoading.set(true);
          }
          this.errorMessage.set(null);
        }),
        switchMap((date) =>
          this.classService.getClasses(date).pipe(
            catchError((err) => {
              this.isLoading.set(false);
              if (err.status === 401) {
                this.authService.logout();
              } else {
                this.errorMessage.set(
                  'No se pudieron cargar las clases. Por favor, reintente más tarde.'
                );
              }
              return EMPTY;
            })
          )
        ),
        takeUntilDestroyed()
      )
      .subscribe((list) => {
        this.classes.set(list);
        this.isLoading.set(false);
      });

    // Solo los clientes tienen inscripciones (el endpoint es exclusivo del rol CLIENT).
    if (this.authService.isClient()) {
      this.loadEnrollments();
    }
  }

  // --- acciones: fecha ---
  protected onDateChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    if (value) {
      this.selectedDate.set(value);
    }
  }

  protected shiftWeek(weeks: number): void {
    this.selectedDate.set(toIsoDate(addDays(parseIsoDate(this.selectedDate()), weeks * 7)));
  }

  protected goToToday(): void {
    this.selectedDate.set(toIsoDate(new Date()));
  }

  /** Clic en una columna: ese día pasa a ser la fecha elegida. */
  protected selectDate(iso: string): void {
    this.selectedDate.set(iso);
  }

  protected retry(): void {
    // Reasignar la misma fecha no dispara el signal, así que se recarga directo.
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.classService.getClasses(this.selectedDate()).subscribe({
      next: (list) => {
        this.classes.set(list);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.errorMessage.set('No se pudieron cargar las clases. Por favor, reintente más tarde.');
      }
    });
  }

  // --- acciones: inscripción (GS-32) ---
  protected enroll(c: GymClass): void {
    if (this.pendingClassId() !== null) {
      return;
    }
    this.pendingClassId.set(c.externalId);
    this.feedback.set(null);

    this.enrollmentService.enroll(c.externalId).subscribe({
      next: (created) => {
        this.pendingClassId.set(null);
        // Se usa la clase que ya tenemos: no dependemos de que la respuesta traiga todo anidado.
        const enrollment: Enrollment = {
          ...created,
          gymClass: c,
          enrollmentDate: created.enrollmentDate ?? toIsoDate(new Date())
        };
        this.enrollments.update((list) => [...list, enrollment]);
        this.adjustSpots(c.externalId, -1);
        this.showFeedback({
          kind: 'success',
          title: 'Inscripción confirmada',
          message: `Te inscribiste a ${this.disciplineOf(c)} (${DAY_LABELS[c.dayOfWeek]} ${this.time(c.startTime)}).`
        });
        this.refreshClasses();
        this.loadEnrollments();
      },
      error: (err: HttpErrorLike) => this.handleActionError(err, feedbackForEnrollmentError(err))
    });
  }

  protected cancel(c: GymClass): void {
    const enrollment = this.enrollmentOf(c);
    if (!enrollment || this.pendingClassId() !== null) {
      return;
    }
    this.pendingClassId.set(c.externalId);
    this.feedback.set(null);

    this.enrollmentService.cancel(enrollment.externalId).subscribe({
      next: () => {
        this.pendingClassId.set(null);
        this.enrollments.update((list) => list.filter((e) => e.externalId !== enrollment.externalId));
        this.adjustSpots(c.externalId, 1);
        this.showFeedback({
          kind: 'success',
          title: 'Inscripción cancelada',
          message: `Cancelaste tu lugar en ${this.disciplineOf(c)} (${DAY_LABELS[c.dayOfWeek]} ${this.time(c.startTime)}).`
        });
        this.refreshClasses();
      },
      error: (err: HttpErrorLike) => this.handleActionError(err, feedbackForCancelError(err))
    });
  }

  protected dismissFeedback(): void {
    this.feedback.set(null);
  }

  // --- helpers de vista ---
  protected disciplineOf(c: GymClass): string | undefined {
    return c.activity?.name ?? c.activityName;
  }

  protected time(value: string): string {
    return value.slice(0, 5); // "18:30:00" -> "18:30"
  }

  protected professorName(c: GymClass): string {
    return [c.professor?.firstName, c.professor?.lastName].filter(Boolean).join(' ');
  }

  protected enrollmentOf(c: GymClass): Enrollment | undefined {
    return this.enrollmentByClass().get(c.externalId);
  }

  /** Los cupos libres son de la fecha elegida: solo aplican a las clases de ese día de la semana. */
  private hasSpotsData(c: GymClass): boolean {
    return c.availableSpots != null && c.dayOfWeek === this.selectedApiDay();
  }

  protected isFull(c: GymClass): boolean {
    return this.hasSpotsData(c) && c.availableSpots === 0;
  }

  protected spotsLabel(c: GymClass): string {
    if (!this.hasSpotsData(c)) {
      return `Cupo máx.: ${c.capacityMax}`;
    }
    return c.availableSpots === 0
      ? 'Completo'
      : `${c.availableSpots} de ${c.capacityMax} lugares`;
  }

  // --- badges de estado (GS-33) ---
  protected statusOf(c: GymClass): ClassStatus | null {
    if (this.enrollmentOf(c)) {
      return 'enrolled';
    }
    if (this.isFull(c)) {
      return 'full';
    }
    if (this.hasSpotsData(c)) {
      return 'available';
    }
    return null; // sin dato de cupos del back, no se afirma nada
  }

  protected statusLabel(status: ClassStatus): string {
    return STATUS_LABELS[status];
  }

  // --- internos ---
  private loadEnrollments(): void {
    this.enrollmentService.getMyEnrollments().subscribe({
      next: (list) => this.enrollments.set(list),
      error: (err: HttpErrorLike) => {
        if (err.status === 401) {
          this.authService.logout();
        } else {
          this.showFeedback({
            kind: 'warning',
            title: 'Inscripciones no disponibles',
            message: 'No pudimos cargar tus inscripciones, por eso no se muestran los estados.'
          });
        }
      }
    });
  }

  /** Recarga silenciosa (sin spinner) para dejar el aforo igual al del back. */
  private refreshClasses(): void {
    this.classService.getClasses(this.selectedDate()).subscribe({
      next: (list) => this.classes.set(list),
      error: () => undefined
    });
  }

  /** Actualiza el aforo visual al instante, antes de que llegue la recarga del back. */
  private adjustSpots(classId: string, delta: number): void {
    this.classes.update((list) =>
      list.map((c) =>
        c.externalId === classId && c.availableSpots != null
          ? {
              ...c,
              availableSpots: Math.max(0, c.availableSpots + delta),
              enrolledCount: c.enrolledCount != null ? c.enrolledCount + delta : c.enrolledCount
            }
          : c
      )
    );
  }

  private handleActionError(err: HttpErrorLike, feedback: Feedback): void {
    this.pendingClassId.set(null);
    if (err.status === 401) {
      this.authService.logout();
      return;
    }
    this.showFeedback(feedback);
    if (err.status === 409 || err.status === 400) {
      this.refreshClasses(); // el cupo pudo cambiar: se actualiza lo que se ve
    }
  }

  private showFeedback(feedback: Feedback): void {
    this.feedback.set(feedback);
    if (feedback.kind === 'success') {
      setTimeout(() => {
        if (this.feedback() === feedback) {
          this.feedback.set(null);
        }
      }, 4000);
    }
  }
}
