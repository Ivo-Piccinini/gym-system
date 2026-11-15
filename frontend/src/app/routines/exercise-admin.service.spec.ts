import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { ExerciseAdminService } from './exercise-admin.service';
import { environment } from '../../environments/environment';
import { ExercisePayload } from './exercise-bank.models';
import { Exercise } from './routines.models';

describe('ExerciseAdminService', () => {
  let service: ExerciseAdminService;
  let httpTesting: HttpTestingController;

  const payload: ExercisePayload = {
    name: 'Press de Banca Plano',
    descripcion: 'Ejercicio clásico de empuje horizontal.',
    muscle_group: 'PECHO',
    enabled: true
  };

  const created: Exercise = {
    idPublic: 'ex-1',
    name: payload.name,
    descripcion: payload.descripcion,
    muscleGroup: 'PECHO'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), ExerciseAdminService]
    });

    service = TestBed.inject(ExerciseAdminService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should create an exercise via POST /exercises', () => {
    service.createExercise(payload).subscribe((result) => {
      expect(result).toEqual(created);
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/exercises`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(created);
  });

  it('should update an exercise via PUT /exercises/{id}', () => {
    service.updateExercise('ex-1', payload).subscribe();

    const req = httpTesting.expectOne(`${environment.apiUrl}/exercises/ex-1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.enabled).toBe(true);
    req.flush(created);
  });

  it('should delete an exercise via DELETE /exercises/{id}', () => {
    service.deleteExercise('ex-1').subscribe();

    const req = httpTesting.expectOne(`${environment.apiUrl}/exercises/ex-1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
