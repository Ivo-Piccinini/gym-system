// Valores EXACTOS de los enums del back (MuscleGroup y TypeRoutines).
export type MuscleGroup =
  | 'PECHO'
  | 'ESPALDA'
  | 'HOMBROS'
  | 'BICEPS'
  | 'TRICEPS'
  | 'ANTEBRAZOS'
  | 'CUADRICEPS'
  | 'ISQUIOTIBIALES'
  | 'GLUTEOS'
  | 'GEMELOS'
  | 'ABDOMINALES';

export const MUSCLE_GROUPS: readonly MuscleGroup[] = [
  'PECHO',
  'ESPALDA',
  'HOMBROS',
  'BICEPS',
  'TRICEPS',
  'ANTEBRAZOS',
  'CUADRICEPS',
  'ISQUIOTIBIALES',
  'GLUTEOS',
  'GEMELOS',
  'ABDOMINALES'
];

export const MUSCLE_GROUP_LABELS: Record<MuscleGroup, string> = {
  PECHO: 'Pecho',
  ESPALDA: 'Espalda',
  HOMBROS: 'Hombros',
  BICEPS: 'Bíceps',
  TRICEPS: 'Tríceps',
  ANTEBRAZOS: 'Antebrazos',
  CUADRICEPS: 'Cuádriceps',
  ISQUIOTIBIALES: 'Isquiotibiales',
  GLUTEOS: 'Glúteos',
  GEMELOS: 'Gemelos',
  ABDOMINALES: 'Abdominales'
};

// El back escribe HITT (no HIIT); se respeta el valor exacto del enum.
export type RoutineType = 'HYPERTROFY' | 'CARDIO' | 'HITT' | 'GAP' | 'FULLBODY';

export const ROUTINE_TYPES: readonly RoutineType[] = ['HYPERTROFY', 'CARDIO', 'HITT', 'GAP', 'FULLBODY'];

export const ROUTINE_TYPE_LABELS: Record<RoutineType, string> = {
  HYPERTROFY: 'Hipertrofia',
  CARDIO: 'Cardio',
  HITT: 'HIIT',
  GAP: 'GAP',
  FULLBODY: 'Full body'
};

/** Ejercicio maestro (GET /api/exercises). */
export interface Exercise {
  idPublic: string;
  name: string;
  descripcion: string;
  muscleGroup: MuscleGroup;
}

export interface RoutineUser {
  publicId: string;
  firstName: string;
  lastName: string;
  dni?: string;
  email?: string;
}

/** Renglón de una rutina tal como lo devuelve el back (FullRoutineDtoResponse). */
export interface RoutineLine {
  publicId: string;
  exercise: Exercise;
  series: number;
  reps: number;
  weight: number;
}

export interface Routine {
  publicId: string;
  name: string;
  startDate: string | null; // "YYYY-MM-DD"
  endDate: string | null; // "YYYY-MM-DD"
  typeRoutine: RoutineType;
  enabled?: boolean;
  client: RoutineUser | null;
  professor: RoutineUser | null;
  exercises: RoutineLine[];
}

/** Renglón anidado que se envía en POST/PUT /api/routines. */
export interface RoutineExercisePayload {
  publicId?: string; // solo al editar un renglón existente
  exerciseId: string;
  series: number;
  reps: number;
  weight: number;
}

/** Carga estructurada que arma el formulario (rutina + array de ejercicios). */
export interface RoutinePayload {
  name: string;
  clientId: string;
  professorId: string;
  typeRoutine: RoutineType;
  startDate: string;
  endDate: string | null;
  exercises: RoutineExercisePayload[];
}