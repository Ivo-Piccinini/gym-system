import { MuscleGroup } from './routines.models';

/** Cuerpo que espera el back en POST /exercises y PUT /exercises/{id} (ExerciseDtoRequest). */
export interface ExercisePayload {
  name: string;
  descripcion: string;
  muscle_group: MuscleGroup;
  enabled: boolean; // el back lo exige también al editar
}

/** Límite de las columnas de nombre y descripción en la base de datos (varchar 255). */
export const EXERCISE_TEXT_MAX = 255;
export const EXERCISE_NAME_MIN = 3;
export const EXERCISE_DESCRIPTION_MIN = 10;
