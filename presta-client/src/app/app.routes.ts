import { Routes } from '@angular/router';
import { authGuard } from './services/guard/auth-guard';

export const routes: Routes = [
    // Routes publiques
    { path: '',
        loadComponent: () => import('./pages/home/home').then(m => m.HomePage) 
    },

    // Routes protégées
    {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard').then(m => m.DashboardPage),
        canActivate: [authGuard]
    },
    {
        path: 'admin',
        loadComponent: () => import('./pages/admin/admin').then(m => m.AdminPage),
        canActivate: [authGuard],
        data: { roles: ['admin'] }
    },

    // { path: 'unauthorized', loadComponent: () => import('./unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent) },
    { path: '**', redirectTo: '' }
];
