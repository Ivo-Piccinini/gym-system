import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';

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
    path: 'mi-entrenamiento',
    redirectTo: 'perfil',
    pathMatch: 'full'
  },
  {
    path: 'admin/dashboard',
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
