import { Component, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { extractApiError, formatDate } from '../routines/routine.utils';
import { DAY_LABELS, Enrollment, GymClass } from './classes.models';
import { EnrollmentService } from './enrollment.service';

@Component({
  selector: 'app-class-students',
  standalone: true,
  templateUrl: './class-students.html',
  styleUrls: ['./class-modal.css', './class-students.css'],
  host: { '(document:keydown.escape)': 'close()' }
})
export class ClassStudents implements OnInit {
  private readonly enrollmentService = inject(EnrollmentService);

  readonly gymClass = input.required<GymClass>();
  readonly closed = output<void>();

  protected readonly isLoading = signal<boolean>(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly enrollments = signal<Enrollment[]>([]);

  /** Alumnos inscriptos en esta clase (la inscripción dura hasta que el socio la cancela), por apellido. */
  protected readonly visible = computed(() =>
    [...this.enrollments()].sort((a, b) =>
      (a.client?.lastName ?? '').localeCompare(b.client?.lastName ?? '', 'es')
    )
  );

  protected readonly formatDate = formatDate;

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    // El back no tiene un endpoint por clase: se pide el listado completo y se filtra acá.
    this.enrollmentService.getAll().subscribe({
      next: (list) => {
        const classId = this.gymClass().externalId;
        this.enrollments.set(list.filter((e) => e.gymClass?.externalId === classId));
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(extractApiError(err, 'No se pudo cargar la lista de alumnos.'));
      }
    });
  }

  protected close(): void {
    this.closed.emit();
  }

  protected dayLabel(c: GymClass): string {
    return DAY_LABELS[c.dayOfWeek];
  }

  protected time(value: string): string {
    return value.slice(0, 5);
  }
}
