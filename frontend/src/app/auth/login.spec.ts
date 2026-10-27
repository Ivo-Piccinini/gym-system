import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Login } from './login';

describe('Login', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    }).compileComponents();
  });

  it('should create the component with invalid initial form', () => {
    const fixture = TestBed.createComponent(Login);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
    expect(component['loginForm'].valid).toBe(false);
  });

  it('should toggle password visibility', () => {
    const fixture = TestBed.createComponent(Login);
    const component = fixture.componentInstance;
    expect(component['showPassword']()).toBe(false);
    component['togglePasswordVisibility']();
    expect(component['showPassword']()).toBe(true);
  });
});
