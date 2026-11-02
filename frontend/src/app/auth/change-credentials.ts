import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { UserService } from './user.service';

function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const newPassword = control.get('newPassword')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  if (!newPassword || !confirmPassword) {
    return null;
  }
  return newPassword === confirmPassword ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-change-credentials',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change-credentials.html',
  styleUrls: ['./change-credentials.css']
})
export class ChangeCredentials {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);

  protected readonly isLoading = signal<boolean>(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  protected readonly currentUsername = this.authService.username;

  protected readonly credentialsForm = this.fb.nonNullable.group(
    {
      oldPassword: ['', [Validators.required, Validators.minLength(4)]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
      newUsername: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(20),
          Validators.pattern(/^[a-zA-Z0-9._-]+$/)
        ]
      ]
    },
    { validators: passwordsMatchValidator }
  );

  protected logout(): void {
    this.authService.logout();
  }

  protected onSubmit(): void {
    if (this.credentialsForm.invalid) {
      this.credentialsForm.markAllAsTouched();
      return;
    }

    const { oldPassword, newPassword, confirmPassword, newUsername } =
      this.credentialsForm.getRawValue();

    if (newUsername.trim().toLowerCase() === this.currentUsername().toLowerCase()) {
      this.errorMessage.set('El nuevo nombre de usuario debe ser diferente al actual.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.userService
      .changePassword({
        oldPassword,
        newPassword,
        confirmPassword
      })
      .subscribe({
        next: () => {
          this.userService
            .changeUsername({
              newUsername: newUsername.trim()
            })
            .subscribe({
              next: (authResponse) => {
                this.isLoading.set(false);
                this.successMessage.set(
                  '¡Credenciales actualizadas exitosamente! Redirigiendo a tu cuenta...'
                );
                this.authService.clearPasswordChangeRequirement(
                  authResponse.token,
                  newUsername.trim()
                );
                setTimeout(() => {
                  this.router.navigateByUrl(this.authService.getDefaultRedirectUrl());
                }, 1500);
              },
              error: (userErr) => {
                this.isLoading.set(false);
                if (userErr.status === 409 || userErr.error?.message?.includes('en uso')) {
                  this.errorMessage.set(
                    `La contraseña se actualizó, pero el nombre de usuario "${newUsername}" ya está en uso. Por favor elija otro nombre de usuario.`
                  );
                } else if (userErr.error?.message) {
                  this.errorMessage.set(userErr.error.message);
                } else {
                  this.errorMessage.set(
                    'La contraseña se cambió correctamente, pero ocurrió un error al actualizar el nombre de usuario.'
                  );
                }
              }
            });
        },
        error: (passErr) => {
          this.isLoading.set(false);
          if (passErr.status === 400 || passErr.error?.message?.includes('incorrecta')) {
            this.errorMessage.set(
              passErr.error?.message || 'La contraseña actual ingresada es incorrecta.'
            );
          } else if (passErr.error?.message) {
            this.errorMessage.set(passErr.error.message);
          } else {
            this.errorMessage.set(
              'No fue posible cambiar la contraseña. Verifique los datos ingresados.'
            );
          }
        }
      });
  }
}

