import { Routes } from '@angular/router';
import { Home } from './pages/home/home';

export const routes: Routes = [
    {path: '', redirectTo: 'home', pathMatch: 'full'},
    {path: 'home', 
        component: Home, 
        children: [
            {path: '', redirectTo: 'dashboard', pathMatch: 'full'},
            {path: 'dashboard', loadComponent: () => import('./pages/dashboard/dashboard').then(m => m.Dashboard)
            },
            {
                path:'my-accounts', loadComponent: () => import('./pages/my-accounts/my-accounts').then(m => m.MyAccounts)
            },
            {
                path:'transfer-funds', loadComponent: () => import('./pages/transfer-funds/transfer-funds').then(m => m.TransferFunds)
            },
            {
                path:'transactions', loadComponent: () => import('./pages/transactions/transactions').then(m => m.Transactions)
            },
            {
                path: 'monthly-statements', loadComponent: () => import('./pages/monthly-statements/monthly-statements').then(m => m.MonthlyStatements)
            },
            {
                path: 'spending-insights', loadComponent: () => import('./pages/spending-insights/spending-insights').then(m => m.SpendingInsights)
            },
            {
                path: 'standing-orders', loadComponent: () => import('./pages/standing-orders/standing-orders').then(m => m.StandingOrders)
            }
        ]
    },
];