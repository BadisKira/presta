import { Routes } from '@angular/router';
import { authGuard } from './services/guard/auth-guard';

export const routes: Routes = [
    // Routes publiques
    {
        path: '',
        //loadComponent: () => import('./pages/landing/landing').then(m => m.LandingPage),
        loadComponent: () => import('./pages/admin/admin').then(m => m.AdminPage),

    },

    // Routes protégées
    // {
    //     path: 'dashboard',
    //     loadComponent: () => import('./pages/landing/landing').then(m => m.LandingPage),
    //     canActivate: [authGuard]
    // },
    // {
    //     path: 'admin',
    //     //loadComponent: () => import('./pages/admin/admin').then(m => m.AdminPage),
    //     // canActivate: [authGuard],
    //     // data: { roles: ['admin'] }
    // },

    //{ path: 'notfound', component: Notfound },
    { path: '**', redirectTo: '/notfound' }
    // { path: 'unauthorized', loadComponent: () => import('./unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent) },

];


/**
 
export const appRoutes: Routes = [
    {
        path: '',
        component: AppLayout,
        children: [
            { path: '', component: Dashboard },
            { path: 'uikit', loadChildren: () => import('./app/pages/uikit/uikit.routes') },
            { path: 'documentation', component: Documentation },
            { path: 'pages', loadChildren: () => import('./app/pages/pages.routes') }
        ]
    },
    { path: 'landing', component: Landing },
    { path: 'notfound', component: Notfound },
    { path: 'auth', loadChildren: () => import('./app/pages/auth/auth.routes') },
    
];
 */