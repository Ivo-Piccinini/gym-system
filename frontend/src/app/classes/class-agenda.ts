import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { EMPTY, catchError, switchMap, tap } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { ClassFilters } from './class-filters';
import { ClassService } from './class.service';
import { ApiDay, DAY_LABELS, DAY_ORDER, GymClass } from './classes.models';
import { addDays, parseIsoDate, startOfWeek, toIsoDate } from './date.utils';

@Component({
  selector: 'app-class-agenda',
  standalone: true,
  imports: [RouterLink, ClassFilters],
  templateUrl: './class-agenda.html',
  styleUrls: ['./class-agenda.css']
})
export class ClassAgenda {
  private readonly classService = inject(ClassService);
  protected readonly authService = inject(AuthService);

  // --- estado ---
  protected readonly classes = signal<GymClass[]>([]);
  protected readonly isLoading = signal<boolean>(true);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly selectedDate = signal<string>(toIsoDate(new Date())); // "YYYY-MM-DD"
  protected readonly discipline = signal<string>('');
  protected readonly day = signal<ApiDay | ''>('');

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

  constructor() {
    // Recarga cuando cambia la fecha; switchMap cancela la request anterior si cambia rápido.
    toObservable(this.selectedDate)
      .pipe(
        tap(() => {
          this.isLoading.set(true);
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
  }

  // --- acciones ---
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
}
