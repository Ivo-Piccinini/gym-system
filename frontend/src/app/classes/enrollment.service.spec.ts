import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { EnrollmentService } from './enrollment.service';
import { environment } from '../../environments/environment';
import { Enrollment } from './classes.models';

describe('EnrollmentService', () => {
  let service: EnrollmentService;
  let httpTesting: HttpTestingController;

  const mockEnrollment = {
    externalId: 'enrollment-uuid-1',
    enrollmentDate: '2026-11-02',
    gymClass: { externalId: 'class-uuid-1' }
  } as Enrollment;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), EnrollmentService]
    });

    service = TestBed.inject(EnrollmentService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get my enrollments via GET /enrollments/my-enrollments', () => {
    service.getMyEnrollments().subscribe((list) => {
      expect(list).toEqual([mockEnrollment]);
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/enrollments/my-enrollments`);
    expect(req.request.method).toBe('GET');
    req.flush([mockEnrollment]);
  });

  it('should enroll via POST /enrollments/class/{id}', () => {
    service.enroll('class-uuid-1').subscribe((enrollment) => {
      expect(enrollment).toEqual(mockEnrollment);
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/enrollments/class/class-uuid-1`);
    expect(req.request.method).toBe('POST');
    req.flush(mockEnrollment);
  });

  it('should cancel via DELETE /enrollments/{id}', () => {
    service.cancel('enrollment-uuid-1').subscribe();

    const req = httpTesting.expectOne(`${environment.apiUrl}/enrollments/enrollment-uuid-1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
