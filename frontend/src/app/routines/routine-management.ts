import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { RoutineService } from './routine.service';
import { AuthService } from '../auth/auth.service';
import {
  MUSCLE_GROUP_LABELS,
  ROUTINE_TYPES,
  ROUTINE_TYPE_LABELS,
  Routine,
  RoutineLine,
  RoutineType
} from './routines.models';
import { extractApiError, formatDate, todayIso, toDateInput } from './routine.utils';
import { NavbarComponent } from '../shared/navbar/navbar';

@Component({
  selector: 'app-routine-management',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent],
  templateUrl: './routine-management.html',
  styleUrls: ['./routine-management.css']
})
export class RoutineManagement implements OnInit {
  private readonly routineService = inject(RoutineService);
  protected readonly authService = inject(AuthService);

  protected readonly routines = signal<Routine[]>([]);
  protected readonly isLoading = signal<boolean>(true);
  protected readonly isDeleting = signal<boolean>(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  protected readonly searchTerm = signal<string>('');
  protected readonly selectedType = signal<'ALL' | RoutineType>('ALL');
  protected readonly expandedId = signal<string | null>(null);

  protected readonly routineToDelete = signal<Routine | null>(null);

  protected readonly routineTypes = ROUTINE_TYPES;

  protected readonly totalCount = computed(() => this.routines().length);
  protected readonly clientCount = computed(
    () => new Set(this.routines().map((r) => r.client?.publicId).filter(Boolean)).size
  );
  protected readonly exerciseCount = computed(() =>
    this.routines().reduce((acc, r) => acc + (r.exercises?.length ?? 0), 0)
  );

  protected readonly filteredRoutines = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const typeFilter = this.selectedType();

    return this.routines().filter((routine) => {
      if (typeFilter !== 'ALL' && routine.typeRoutine !== typeFilter) {
        return false;
      }

      if (!term) {
        return true;
      }

      const clientName = `${routine.client?.firstName ?? ''} ${routine.client?.lastName ?? ''}`.toLowerCase();
      const dni = (routine.client?.dni ?? '').toLowerCase();
      return (
        routine.name.toLowerCase().includes(term) ||
        clientName.includes(term) ||
        dni.includes(term)
      );
    });
  });

  ngOnInit(): void {
    // Mensaje de éxito que deja el formulario al guardar (se pasa por history.state al navegar).
    const flash = history.state?.['success'];
    if (typeof flash === 'string') {
      this.showSuccess(flash);
    }
    this.loadRoutines();
  }

  protected loadRoutines(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.routineService.getRoutines().subscribe({
      next: (data) => {
        this.routines.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 401) {
          this.authService.logout();
        } else {
          this.errorMessage.set(extractApiError(err, 'Error al cargar las rutinas desde el servidor.'));
        }
      }
    });
  }

  protected updateSearch(event: Event): void {
    this.searchTerm.set((event.target as HTMLInputElement).value);
  }

  protected setTypeFilter(type: 'ALL' | RoutineType): void {
    this.selectedType.set(type);
  }

  protected toggleExpanded(routine: Routine): void {
    this.expandedId.update((current) => (current === routine.publicId ? null : routine.publicId));
  }

  protected promptDelete(routine: Routine): void {
    this.routineToDelete.set(routine);
  }

  protected cancelDelete(): void {
    if (this.isDeleting()) {
      return;
    }
    this.routineToDelete.set(null);
  }

  protected confirmDelete(): void {
    const routine = this.routineToDelete();
    if (!routine) {
      return;
    }

    this.isDeleting.set(true);
    this.errorMessage.set(null);

    this.routineService.deleteRoutine(routine).subscribe({
      next: () => {
        this.isDeleting.set(false);
        this.routineToDelete.set(null);
        this.routines.update((current) => current.filter((r) => r.publicId !== routine.publicId));
        this.showSuccess(`Rutina "${routine.name}" dada de baja correctamente.`);
      },
      error: (err) => {
        this.isDeleting.set(false);
        this.routineToDelete.set(null);
        this.errorMessage.set(
          extractApiError(err, 'No fue posible dar de baja la rutina. Intente nuevamente.')
        );
      }
    });
  }

  protected getTypeLabel(type: RoutineType): string {
    return ROUTINE_TYPE_LABELS[type] ?? type;
  }

  protected getMuscleLabel(line: RoutineLine): string {
    return MUSCLE_GROUP_LABELS[line.exercise.muscleGroup] ?? line.exercise.muscleGroup;
  }

  protected getClientName(routine: Routine): string {
    return routine.client ? `${routine.client.firstName} ${routine.client.lastName}` : 'Sin socio asignado';
  }

  protected getClientInitials(routine: Routine): string {
    const first = routine.client?.firstName?.charAt(0) ?? '';
    const last = routine.client?.lastName?.charAt(0) ?? '';
    return `${first}${last}`.toUpperCase() || '?';
  }

  protected isExpired(routine: Routine): boolean {
    const end = toDateInput(routine.endDate);
    return !!end && end < todayIso();
  }

  protected formatDate(value: string | null): string {
    return formatDate(value);
  }

  private showSuccess(msg: string): void {
    this.successMessage.set(msg);
    setTimeout(() => {
      this.successMessage.set(null);
    }, 4500);
  }
}