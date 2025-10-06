import { Routes } from '@angular/router';
import { authGuard } from './services/guard/auth-guard';

export const routes: Routes = [
    // Routes publiques
    {
        path: '',
        loadComponent: () => import('./pages/landing/landing').then(m => m.LandingPage),
    },
    {
        path: 'search',
        loadComponent: () => import('./pages/contractor/contractor-search/contractor-search').then(m => m.ContractorSearchPage),
    },
    {
        path: 'admin',
        loadComponent: () => import('./pages/admin/admin').then(m => m.AdminLayout),
        // canActivate: [authGuard],
        // data: { roles: ['ADMIN'] },
        children: [
            {
                path: 'service',
                loadComponent: () => import('./pages/admin/service.page/service.page').then(m => m.ServicePage)
            },
            {
                path: 'client',
                loadComponent: () => import('./pages/admin/client.page/client.page').then(m => m.ClientPage)
            },
             {
                path: 'contractor',
                loadComponent: () => import('./pages/admin/contractor.page/contractor.page').then(m => m.ContractorPage)
            }
        ]
    },
    {
        path: 'contractor',
        loadComponent: () => import('./pages/contractor/contractor').then(m => m.ContractorLayout),
        //canActivate: [authGuard],
        children: [
            {
                path: 'complete-profile',
                loadComponent: () => import('./pages/contractor/contractor-fill-information/contractor-fill-information').then(m => m.ContractorFillInformation)
            } , //ProfilePage
            {
                path: 'profile',
                loadComponent: () => import('./pages/contractor/profile.page/profile.page').then(m => m.ProfilePage)
            } 
        ]
    },
    {
        path: 'client',
        loadComponent: () => import('./pages/client/client').then(m => m.ClientLayout),
        canActivate: [authGuard],
        children: [

        ]
    },

    { path: 'unauthorized', loadComponent: () => import('./pages/unauthorized/unauthorized').then(m => m.UnauthorizedPage) },
    { path: 'notfound', loadComponent: () => import('./pages/notfound/notfound').then(m => m.NotfoundPage) },


    //{ path: 'notfound', component: Notfound },
    { path: '**', redirectTo: '/notfound' },

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