import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validador para exigir una edad mínima en controles de tipo fecha (YYYY-MM-DD).
 */
export function minAgeValidator(minAge: number = 18): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }

    const val = String(control.value).trim();
    const parts = val.split('-').map(Number);

    let birthDate: Date;
    if (parts.length === 3 && !isNaN(parts[0]) && !isNaN(parts[1]) && !isNaN(parts[2])) {
      birthDate = new Date(parts[0], parts[1] - 1, parts[2]);
    } else {
      birthDate = new Date(val);
      if (isNaN(birthDate.getTime())) return null;
    }

    const today = new Date();
    // Fecha límite para tener al menos minAge años
    const cutoff = new Date(today.getFullYear() - minAge, today.getMonth(), today.getDate());

    return birthDate > cutoff ? { minAge: { requiredAge: minAge } } : null;
  };
}

/**
 * Retorna la fecha máxima permitida en formato YYYY-MM-DD para el atributo max del input date.
 */
export function getMaxBirthDate(minAge: number = 18): string {
  const today = new Date();
  const max = new Date(today.getFullYear() - minAge, today.getMonth(), today.getDate());
  const y = max.getFullYear();
  const m = String(max.getMonth() + 1).padStart(2, '0');
  const d = String(max.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}
