// Valores EXACTOS del enum DayOfWeek del back (incluye el typo WEDNSEDAY y no existe domingo).
// Si el back corrige el enum, se cambia solo acá.
export type ApiDay =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNSEDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY';

export const DAY_ORDER: readonly ApiDay[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNSEDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY'
];

export const DAY_LABELS: Record<ApiDay, string> = {
  MONDAY: 'Lunes',
  TUESDAY: 'Martes',
  WEDNSEDAY: 'Miércoles',
  THURSDAY: 'Jueves',
  FRIDAY: 'Viernes',
  SATURDAY: 'Sábado'
};

export interface ClassActivity {
  externalId: string;
  name: string;
  description: string;
}

/** Profesor tal como lo devuelve/espera el back (UserDTO). */
export interface ClassProfessor {
  publicId?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  dni?: string;
}

export interface GymClass {
  externalId: string;
  activity: ClassActivity;
  activityName?: string;
  professor?: ClassProfessor;
  dayOfWeek: ApiDay;
  startTime: string; // "HH:mm:ss"
  endTime: string; // "HH:mm:ss"
  capacityMax: number;
  // Los agrega el back cuando implemente el cálculo de cupos por fecha (ver pedido al back).
  enrolledCount?: number;
  availableSpots?: number;
}

export interface Enrollment {
  externalId: string;
  client?: ClassProfessor; // UserDTO del socio (mismos campos que el profesor)
  gymClass: GymClass;
  enrollmentDate: string; // "YYYY-MM-DD": cuándo se hizo la inscripción
}

export type ClassStatus = 'enrolled' | 'full' | 'available';

export const STATUS_LABELS: Record<ClassStatus, string> = {
  enrolled: 'Inscripto',
  full: 'Cupo completo',
  available: 'Disponible'
};

/** Cuerpo que espera el back en POST /gym-classes y PUT /gym-classes/{id} (GymClassDTO). */
export interface ClassPayload {
  externalId?: string; // el back lo exige al editar
  activity: ClassActivity;
  professor: ClassProfessor;
  dayOfWeek: ApiDay;
  startTime: string; // "HH:mm:ss"
  endTime: string; // "HH:mm:ss"
  capacityMax: number;
}
