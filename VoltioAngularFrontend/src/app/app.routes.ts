import { Routes } from '@angular/router';
import { Home } from './Components/home/home';

export const routes: Routes = [
    {path: '', redirectTo: 'home', pathMatch: 'full'},
    {
        path: 'login', loadComponent: () => import('./Components/login/login').then(m => m.Login)
    },
    {
        path: 'register', loadComponent: () => import('./Components/register/register').then(m => m.Register)
    },
    {path: 'home', 
        component: Home, 
        children: [
            {path: '', redirectTo: 'dashboard', pathMatch: 'full'},
            {path: 'dashboard', loadComponent: () => import('./Components/dashboard/dashboard').then(m => m.Dashboard)
            },
            {
                path:'my-accounts', loadComponent: () => import('./Components/my-accounts/my-accounts').then(m => m.MyAccounts)
            },
            {
                path:'transfer-funds', loadComponent: () => import('./Components/transfer-funds/transfer-funds').then(m => m.TransferFunds)
            },
            {
                path:'transactions', loadComponent: () => import('./Components/transactions/transactions').then(m => m.Transactions)
            },
            {
                path: 'monthly-statements', loadComponent: () => import('./Components/monthly-statements/monthly-statements').then(m => m.MonthlyStatements)
            },
            {
                path: 'spending-insights', loadComponent: () => import('./Components/spending-insights/spending-insights').then(m => m.SpendingInsights)
            },
            {
                path: 'standing-orders', loadComponent: () => import('./Components/standing-orders/standing-orders').then(m => m.StandingOrders)
            }
        ]
    },
];