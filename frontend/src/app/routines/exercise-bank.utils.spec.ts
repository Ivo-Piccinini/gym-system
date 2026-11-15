import { FormControl } from '@angular/forms';
import { describe, it, expect } from 'vitest';
import { findDuplicateName, normalizeText } from './exercise-bank.utils';
import { trimmedMinLength } from './exercise-form.validators';
import { Exercise } from './routines.models';

const exercises: Exercise[] = [
  { idPublic: 'e1', name: 'Press de Banca', descripcion: 'Empuje horizontal', muscleGroup: 'PECHO' },
  { idPublic: 'e2', name: 'Curl de Bíceps', descripcion: 'Flexión de codo', muscleGroup: 'BICEPS' }
];

describe('normalizeText', () => {
  it('should ignore case and accents', () => {
    expect(normalizeText('  Bíceps ')).toBe('biceps');
    expect(normalizeText('PRESS')).toBe('press');
    expect(normalizeText(null)).toBe('');
  });
});

describe('findDuplicateName', () => {
  it('should find an existing name ignoring case and accents', () => {
    expect(findDuplicateName(exercises, 'press de banca')?.idPublic).toBe('e1');
    expect(findDuplicateName(exercises, 'CURL DE BICEPS')?.idPublic).toBe('e2');
  });

  it('should ignore the exercise being edited', () => {
    expect(findDuplicateName(exercises, 'Press de Banca', 'e1')).toBeUndefined();
  });

  it('should return nothing for empty or new names', () => {
    expect(findDuplicateName(exercises, '')).toBeUndefined();
    expect(findDuplicateName(exercises, 'Remo con barra')).toBeUndefined();
  });
});

describe('trimmedMinLength', () => {
  const validator = trimmedMinLength(3);

  it('should accept text long enough and leave empty values to required', () => {
    expect(validator(new FormControl('Pecho'))).toBeNull();
    expect(validator(new FormControl(''))).toBeNull();
  });

  it('should reject text that is too short once trimmed', () => {
    expect(validator(new FormControl('ab'))).not.toBeNull();
    expect(validator(new FormControl('   '))).not.toBeNull();
    expect(validator(new FormControl('ab    '))).not.toBeNull();
  });
});
