import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { RoutineService } from './routine.service';
import { environment } from '../../environments/environment';
import { Routine, RoutinePayload } from './routines.models';

describe('RoutineService', () => {
  let service: RoutineService;
  let httpTesting: HttpTestingController;

  const payload: RoutinePayload = {
    name: 'Hipertrofia tren superior',
    clientId: 'client-1',
    professorId: 'prof-1',
    typeRoutine: 'HYPERTROFY',
    startDate: '2026-11-16',
    endDate: null,
    exercises: [
      { exerciseId: 'ex-1', series: 4, reps: 10, weight: 60 },
      { exerciseId: 'ex-2', series: 3, reps: 12, weight: 0 }
    ]
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), RoutineService]
    });
    service = TestBed.inject(RoutineService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should create the routine and then one full-routine per exercise', () => {
    service.createRoutine(payload).subscribe();

    const routineReq = httpTesting.expectOne(`${environment.apiUrl}/routines`);
    expect(routineReq.request.method).toBe('POST');
    expect(routineReq.request.body.startDate).toBe('2026-11-16T12:00:00');
    routineReq.flush({ publicId: 'routine-1' });

    const lines = httpTesting.match(`${environment.apiUrl}/full-routines`);
    expect(lines.length).toBe(2);
    expect(lines[0].request.body).toEqual({
      routineID: 'routine-1',
      exerciseID: 'ex-1',
      series: 4,
      reps: 10,
      weight: 60
    });
    lines.forEach((l) => l.flush({}));
  });

  it('should hide a deleted routine after disabling its lines', () => {
    const routine = { publicId: 'routine-1', exercises: [{ publicId: 'line-1' }] } as Routine;
    service.deleteRoutine(routine).subscribe();

    const del = httpTesting.expectOne(`${environment.apiUrl}/full-routines/line-1`);
    expect(del.request.method).toBe('DELETE');
    del.flush(null);

    service.getRoutines().subscribe((list) => expect(list).toEqual([]));
    httpTesting.expectOne(`${environment.apiUrl}/routines`).flush([routine]);
  });
});