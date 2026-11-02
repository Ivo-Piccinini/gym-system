import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { ClassService } from './class.service';
import { environment } from '../../environments/environment';
import { GymClass } from './classes.models';

describe('ClassService', () => {
  let service: ClassService;
  let httpTesting: HttpTestingController;

  const mockClasses: GymClass[] = [
    {
      externalId: 'class-uuid-1',
      activity: { externalId: 'act-uuid-1', name: 'Crossfit', description: 'Entrenamiento funcional' },
      professor: { firstName: 'Martin' },
      dayOfWeek: 'MONDAY',
      startTime: '18:00:00',
      endTime: '19:00:00',
      capacityMax: 20,
      availableSpots: 12
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), ClassService]
    });

    service = TestBed.inject(ClassService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get classes via GET /api/gym-classes without params', () => {
    service.getClasses().subscribe((classes) => {
      expect(classes).toEqual(mockClasses);
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/gym-classes`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.has('date')).toBe(false);
    req.flush(mockClasses);
  });

  it('should send the date param when provided', () => {
    service.getClasses('2026-11-02').subscribe();

    const req = httpTesting.expectOne(
      (r) => r.url === `${environment.apiUrl}/gym-classes` && r.params.get('date') === '2026-11-02'
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockClasses);
  });
});
