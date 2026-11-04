import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.html',
  styleUrl: './layout.css'
})
export class Layout {
  readonly authService = inject(AuthService);
  readonly menuOpen = signal(false);

  toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  onLogout(): void {
    this.closeMenu();
    this.authService.logout();
  }

  getRoleDisplayName(): string {
    if (this.authService.isAdmin()) {
      return 'Panel Administrador';
    }
    if (this.authService.isProfessor()) {
      return 'Portal Profesor';
    }
    if (this.authService.isClient()) {
      return 'Portal Socio';
    }
    return 'Gimnasio';
  }
}

