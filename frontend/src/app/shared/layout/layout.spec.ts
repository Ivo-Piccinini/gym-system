import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { Layout } from './layout';
import { AuthService } from '../../auth/auth.service';

describe('Layout', () => {
  let component: Layout;
  let fixture: ComponentFixture<Layout>;
  let mockAuthService: {
    logout: ReturnType<typeof vi.fn>;
    isAdmin: ReturnType<typeof vi.fn>;
    isProfessor: ReturnType<typeof vi.fn>;
    isClient: ReturnType<typeof vi.fn>;
    username: ReturnType<typeof signal<string>>;
    isAuthenticated: ReturnType<typeof signal<boolean>>;
  };

  beforeEach(async () => {
    mockAuthService = {
      logout: vi.fn(),
      isAdmin: vi.fn(),
      isProfessor: vi.fn(),
      isClient: vi.fn(),
      username: signal('testuser'),
      isAuthenticated: signal(true)
    };

    mockAuthService.isAdmin.mockReturnValue(false);
    mockAuthService.isProfessor.mockReturnValue(false);
    mockAuthService.isClient.mockReturnValue(true);

    await TestBed.configureTestingModule({
      imports: [Layout],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Layout);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debe crear el componente layout shell', () => {
    expect(component).toBeTruthy();
  });

  it('debe alternar el menu responsivo con toggleMenu y cerrarlo con closeMenu', () => {
    expect(component.menuOpen()).toBe(false);
    component.toggleMenu();
    expect(component.menuOpen()).toBe(true);
    component.closeMenu();
    expect(component.menuOpen()).toBe(false);
  });

  it('debe invocar authService.logout() al llamar onLogout()', () => {
    component.menuOpen.set(true);
    component.onLogout();
    expect(mockAuthService.logout).toHaveBeenCalled();
    expect(component.menuOpen()).toBe(false);
  });

  it('debe mostrar el nombre de rol correspondiente segun permisos', () => {
    expect(component.getRoleDisplayName()).toBe('Portal Socio');

    mockAuthService.isClient.mockReturnValue(false);
    mockAuthService.isProfessor.mockReturnValue(true);
    expect(component.getRoleDisplayName()).toBe('Portal Profesor');

    mockAuthService.isProfessor.mockReturnValue(false);
    mockAuthService.isAdmin.mockReturnValue(true);
    expect(component.getRoleDisplayName()).toBe('Panel Administrador');

    mockAuthService.isAdmin.mockReturnValue(false);
    expect(component.getRoleDisplayName()).toBe('Gimnasio');
  });
});
