import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface AccountItem {
  id: string;
  customerId: string;
  type: string;
  status: 'ACTIVE' | 'FROZEN' | 'CLOSED';
}

@Component({
  selector: 'app-all-accounts',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './all-accounts.html',
  styleUrl: './all-accounts.css'
})
export class AllAccounts {
  searchQuery = signal('');
  selectedType = signal('ALL');

  accounts = signal<AccountItem[]>([
    { id: '#1000', customerId: '1', type: 'SAVINGS', status: 'FROZEN' },
    { id: '#1001', customerId: '1', type: 'CHECKING', status: 'ACTIVE' },
    { id: '#1002', customerId: '1', type: 'TFSA', status: 'ACTIVE' },
    { id: '#1003', customerId: '1', type: 'RRSP', status: 'ACTIVE' },
    { id: '#1004', customerId: '2', type: 'SAVINGS', status: 'ACTIVE' },
  ]);

  filteredAccounts = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const typeFilter = this.selectedType();

    return this.accounts().filter(account => {
      const matchesQuery = account.id.toLowerCase().includes(query) || account.customerId.toLowerCase().includes(query);
      const matchesType = typeFilter === 'ALL' || account.type === typeFilter;
      return matchesQuery && matchesType;
    });
  });
}