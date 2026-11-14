import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { ExerciseService } from './exercise.service';
import { environment } from '../../environments/environment';
import { Exercise } from './routines.models';

describe('ExerciseService', () => {
  let service: ExerciseService;
  let httpTesting: HttpTestingController;

  const mockExercises: Exercise[] = [
    {
      idPublic: 'ex-uuid-1',
      name: 'Press de Banca Plano',
      descripcion: 'Ejercicio clásico de empuje.',
      muscleGroup: 'PECHO'
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), ExerciseService]
    });

    service = TestBed.inject(ExerciseService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get the whole catalog via GET /api/exercises without params', () => {
    service.getExercises().subscribe((exercises) => {
      expect(exercises).toEqual(mockExercises);
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/exercises`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.has('muscleGroup')).toBe(false);
    req.flush(mockExercises);
  });

  it('should filter by muscle group using the muscleGroup query param', () => {
    service.getExercises('PECHO').subscribe();

    const req = httpTesting.expectOne(`${environment.apiUrl}/exercises?muscleGroup=PECHO`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('muscleGroup')).toBe('PECHO');
    req.flush(mockExercises);
  });
});