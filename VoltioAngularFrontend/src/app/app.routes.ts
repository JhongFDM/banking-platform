import { Routes } from '@angular/router';
import { Home } from './pages/home/home';

export const routes: Routes = [
    { path: '', redirectTo: 'home', pathMatch: 'full' },
    {
        path: 'home',
        component: Home,
        children: [
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
            {
                path: 'dashboard', loadComponent: () => import('./pages/dashboard/dashboard').then(m => m.Dashboard)
            },
            {
                path: 'my-accounts', loadComponent: () => import('./pages/my-accounts/my-accounts').then(m => m.MyAccounts)
            },
            {
                path: 'transfer-funds', loadComponent: () => import('./pages/transfer-funds/transfer-funds').then(m => m.TransferFunds)
            },
            {
                path: 'transactions', loadComponent: () => import('./pages/transactions/transactions').then(m => m.Transactions)
            },
            {
                path: 'monthly-statements', loadComponent: () => import('./pages/monthly-statements/monthly-statements').then(m => m.MonthlyStatements)
            },
            {
                path: 'spending-insights', loadComponent: () => import('./pages/spending-insights/spending-insights').then(m => m.SpendingInsights)
            },
            {
                path: 'standing-orders', loadComponent: () => import('./pages/standing-orders/standing-orders').then(m => m.StandingOrders)
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