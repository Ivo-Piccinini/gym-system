import { Exercise } from './routines.models';

/** Minúsculas y sin tildes, para buscar "biceps" y encontrar "Bíceps". */
export function normalizeText(value: string | null | undefined): string {
  return (value ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();
}

/**
 * Busca un ejercicio con el mismo nombre (sin distinguir mayúsculas ni tildes), ignorando el que se
 * está editando. Sirve para avisar antes de enviar, igual que el 409 del back.
 */
export function findDuplicateName(
  exercises: readonly Exercise[],
  name: string | null | undefined,
  excludeIdPublic?: string
): Exercise | undefined {
  const wanted = normalizeText(name);
  if (!wanted) {
    return undefined;
  }
  return exercises.find((e) => e.idPublic !== excludeIdPublic && normalizeText(e.name) === wanted);
}
