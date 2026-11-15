import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../shared/navbar/navbar';
import { ExerciseAdminService } from './exercise-admin.service';
import { normalizeText } from './exercise-bank.utils';
import { ExerciseForm } from './exercise-form';
import { ExerciseService } from './exercise.service';
import { extractApiError } from './routine.utils';
import { Exercise, MUSCLE_GROUP_LABELS, MUSCLE_GROUPS, MuscleGroup } from './routines.models';

@Component({
  selector: 'app-exercise-bank',
  standalone: true,
  imports: [RouterLink, NavbarComponent, ExerciseForm],
  templateUrl: './exercise-bank.html',
  styleUrls: ['./exercise-modal.css', './exercise-bank.css']
})
export class ExerciseBank implements OnInit {
  private readonly exerciseService = inject(ExerciseService);
  private readonly exerciseAdminService = inject(ExerciseAdminService);

  // --- datos ---
  protected readonly exercises = signal<Exercise[]>([]);

  // --- estado de pantalla ---
  protected readonly isLoading = signal<boolean>(true);
  protected readonly loadError = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly errorMessage = signal<string | null>(null);

  // --- búsqueda y filtro ---
  protected readonly search = signal<string>('');
  protected readonly muscleFilter = signal<MuscleGroup | ''>(''); // '' = todos

  // --- ventanas ---
  protected readonly formOpen = signal<boolean>(false);
  protected readonly editing = signal<Exercise | null>(null);
  protected readonly deleteTarget = signal<Exercise | null>(null);
  protected readonly isDeleting = signal<boolean>(false);

  /** Cantidad de ejercicios de cada grupo muscular. */
  private readonly countsByGroup = computed(() => {
    const counts = new Map<MuscleGroup, number>();
    for (const e of this.exercises()) {
      counts.set(e.muscleGroup, (counts.get(e.muscleGroup) ?? 0) + 1);
    }
    return counts;
  });

  /** Opciones del selector: los 11 grupos del back con su cantidad entre paréntesis. */
  protected readonly groupOptions = computed(() =>
    MUSCLE_GROUPS.map((value) => ({
      value,
      label: `${MUSCLE_GROUP_LABELS[value]} (${this.countsByGroup().get(value) ?? 0})`
    }))
  );

  /** Búsqueda en tiempo real por nombre (sin distinguir mayúsculas ni tildes) + filtro por grupo. */
  protected readonly filtered = computed(() => {
    const term = normalizeText(this.search());
    const group = this.muscleFilter();
    return this.exercises()
      .filter(
        (e) => (!group || e.muscleGroup === group) && (!term || normalizeText(e.name).includes(term))
      )
      .sort((a, b) => a.name.localeCompare(b.name, 'es'));
  });

  ngOnInit(): void {
    this.loadAll();
  }

  protected loadAll(): void {
    this.isLoading.set(true);
    this.loadError.set(null);
    this.exerciseService.getExercises().subscribe({
      next: (list) => {
        this.exercises.set(list);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.loadError.set(extractApiError(err, 'No se pudieron cargar los ejercicios.'));
      }
    });
  }

  // --- búsqueda y filtro ---
  protected onSearch(event: Event): void {
    this.search.set((event.target as HTMLInputElement).value);
  }

  protected onGroupChange(event: Event): void {
    this.muscleFilter.set((event.target as HTMLSelectElement).value as MuscleGroup | '');
  }

  protected clearFilters(): void {
    this.search.set('');
    this.muscleFilter.set('');
  }

  // --- alta / edición ---
  protected openCreate(): void {
    this.editing.set(null);
    this.formOpen.set(true);
  }

  protected openEdit(exercise: Exercise): void {
    this.editing.set(exercise);
    this.formOpen.set(true);
  }

  protected closeForm(): void {
    this.formOpen.set(false);
    this.editing.set(null);
  }

  protected onSaved(): void {
    const wasEditing = this.editing() !== null;
    this.closeForm();
    this.showSuccess(wasEditing ? 'Ejercicio actualizado correctamente.' : 'Ejercicio creado correctamente.');
    this.reload();
  }

  // --- baja ---
  protected askDelete(exercise: Exercise): void {
    this.deleteTarget.set(exercise);
  }

  protected cancelDelete(): void {
    if (!this.isDeleting()) {
      this.deleteTarget.set(null);
    }
  }

  protected confirmDelete(): void {
    const target = this.deleteTarget();
    if (!target || this.isDeleting()) {
      return;
    }
    this.isDeleting.set(true);
    this.exerciseAdminService.deleteExercise(target.idPublic).subscribe({
      next: () => {
        this.isDeleting.set(false);
        this.deleteTarget.set(null);
        this.showSuccess('Ejercicio eliminado correctamente.');
        this.reload();
      },
      error: (err) => {
        this.isDeleting.set(false);
        this.deleteTarget.set(null);
        this.errorMessage.set(extractApiError(err, 'No se pudo eliminar el ejercicio.'));
      }
    });
  }

  // --- helpers de vista ---
  protected groupLabel(exercise: Exercise): string {
    return MUSCLE_GROUP_LABELS[exercise.muscleGroup] ?? exercise.muscleGroup;
  }

  protected dismissSuccess(): void {
    this.successMessage.set(null);
  }

  protected dismissError(): void {
    this.errorMessage.set(null);
  }

  // --- internos ---
  private reload(): void {
    this.exerciseService.getExercises().subscribe({
      next: (list) => this.exercises.set(list),
      error: (err) =>
        this.errorMessage.set(extractApiError(err, 'No se pudo actualizar el listado de ejercicios.'))
    });
  }

  private showSuccess(message: string): void {
    this.successMessage.set(message);
    setTimeout(() => {
      if (this.successMessage() === message) {
        this.successMessage.set(null);
      }
    }, 4000);
  }
}
