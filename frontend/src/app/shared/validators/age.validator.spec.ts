import { FormControl } from '@angular/forms';
import { describe, it, expect } from 'vitest';
import { minAgeValidator, getMaxBirthDate } from './age.validator';

describe('minAgeValidator', () => {
  const validator = minAgeValidator(18);

  it('permite valores nulos o vacíos dejando la obligatoriedad a Validators.required', () => {
    expect(validator(new FormControl(''))).toBeNull();
    expect(validator(new FormControl(null))).toBeNull();
  });

  it('valida exitosamente si la persona tiene exactamente 18 años o más', () => {
    const today = new Date();
    const exactly18 = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
    const y = exactly18.getFullYear();
    const m = String(exactly18.getMonth() + 1).padStart(2, '0');
    const d = String(exactly18.getDate()).padStart(2, '0');

    expect(validator(new FormControl(`${y}-${m}-${d}`))).toBeNull();
    expect(validator(new FormControl('1990-05-15'))).toBeNull();
  });

  it('retorna error { minAge: { requiredAge: 18 } } si la persona tiene menos de 18 años', () => {
    const today = new Date();
    const under18 = new Date(today.getFullYear() - 17, today.getMonth(), today.getDate());
    const y = under18.getFullYear();
    const m = String(under18.getMonth() + 1).padStart(2, '0');
    const d = String(under18.getDate()).padStart(2, '0');

    const result = validator(new FormControl(`${y}-${m}-${d}`));
    expect(result).toEqual({ minAge: { requiredAge: 18 } });
  });

  it('getMaxBirthDate genera correctamente la fecha límite de hace 18 años', () => {
    const maxDateStr = getMaxBirthDate(18);
    const today = new Date();
    const expectedYear = today.getFullYear() - 18;
    expect(maxDateStr.startsWith(String(expectedYear))).toBe(true);
  });
});
