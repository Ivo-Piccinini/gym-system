import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { AuditService } from './audit.service';
import { environment } from '../../../environments/environment';
import { AuditLog } from './audit.models';

describe('AuditService', () => {
  let service: AuditService;
  let httpTesting: HttpTestingController;

  const mockLogs: AuditLog[] = [
    {
      id: 1,
      action: 'UPDATE_PLAN',
      performedBy: 'admin',
      timestamp: '2026-11-05T20:00:00',
      details: '[{"name":"Pase Libre","price":25000}]'
    },
    {
      id: 2,
      action: 'PROCESS_PAYMENT',
      performedBy: 'admin',
      timestamp: '2026-11-05T19:30:00',
      details: '[{"amount":25000}]'
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), AuditService]
    });

    service = TestBed.inject(AuditService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('debe crearse correctamente', () => {
    expect(service).toBeTruthy();
  });

  it('getAuditLogs() realiza GET a /audit/logs y retorna la lista', () => {
    let result: AuditLog[] | undefined;

    service.getAuditLogs().subscribe((logs) => (result = logs));

    const req = httpTesting.expectOne(`${environment.apiUrl}/audit/logs`);
    expect(req.request.method).toBe('GET');
    req.flush(mockLogs);

    expect(result).toEqual(mockLogs);
    expect(result?.length).toBe(2);
  });
});
