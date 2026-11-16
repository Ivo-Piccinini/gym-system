import { FormControl, FormGroup } from '@angular/forms';
import { describe, it, expect } from 'vitest';
import {
  findProfessorOverlap,
  integerValidator,
  sameProfessor,
  timeRangeValidator
} from './class-form.validators';
import { GymClass } from './classes.models';

function makeClass(partial: Partial<GymClass>): GymClass {
  return {
    externalId: 'c1',
    activity: { externalId: 'a1', name: 'Spinning', description: '' },
    professor: { publicId: 'p1', firstName: 'Martin', lastName: 'Palermo' },
    dayOfWeek: 'MONDAY',
    startTime: '18:00:00',
    endTime: '19:00:00',
    capacityMax: 20,
    ...partial
  };
}

describe('timeRangeValidator', () => {
  const group = (startTime: string, endTime: string) =>
    new FormGroup({ startTime: new FormControl(startTime), endTime: new FormControl(endTime) });

  it('should accept an end time after the start time', () => {
    expect(timeRangeValidator(group('08:00', '09:00'))).toBeNull();
  });

  it('should reject an end time equal to or before the start time', () => {
    expect(timeRangeValidator(group('09:00', '09:00'))).toEqual({ timeRange: true });
    expect(timeRangeValidator(group('10:00', '09:00'))).toEqual({ timeRange: true });
  });

  it('should not complain while a time is still empty', () => {
    expect(timeRangeValidator(group('', '09:00'))).toBeNull();
  });
});

describe('integerValidator', () => {
  it('should accept integers and empty values', () => {
    expect(integerValidator(new FormControl(20))).toBeNull();
    expect(integerValidator(new FormControl(''))).toBeNull();
  });

  it('should reject decimals', () => {
    expect(integerValidator(new FormControl(2.5))).toEqual({ integer: true });
  });
});

describe('sameProfessor', () => {
  it('should compare by publicId when both have it', () => {
    expect(sameProfessor({ publicId: 'p1', firstName: 'A' }, { publicId: 'p1', firstName: 'B' })).toBe(true);
    expect(sameProfessor({ publicId: 'p1' }, { publicId: 'p2' })).toBe(false);
  });

  it('should fall back to first and last name', () => {
    expect(
      sameProfessor({ firstName: 'Martin', lastName: 'Palermo' }, { firstName: 'Martin', lastName: 'Palermo' })
    ).toBe(true);
    expect(sameProfessor({ firstName: 'Martin', lastName: 'Palermo' }, { firstName: 'Martin', lastName: 'Perez' })).toBe(false);
  });

  it('should be false when a professor is missing', () => {
    expect(sameProfessor(undefined, { publicId: 'p1' })).toBe(false);
  });
});

describe('findProfessorOverlap', () => {
  const existing = [makeClass({})]; // lunes 18:00-19:00, profesor p1
  const p1 = { publicId: 'p1', firstName: 'Martin', lastName: 'Palermo' };

  it('should detect an overlapping range for the same professor and day', () => {
    expect(findProfessorOverlap(existing, p1, 'MONDAY', '18:30', '19:30')).toBeDefined();
    expect(findProfessorOverlap(existing, p1, 'MONDAY', '17:00', '18:01')).toBeDefined();
  });

  it('should allow back-to-back classes', () => {
    expect(findProfessorOverlap(existing, p1, 'MONDAY', '19:00', '20:00')).toBeUndefined();
    expect(findProfessorOverlap(existing, p1, 'MONDAY', '17:00', '18:00')).toBeUndefined();
  });

  it('should ignore other days and other professors', () => {
    expect(findProfessorOverlap(existing, p1, 'TUESDAY', '18:00', '19:00')).toBeUndefined();
    expect(findProfessorOverlap(existing, { publicId: 'p2' }, 'MONDAY', '18:00', '19:00')).toBeUndefined();
  });

  it('should exclude the class being edited', () => {
    expect(findProfessorOverlap(existing, p1, 'MONDAY', '18:00', '19:00', 'c1')).toBeUndefined();
  });

  it('should return nothing for incomplete or invalid ranges', () => {
    expect(findProfessorOverlap(existing, p1, '', '18:00', '19:00')).toBeUndefined();
    expect(findProfessorOverlap(existing, p1, 'MONDAY', '19:00', '18:00')).toBeUndefined();
  });
});
