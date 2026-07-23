import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Auth } from '../../../core/services/auth';

interface NavItem {
  label: string;
  link: string;
}

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css',
})
export class NavBar {
  private authService = inject(Auth);

  navItems = computed<NavItem[]>(() => {
    const role = this.authService.userRole();

    if (role === 'ADMIN') {
      return [
        { label: 'All Accounts', link: 'all-accounts' },
        { label: 'Customers', link: 'customers' }
      ];
    }

    if (role === 'CUSTOMER') {
      return [
        { label: 'Overview', link: 'dashboard' },
        { label: 'My Accounts', link: 'my-accounts' },
        { label: 'Transfer Funds', link: 'transfer-funds' },
        { label: 'Transactions', link: 'transactions' },
        { label: 'Monthly Statements', link: 'monthly-statements' },
        { label: 'Spending Insights', link: 'spending-insights' },
        { label: 'Standing Orders', link: 'standing-orders' }
      ];
    }

    return [];
  });
}