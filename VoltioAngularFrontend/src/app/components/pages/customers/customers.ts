import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Button } from '../../shared-components/button/button';

interface CustomerItem {
  id: string;
  name: string;
  type: string;
  totalAccounts: number;
  memberSince: string;
}

@Component({
  selector: 'app-customers',
  standalone: true,
  imports: [CommonModule, FormsModule, Button],
  templateUrl: './customers.html',
  styleUrl: './customers.css'
})
export class Customers {
  searchQuery = signal('');

  customers = signal<CustomerItem[]>([
    { id: '1', name: 'Asd', type: 'PERSON', totalAccounts: 3, memberSince: '6/9/2026' },
    { id: '2', name: 'Chodavaram Sai Bharath Reddy', type: 'PERSON', totalAccounts: 1, memberSince: '7/3/2026' },
  ]);

  filteredCustomers = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    return this.customers().filter(cust => 
      cust.name.toLowerCase().includes(query) || cust.id.includes(query)
    );
  });

  manageCustomer(customer: CustomerItem): void {
    console.log('Managing customer:', customer);
  }
}