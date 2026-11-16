import { AbstractControl, FormArray, ValidationErrors, ValidatorFn } from '@angular/forms';

/** El FormArray debe tener al menos `min` renglones. */
export function minArrayLength(min: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const array = control as FormArray;
    return array.length >= min ? null : { minArray: { required: min, actual: array.length } };
  };
}

/** No se puede repetir el mismo ejercicio maestro dentro de una rutina. */
export const uniqueExercises: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const array = control as FormArray;
  const ids = array.controls
    .map((row) => row.get('exerciseId')?.value as string)
    .filter((id) => !!id);
  return new Set(ids).size === ids.length ? null : { duplicated: true };
};

/** La fecha de fin, si existe, no puede ser anterior a la de inicio. */
export const dateRangeValidator: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const start = group.get('startDate')?.value as string | null;
  const end = group.get('endDate')?.value as string | null;
  return start && end && end < start ? { dateRange: true } : null;
};

/** Series y repeticiones deben ser números enteros. */
export const integerValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = control.value;
  if (value === null || value === undefined || value === '') {
    return null; // lo cubre Validators.required
  }
  return Number.isInteger(Number(value)) ? null : { integer: true };
};