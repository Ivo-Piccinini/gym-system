import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly isLoading = signal<boolean>(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly showPassword = signal<boolean>(false);

  protected readonly loginForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(4)]]
  });

  protected togglePasswordVisibility(): void {
    this.showPassword.update((val) => !val);
  }

  protected onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { username, password } = this.loginForm.getRawValue();

    this.authService.login({ username: username.trim(), password }).subscribe({
      next: () => {
        this.isLoading.set(false);
        const destination = this.authService.getDefaultRedirectUrl();
        this.router.navigateByUrl(destination);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 401) {
          this.errorMessage.set('Credenciales incorrectas o cuenta inhabilitada.');
        } else if (err.status === 0) {
          this.errorMessage.set('No se pudo conectar con el servidor backend (puerto 8080).');
        } else {
          this.errorMessage.set('Ocurrió un error inesperado al iniciar sesión.');
        }
      }
    });
  }
}
