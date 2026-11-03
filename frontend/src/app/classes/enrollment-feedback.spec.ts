import { describe, it, expect } from 'vitest';
import { feedbackForCancelError, feedbackForEnrollmentError } from './enrollment-feedback';

describe('enrollment-feedback', () => {
  it('should map 403 to an inactive membership warning', () => {
    const fb = feedbackForEnrollmentError({ status: 403 });
    expect(fb.kind).toBe('warning');
    expect(fb.title).toBe('Membresía inactiva');
  });

  it('should map 409 to a class full warning', () => {
    const fb = feedbackForEnrollmentError({ status: 409 });
    expect(fb.kind).toBe('warning');
    expect(fb.title).toBe('Clase sin cupo');
  });

  it('should map status 0 to a connection error', () => {
    const fb = feedbackForEnrollmentError({ status: 0 });
    expect(fb.kind).toBe('error');
    expect(fb.title).toBe('Sin conexión');
  });

  it('should use the server message for other errors when present', () => {
    const fb = feedbackForEnrollmentError({ status: 400, error: { message: 'La clase ha alcanzado su capacidad' } });
    expect(fb.kind).toBe('error');
    expect(fb.message).toBe('La clase ha alcanzado su capacidad');
  });

  it('should fall back to a generic message when the server sends none', () => {
    const fb = feedbackForEnrollmentError({ status: 500 });
    expect(fb.message).toContain('error inesperado');
  });
//
  it('should build a cancel error with the server message', () => {
    const fb = feedbackForCancelError({ status: 400, error: { message: 'No te pertenece' } });
    expect(fb.title).toBe('No se pudo cancelar');
    expect(fb.message).toBe('No te pertenece');
  });
}); 
