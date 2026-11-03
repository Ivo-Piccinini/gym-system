import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { PaymentService } from './payment.service';
import { environment } from '../../environments/environment';
import { Payment, PaymentRequest } from './membership.models';

describe('PaymentService', () => {
  let service: PaymentService;
  let httpTesting: HttpTestingController;

  const mockPayment: Payment = {
    id: 1,
    publicId: 'pay-uuid-1',
    subscriptionId: 7,
    amount: 25000,
    paymentDate: '2026-11-03',
    method: 'TARJETA'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), PaymentService]
    });

    service = TestBed.inject(PaymentService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('createPayment hace POST a /payments con el body recibido', () => {
    const request: PaymentRequest = {
      subscriptionId: 'sub-uuid-1',
      amount: 25000,
      paymentDate: '2026-11-03',
      method: 'TARJETA'
    };
    let result: Payment | undefined;

    service.createPayment(request).subscribe((p) => (result = p));

    const req = httpTesting.expectOne(`${environment.apiUrl}/payments`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockPayment);

    expect(result).toEqual(mockPayment);
  });

  it('getMyPayments hace GET a /payments/my-payments', () => {
    let result: Payment[] | undefined;

    service.getMyPayments().subscribe((p) => (result = p));

    const req = httpTesting.expectOne(`${environment.apiUrl}/payments/my-payments`);
    expect(req.request.method).toBe('GET');
    req.flush([mockPayment]);

    expect(result).toEqual([mockPayment]);
  });
});