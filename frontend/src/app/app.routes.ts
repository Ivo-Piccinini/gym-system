import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { adminGuard } from './auth/admin.guard';
import { changeCredentialsGuard } from './auth/change-credentials.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./auth/login').then((m) => m.Login)
  },
  {
    path: 'cambiar-credenciales',
    canActivate: [changeCredentialsGuard],
    loadComponent: () =>
      import('./auth/change-credentials').then((m) => m.ChangeCredentials)
  },
  {
    path: 'perfil',
    canActivate: [authGuard],
    loadComponent: () => import('./profile/profile').then((m) => m.Profile)
  },
  {
  path: 'clases',
  canActivate: [authGuard],
  loadComponent: () => import('./classes/class-agenda').then((m) => m.ClassAgenda)
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
