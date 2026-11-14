import { Component, inject, computed } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent {
  readonly authService = inject(AuthService);

  readonly isAdmin = computed(() => {
    return typeof this.authService.isAdmin === 'function' ? this.authService.isAdmin() : false;
  });

  readonly isProfessor = computed(() => {
    return typeof this.authService.isProfessor === 'function' ? this.authService.isProfessor() : false;
  });

  readonly isClient = computed(() => {
    return typeof this.authService.isClient === 'function' ? this.authService.isClient() : false;
  });

  readonly roleDisplayName = computed(() => {
    if (this.isAdmin()) {
      return 'Panel Administrador';
    }
    if (this.isProfessor()) {
      return 'Portal Profesor';
    }
    if (this.isClient()) {
      return 'Portal Socio';
    }
    return 'Gimnasio';
  });

  onLogout(): void {
    if (typeof this.authService.logout === 'function') {
      this.authService.logout();
    }
  }
}
