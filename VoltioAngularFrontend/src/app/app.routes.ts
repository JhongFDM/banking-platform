import { Routes } from '@angular/router';
import { Home } from './components/pages/home/home';
import { adminGuard } from './core/guards/admin-guard';
import { defaultRouteGuard } from './core/guards/default-route-guard';

export const routes: Routes = [
    { path: '', redirectTo: 'home', pathMatch: 'full' },
    {
        path: 'home',
        component: Home,
        children: [
            { 
                path: '', 
                canActivate: [defaultRouteGuard],
                children: [] 
            },
            { 
                path: 'profile', loadComponent: () => import('./components/pages/profile/profile').then(m => m.Profile) 
            },
            {
                path: 'dashboard', loadComponent: () => import('./components/pages/dashboard/dashboard').then(m => m.Dashboard)
            },
            {
                path:'my-accounts', loadComponent: () => import('./components/pages/my-accounts/my-accounts').then(m => m.MyAccounts)
            },
            {
                path:'transfer-funds', loadComponent: () => import('./components/pages/transfer-funds/transfer-funds').then(m => m.TransferFunds)
            },
            {
                path:'transactions', loadComponent: () => import('./components/pages/transactions/transactions').then(m => m.Transactions)
            },
            {
                path: 'monthly-statements', loadComponent: () => import('./components/pages/monthly-statements/monthly-statements').then(m => m.MonthlyStatements)
            },
            {
                path: 'spending-insights', loadComponent: () => import('./components/pages/spending-insights/spending-insights').then(m => m.SpendingInsights)
            },
            {
                path: 'standing-orders', loadComponent: () => import('./components/pages/standing-orders/standing-orders').then(m => m.StandingOrders)
            },
            { 
                path: 'all-accounts', 
                loadComponent: () => import('./components/pages/all-accounts/all-accounts').then(m => m.AllAccounts),
                canActivate: [adminGuard]
            },
            { 
                path: 'customers', 
                loadComponent: () => import('./components/pages/customers/customers').then(m => m.Customers),
                canActivate: [adminGuard]
            },
            {
                path: 'customer-detail', loadComponent: () => import('./pages/customer-detail/customer-detail').then(m => m.CustomerDetail)
            },
            {
                path: 'customer-profile', loadComponent: () => import('./pages/customer-profile/customer-profile').then(m => m.CustomerProfile)
            },
            {
                path: 'customer-edit', loadComponent: () => import('./pages/customer-edit/customer-edit').then(m => m.CustomerEdit)
            },
            {
                path: 'customer-create', loadComponent: () => import('./pages/customer-create/customer-create').then(m => m.CustomerCreate)
            },
        ]
    },
];