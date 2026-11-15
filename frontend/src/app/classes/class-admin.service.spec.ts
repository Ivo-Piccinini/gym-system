import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { ClassAdminService } from './class-admin.service';
import { environment } from '../../environments/environment';
import { ClassPayload, GymClass } from './classes.models';

describe('ClassAdminService', () => {
  let service: ClassAdminService;
  let httpTesting: HttpTestingController;

  const payload: ClassPayload = {
    activity: { externalId: 'act-1', name: 'Spinning', description: 'Ciclismo indoor' },
    professor: { publicId: 'prof-1', firstName: 'Martin', lastName: 'Palermo' },
    dayOfWeek: 'MONDAY',
    startTime: '08:00:00',
    endTime: '09:00:00',
    capacityMax: 20
  };

  const created = { externalId: 'class-1', ...payload } as GymClass;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), ClassAdminService]
    });

    service = TestBed.inject(ClassAdminService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should create a class via POST /gym-classes', () => {
    service.createClass(payload).subscribe((result) => {
      expect(result).toEqual(created);
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/gym-classes`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(created);
  });

  it('should update a class via PUT /gym-classes/{id}', () => {
    const body: ClassPayload = { ...payload, externalId: 'class-1', capacityMax: 25 };

    service.updateClass('class-1', body).subscribe();

    const req = httpTesting.expectOne(`${environment.apiUrl}/gym-classes/class-1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.externalId).toBe('class-1');
    req.flush({ ...created, capacityMax: 25 });
  });

  it('should delete a class via DELETE /gym-classes/{id}', () => {
    service.deleteClass('class-1').subscribe();

    const req = httpTesting.expectOne(`${environment.apiUrl}/gym-classes/class-1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
