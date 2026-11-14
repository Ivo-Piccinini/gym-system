import { ComponentFixture, TestBed } from '@angular/core/testing';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { AccessCredentialModal } from './access-credential-modal';
import { AccessPass } from './access-pass.models';

describe('AccessCredentialModal Component', () => {
  let component: AccessCredentialModal;
  let fixture: ComponentFixture<AccessCredentialModal>;

  const mockPass: AccessPass = {
    publicId: 'pass-uuid-1',
    userPublicId: 'user-uuid-1',
    clientName: 'Juan Perez',
    clientDni: '10000001',
    clientEmail: 'juan@gym.com',
    planName: 'Plan Trimestral',
    accessCode: 'GYM-PASS-10000001-XYZ1',
    qrData: '{"code":"GYM-PASS-10000001-XYZ1"}',
    qrImageBase64: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...',
    generatedBy: 'admin',
    generatedAt: '2026-11-14T19:00:00',
    validUntil: '2027-02-12',
    active: true
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccessCredentialModal]
    }).compileComponents();

    fixture = TestBed.createComponent(AccessCredentialModal);
    component = fixture.componentInstance;
    component.pass = mockPass;
    fixture.detectChanges();
  });

  it('should render pass information correctly', () => {
    expect(component).toBeTruthy();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.client-name')?.textContent).toContain('Juan Perez');
    expect(compiled.querySelector('.code-value')?.textContent).toContain('GYM-PASS-10000001-XYZ1');
    expect(compiled.querySelector('.plan-name')?.textContent).toContain('Plan Trimestral');
  });

  it('should emit close event when close button or backdrop is clicked', () => {
    const closeSpy = vi.fn();
    component.close.subscribe(closeSpy);

    component.onClose();
    expect(closeSpy).toHaveBeenCalled();
  });

  it('should emit regenerate event when onRegenerate is called', () => {
    const regSpy = vi.fn();
    component.regenerate.subscribe(regSpy);

    component.onRegenerate();
    expect(regSpy).toHaveBeenCalled();
  });

  it('should trigger window.print on print', () => {
    const printSpy = vi.spyOn(window, 'print').mockImplementation(() => {});
    component.print();
    expect(printSpy).toHaveBeenCalled();
    printSpy.mockRestore();
  });
});
