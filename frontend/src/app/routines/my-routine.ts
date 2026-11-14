import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { RoutineService } from './routine.service';
import { AuthService } from '../auth/auth.service';
import {
  MUSCLE_GROUP_LABELS,
  ROUTINE_TYPE_LABELS,
  Routine,
  RoutineLine
} from './routines.models';
import { extractApiError, formatDate, todayIso, toDateInput } from './routine.utils';

@Component({
  selector: 'app-my-routine',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './my-routine.html',
  styleUrls: ['./my-routine.css']
})
export class MyRoutine implements OnInit {
  private readonly routineService = inject(RoutineService);
  protected readonly authService = inject(AuthService);

  protected readonly routine = signal<Routine | null>(null);
  protected readonly isLoading = signal<boolean>(true);
  // 404 del back = el socio todavía no tiene una rutina activa asignada.
  protected readonly isEmpty = signal<boolean>(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly lines = computed<RoutineLine[]>(() => this.routine()?.exercises ?? []);

  protected readonly isExpired = computed(() => {
    const end = toDateInput(this.routine()?.endDate);
    return !!end && end < todayIso();
  });

  ngOnInit(): void {
    this.loadRoutine();
  }

  protected loadRoutine(): void {
    this.isLoading.set(true);
    this.isEmpty.set(false);
    this.errorMessage.set(null);

    this.routineService.getMyRoutine().subscribe({
      next: (data) => {
        this.routine.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.routine.set(null);
        this.isLoading.set(false);
        if (err.status === 404) {
          this.isEmpty.set(true);
        } else if (err.status !== 401 && err.status !== 403) {
          // 401/403 ya los resuelve el errorInterceptor (logout / redirección al perfil).
          this.errorMessage.set(extractApiError(err, 'No pudimos cargar tu rutina. Intentá nuevamente.'));
        }
      }
    });
  }

  protected getTypeLabel(): string {
    const type = this.routine()?.typeRoutine;
    return type ? (ROUTINE_TYPE_LABELS[type] ?? type) : '';
  }

  protected getMuscleLabel(line: RoutineLine): string {
    return MUSCLE_GROUP_LABELS[line.exercise.muscleGroup] ?? line.exercise.muscleGroup;
  }

  protected getProfessorName(): string {
    const prof = this.routine()?.professor;
    return prof ? `${prof.firstName} ${prof.lastName}` : 'Sin asignar';
  }

  protected formatWeight(weight: number): string {
    return weight > 0 ? `${weight} kg` : 'Corporal';
  }

  protected formatDate(value: string | null | undefined): string {
    return formatDate(value);
  }
}