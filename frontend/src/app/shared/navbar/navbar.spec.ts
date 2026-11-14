import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal, WritableSignal } from '@angular/core';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { NavbarComponent } from './navbar';
import { AuthService } from '../../auth/auth.service';

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;
  let isAdminSig: WritableSignal<boolean>;
  let isProfessorSig: WritableSignal<boolean>;
  let isClientSig: WritableSignal<boolean>;
  let mockAuthService: {
    logout: ReturnType<typeof vi.fn>;
    isAdmin: () => boolean;
    isProfessor: () => boolean;
    isClient: () => boolean;
    username: WritableSignal<string>;
    isAuthenticated: WritableSignal<boolean>;
  };

  beforeEach(async () => {
    isAdminSig = signal(false);
    isProfessorSig = signal(false);
    isClientSig = signal(true);

    mockAuthService = {
      logout: vi.fn(),
      isAdmin: vi.fn(() => isAdminSig()),
      isProfessor: vi.fn(() => isProfessorSig()),
      isClient: vi.fn(() => isClientSig()),
      username: signal('admin'),
      isAuthenticated: signal(true)
    };

    await TestBed.configureTestingModule({
      imports: [NavbarComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debe crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  it('debe mostrar los botones correspondientes a CLIENTE', () => {
    isAdminSig.set(false);
    isProfessorSig.set(false);
    isClientSig.set(true);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const textContent = compiled.textContent ?? '';

    expect(component.roleDisplayName()).toBe('Portal Socio');
    expect(textContent).toContain('Clases');
    expect(textContent).toContain('Mi Rutina');
    expect(textContent).toContain('Membresías');
    expect(textContent).toContain('Pagos');
    expect(textContent).toContain('Mi Perfil');
    expect(textContent).toContain('Salir');

    expect(textContent).not.toContain('Métricas');
    expect(textContent).not.toContain('Padrón');
    expect(textContent).not.toContain('Planes');
  });

  it('debe mostrar los botones correspondientes a PROFESOR', () => {
    isAdminSig.set(false);
    isProfessorSig.set(true);
    isClientSig.set(false);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const textContent = compiled.textContent ?? '';

    expect(component.roleDisplayName()).toBe('Portal Profesor');
    expect(textContent).toContain('Clases');
    expect(textContent).toContain('Rutinas');
    expect(textContent).toContain('Mi Perfil');
    expect(textContent).toContain('Salir');

    expect(textContent).not.toContain('Mi Rutina');
    expect(textContent).not.toContain('Pagos');
    expect(textContent).not.toContain('Métricas');
    expect(textContent).not.toContain('Padrón');
    expect(textContent).not.toContain('Planes');
  });

  it('debe mostrar los botones correspondientes a ADMINISTRADOR', () => {
    isAdminSig.set(true);
    isProfessorSig.set(false);
    isClientSig.set(false);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const textContent = compiled.textContent ?? '';

    expect(component.roleDisplayName()).toBe('Panel Administrador');
    expect(textContent).toContain('Métricas');
    expect(textContent).toContain('Padrón');
    expect(textContent).toContain('Planes');
    expect(textContent).toContain('Clases');
    expect(textContent).toContain('Rutinas');
    expect(textContent).toContain('Membresías');
    expect(textContent).toContain('Mi Perfil');
    expect(textContent).toContain('Salir');

    expect(textContent).not.toContain('Mi Rutina');
    expect(textContent).not.toContain('Pagos');
  });

  it('debe ejecutar authService.logout() al hacer click en Salir', () => {
    component.onLogout();
    expect(mockAuthService.logout).toHaveBeenCalled();
  });
});
