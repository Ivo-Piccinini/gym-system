import { describe, it, expect } from 'vitest';
import { addMinutes, formatDuration, formatMinutes, toMinutes } from './class-time.utils';

describe('class-time.utils', () => {
  it('should convert between "HH:mm" and minutes', () => {
    expect(toMinutes('18:30')).toBe(1110);
    expect(toMinutes('08:00:00')).toBe(480);
    expect(formatMinutes(1110)).toBe('18:30');
    expect(formatMinutes(0)).toBe('00:00');
  });

  it('should add minutes and return null past midnight', () => {
    expect(addMinutes('18:00', 60)).toBe('19:00');
    expect(addMinutes('08:30', 90)).toBe('10:00');
    expect(addMinutes('23:45', 60)).toBeNull();
  });

  it('should format durations', () => {
    expect(formatDuration('18:00', '19:30')).toBe('1 h 30 min');
    expect(formatDuration('08:00', '08:45')).toBe('45 min');
    expect(formatDuration('10:00', '12:00')).toBe('2 h');
    expect(formatDuration('10:00', '09:00')).toBeNull();
  });
});
