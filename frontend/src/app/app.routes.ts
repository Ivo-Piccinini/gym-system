import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { adminGuard } from './auth/admin.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./auth/login').then((m) => m.Login)
  },
  {
    path: 'perfil',
    canActivate: [authGuard],
    loadComponent: () => import('./profile/profile').then((m) => m.Profile)
  },
  {
    path: 'admin/usuarios',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./admin/users/users-management').then((m) => m.UsersManagement)
  },
  {
    path: 'admin/users',
    redirectTo: 'admin/usuarios',
    pathMatch: 'full'
  },
  {
    path: 'admin/dashboard',
    redirectTo: 'admin/usuarios',
    pathMatch: 'full'
  },
  {
    path: 'mi-entrenamiento',
    redirectTo: 'perfil',
    pathMatch: 'full'
  },
  {
    path: 'profesor/rutinas',
    redirectTo: 'perfil',
    pathMatch: 'full'
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];
