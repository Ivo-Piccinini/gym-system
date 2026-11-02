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

export interface GymClass {
  externalId: string;
  activity: ClassActivity;
  activityName?: string;
  professor?: { firstName?: string; lastName?: string };
  dayOfWeek: ApiDay;
  startTime: string; // "HH:mm:ss"
  endTime: string; // "HH:mm:ss"
  capacityMax: number;
  // Los agrega el back cuando implemente el cálculo de cupos por fecha (ver pedido al back).
  enrolledCount?: number;
  availableSpots?: number;
}
