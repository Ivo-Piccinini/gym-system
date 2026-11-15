import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Largo mínimo sin contar espacios de los costados: "   " o "ab   " no valen como texto.
 * Un campo vacío lo resuelve Validators.required.
 */
export function trimmedMinLength(min: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = (control.value ?? '') as string;
    if (value.length === 0) {
      return null;
    }
    const actual = value.trim().length;
    return actual >= min ? null : { trimmedMinLength: { required: min, actual } };
  };
}
