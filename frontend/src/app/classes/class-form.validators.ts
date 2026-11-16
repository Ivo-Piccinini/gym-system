import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { ApiDay, ClassProfessor, GymClass } from './classes.models';

/** La hora de fin tiene que ser estrictamente posterior a la de inicio ("HH:mm" compara bien como texto). */
export const timeRangeValidator: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const start = group.get('startTime')?.value as string | null;
  const end = group.get('endTime')?.value as string | null;
  return start && end && end <= start ? { timeRange: true } : null;
};

/** El cupo tiene que ser un número entero. */
export const integerValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = control.value;
  if (value === null || value === undefined || value === '') {
    return null; // lo cubre Validators.required
  }
  return Number.isInteger(Number(value)) ? null : { integer: true };
};

/** Mismo profesor: por publicId; si alguno no lo trae, por nombre y apellido. */
export function sameProfessor(a?: ClassProfessor, b?: ClassProfessor): boolean {
  if (!a || !b) {
    return false;
  }
  if (a.publicId && b.publicId) {
    return a.publicId === b.publicId;
  }
  return !!a.firstName && a.firstName === b.firstName && a.lastName === b.lastName;
}

/**
 * Busca una clase del mismo profesor, el mismo día, cuyo horario se pisa con el rango dado.
 * Misma regla que el back (inicio < fin existente y fin > inicio existente), así se avisa
 * antes de enviar. Al editar se excluye la propia clase.
 */
export function findProfessorOverlap(
  classes: readonly GymClass[],
  professor: ClassProfessor | undefined,
  day: ApiDay | '' | null | undefined,
  start: string | null | undefined,
  end: string | null | undefined,
  excludeExternalId?: string
): GymClass | undefined {
  if (!professor || !day || !start || !end || end <= start) {
    return undefined;
  }
  const from = start.slice(0, 5);
  const to = end.slice(0, 5);
  return classes.find(
    (c) =>
      c.externalId !== excludeExternalId &&
      c.dayOfWeek === day &&
      sameProfessor(c.professor, professor) &&
      from < c.endTime.slice(0, 5) &&
      to > c.startTime.slice(0, 5)
  );
}
