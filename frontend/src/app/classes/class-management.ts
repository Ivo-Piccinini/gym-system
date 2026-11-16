import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { catchError, forkJoin, of } from 'rxjs';
import { AdminUserService } from '../admin/users/admin-user.service';
import { AuthService } from '../auth/auth.service';
import { extractApiError } from '../routines/routine.utils';
import { NavbarComponent } from '../shared/navbar/navbar';
import { ActivityService } from './activity.service';
import { ClassAdminService } from './class-admin.service';
import { ClassForm } from './class-form';
import { ClassStudents } from './class-students';
import { ClassService } from './class.service';
import {
  ApiDay,
  ClassActivity,
  ClassProfessor,
  DAY_LABELS,
  DAY_ORDER,
  GymClass
} from './classes.models';

@Component({
  selector: 'app-class-management',
  standalone: true,
  imports: [NavbarComponent, ClassForm, ClassStudents],
  templateUrl: './class-management.html',
  styleUrls: ['./class-modal.css', './class-management.css']
})
export class ClassManagement implements OnInit {
  private readonly classService = inject(ClassService);
  private readonly classAdminService = inject(ClassAdminService);
  private readonly activityService = inject(ActivityService);
  private readonly adminUserService = inject(AdminUserService);
  protected readonly authService = inject(AuthService);

  // --- datos ---
  protected readonly classes = signal<GymClass[]>([]);
  protected readonly activities = signal<ClassActivity[]>([]);
  protected readonly professors = signal<ClassProfessor[]>([]);

  // --- estado de pantalla ---
  protected readonly isLoading = signal<boolean>(true);
  protected readonly loadError = signal<string | null>(null);
  protected readonly catalogWarning = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly errorMessage = signal<string | null>(null);

  // --- filtros rápidos ---
  protected readonly professorFilter = signal<string>(''); // '' = todos
  protected readonly dayFilter = signal<ApiDay | ''>(''); // '' = todos

  // --- ventanas ---
  protected readonly formOpen = signal<boolean>(false);
  protected readonly editing = signal<GymClass | null>(null);
  protected readonly studentsFor = signal<GymClass | null>(null);
  protected readonly deleteTarget = signal<GymClass | null>(null);
  protected readonly isDeleting = signal<boolean>(false);

  protected readonly days = DAY_ORDER.map((value) => ({
    value,
    label: DAY_LABELS[value],
    short: DAY_LABELS[value].slice(0, 3)
  }));

  /** Profesores que aparecen en el filtro: los que tienen al menos una clase. */
  protected readonly professorOptions = computed(() => {
    const options = new Map<string, string>();
    for (const c of this.classes()) {
      const key = this.professorKey(c);
      if (key) {
        options.set(key, this.professorName(c));
      }
    }
    return [...options]
      .map(([key, label]) => ({ key, label }))
      .sort((a, b) => a.label.localeCompare(b.label, 'es'));
  });

  /** Clases filtradas, ordenadas por día de la semana y horario. */
  protected readonly filtered = computed(() => {
    const professor = this.professorFilter();
    const day = this.dayFilter();
    return this.classes()
      .filter(
        (c) => (!professor || this.professorKey(c) === professor) && (!day || c.dayOfWeek === day)
      )
      .sort(
        (a, b) =>
          DAY_ORDER.indexOf(a.dayOfWeek) - DAY_ORDER.indexOf(b.dayOfWeek) ||
          a.startTime.localeCompare(b.startTime)
      );
  });

  protected readonly canCreate = computed(
    () => this.activities().length > 0 && this.professors().length > 0
  );

  ngOnInit(): void {
    this.loadAll();
  }

  protected loadAll(): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    forkJoin({
      classes: this.classService.getClasses(),
      activities: this.activityService.getActivities().pipe(catchError(() => of(null))),
      professors: this.adminUserService.getUsers(true, 'ROLE_PROFESSOR').pipe(catchError(() => of(null)))
    }).subscribe({
      next: ({ classes, activities, professors }) => {
        this.classes.set(classes);
        this.activities.set(activities ?? []);
        this.professors.set(
          (professors ?? []).map((u) => ({
            publicId: u.publicId,
            firstName: u.firstName,
            lastName: u.lastName,
            email: u.email,
            dni: u.dni
          }))
        );
        this.catalogWarning.set(
          activities && professors
            ? null
            : 'No se pudieron cargar las disciplinas o los profesores, así que por ahora no se pueden crear clases.'
        );
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.loadError.set(extractApiError(err, 'No se pudieron cargar las clases.'));
      }
    });
  }

  // --- filtros ---
  protected onProfessorFilter(event: Event): void {
    this.professorFilter.set((event.target as HTMLSelectElement).value);
  }

  protected setDayFilter(day: ApiDay | ''): void {
    this.dayFilter.set(day);
  }

  protected clearFilters(): void {
    this.professorFilter.set('');
    this.dayFilter.set('');
  }

  // --- alta / edición ---
  protected openCreate(): void {
    this.editing.set(null);
    this.formOpen.set(true);
  }

  protected openEdit(c: GymClass): void {
    this.editing.set(c);
    this.formOpen.set(true);
  }

  protected closeForm(): void {
    this.formOpen.set(false);
    this.editing.set(null);
  }

  protected onSaved(): void {
    const wasEditing = this.editing() !== null;
    this.closeForm();
    this.showSuccess(wasEditing ? 'Clase actualizada correctamente.' : 'Clase creada correctamente.');
    this.reloadClasses();
  }

  // --- alumnos ---
  protected openStudents(c: GymClass): void {
    this.studentsFor.set(c);
  }

  protected closeStudents(): void {
    this.studentsFor.set(null);
  }

  // --- baja ---
  protected askDelete(c: GymClass): void {
    this.deleteTarget.set(c);
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
    this.classAdminService.deleteClass(target.externalId).subscribe({
      next: () => {
        this.isDeleting.set(false);
        this.deleteTarget.set(null);
        this.showSuccess('Clase eliminada correctamente.');
        this.reloadClasses();
      },
      error: (err) => {
        this.isDeleting.set(false);
        this.deleteTarget.set(null);
        // Ej.: el back no deja eliminar una clase con alumnos inscriptos y explica el motivo.
        this.errorMessage.set(extractApiError(err, 'No se pudo eliminar la clase.'));
      }
    });
  }

  // --- helpers de vista ---
  protected disciplineOf(c: GymClass): string {
    return c.activity?.name ?? c.activityName ?? '—';
  }

  protected professorName(c: GymClass): string {
    return [c.professor?.firstName, c.professor?.lastName].filter(Boolean).join(' ') || '—';
  }

  protected dayLabel(c: GymClass): string {
    return DAY_LABELS[c.dayOfWeek];
  }

  protected time(value: string): string {
    return value.slice(0, 5);
  }

  protected dismissSuccess(): void {
    this.successMessage.set(null);
  }

  protected dismissError(): void {
    this.errorMessage.set(null);
  }

  // --- internos ---
  private professorKey(c: GymClass): string {
    return c.professor?.publicId ?? c.professor?.firstName ?? '';
  }

  private reloadClasses(): void {
    this.classService.getClasses().subscribe({
      next: (list) => this.classes.set(list),
      error: (err) =>
        this.errorMessage.set(extractApiError(err, 'No se pudo actualizar el listado de clases.'))
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
