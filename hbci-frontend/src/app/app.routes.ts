import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'accounts',
    loadComponent: () => import('./accounts/account-list.component').then(m => m.AccountListComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
