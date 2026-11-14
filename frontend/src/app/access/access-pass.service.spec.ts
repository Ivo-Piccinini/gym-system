import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { AccessPassService } from './access-pass.service';
import { AccessPass } from './access-pass.models';
import { environment } from '../../environments/environment';

describe('AccessPassService', () => {
  let service: AccessPassService;
  let httpTesting: HttpTestingController;

  const mockPass: AccessPass = {
    publicId: 'pass-uuid-1',
    userPublicId: 'user-uuid-1',
    clientName: 'Juan Perez',
    clientDni: '10000001',
    clientEmail: 'juan@gym.com',
    planName: 'Pase Libre Mensual',
    accessCode: 'GYM-PASS-10000001-A1B2',
    qrData: '{"code":"GYM-PASS-10000001-A1B2"}',
    qrImageBase64: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...',
    generatedBy: 'admin',
    generatedAt: '2026-11-14T20:00:00',
    validUntil: '2026-12-14',
    active: true
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AccessPassService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AccessPassService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should generate a new access pass for user', () => {
    service.generatePass('user-uuid-1').subscribe((pass) => {
      expect(pass).toEqual(mockPass);
      expect(pass.accessCode).toBe('GYM-PASS-10000001-A1B2');
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/access/generate/user-uuid-1`);
    expect(req.request.method).toBe('POST');
    req.flush(mockPass);
  });

  it('should get access pass by user publicId', () => {
    service.getPassByUser('user-uuid-1').subscribe((pass) => {
      expect(pass).toEqual(mockPass);
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/access/user/user-uuid-1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPass);
  });

  it('should get current client active pass', () => {
    service.getMyPass().subscribe((pass) => {
      expect(pass).toEqual(mockPass);
    });

    const req = httpTesting.expectOne(`${environment.apiUrl}/access/my-pass`);
    expect(req.request.method).toBe('GET');
    req.flush(mockPass);
  });
});
