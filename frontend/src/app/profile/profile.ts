import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ProfileService } from './profile.service';
import { AuthService } from '../auth/auth.service';
import { UserProfile } from './profile.models';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css']
})
export class Profile implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly profileService = inject(ProfileService);
  protected readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly isLoading = signal<boolean>(true);
  protected readonly isSaving = signal<boolean>(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly userProfile = signal<UserProfile | null>(null);

  protected readonly profileForm = this.fb.group({
    phone: [
      '',
      [
        Validators.pattern(/^[0-9+\-\s]{7,15}$/)
      ]
    ],
    birthDate: ['', [Validators.required]]
  });

  ngOnInit(): void {
    this.loadProfile();
  }

  protected loadProfile(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.profileService.getProfile().subscribe({
      next: (profile) => {
        this.userProfile.set(profile);
        this.populateForm(profile);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 401) {
          this.authService.logout();
        } else {
          this.errorMessage.set(
            'No se pudo cargar la información del perfil. Por favor, reintente más tarde.'
          );
        }
      }
    });
  }

  private populateForm(profile: UserProfile): void {
    this.profileForm.reset({
      phone: profile.phone ?? '',
      birthDate: profile.birthDate ?? ''
    });
  }

  protected onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.isSaving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const formVal = this.profileForm.getRawValue();
    const phone = formVal.phone ? formVal.phone.trim() : '';
    const birthDate = formVal.birthDate || undefined;

    this.profileService
      .updateProfile({
        phone: phone || undefined,
        birthDay: birthDate,
        birthDate: birthDate
      })
      .subscribe({
        next: (updatedProfile) => {
          this.isSaving.set(false);
          this.userProfile.set(updatedProfile);
          this.populateForm(updatedProfile);
          this.successMessage.set('Tus datos personales fueron actualizados correctamente.');

          setTimeout(() => {
            if (this.successMessage()) {
              this.successMessage.set(null);
            }
          }, 4000);
        },
        error: (err) => {
          this.isSaving.set(false);
          if (err.status === 400) {
            this.errorMessage.set(
              'Los datos provistos son inválidos. Revisa el teléfono y la fecha de nacimiento.'
            );
          } else {
            this.errorMessage.set(
              'Ocurrió un error al intentar actualizar los datos personales.'
            );
          }
        }
      });
  }

  protected resetForm(): void {
    const profile = this.userProfile();
    if (profile) {
      this.populateForm(profile);
      this.errorMessage.set(null);
      this.successMessage.set(null);
    }
  }

  protected getRoleBadge(role?: string): string {
    switch (role) {
      case 'ROLE_ADMIN':
        return 'Administrador';
      case 'ROLE_PROFESSOR':
        return 'Profesor';
      case 'ROLE_CLIENT':
        return 'Socio / Cliente';
      default:
        return 'Usuario';
    }
  }

  protected getInitials(firstName?: string, lastName?: string): string {
    const f = firstName?.trim().charAt(0).toUpperCase() ?? '';
    const l = lastName?.trim().charAt(0).toUpperCase() ?? '';
    return `${f}${l}` || 'U';
  }

  protected getMaxBirthDate(): string {
    const today = new Date();
    today.setFullYear(today.getFullYear() - 10);
    return today.toISOString().split('T')[0];
  }
}
