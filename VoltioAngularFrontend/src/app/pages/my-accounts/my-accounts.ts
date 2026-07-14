import { Component, OnInit, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-my-accounts',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './my-accounts.html',
  styleUrl: './my-accounts.css',
})
export class MyAccounts implements OnInit{

  //get current customer id

  //Connect to the accounts service to get all accounts for customer with id

  //ngOnInit, display a card with interior cards of each of the accounts that belongs to the customer

  // 1. App Configuration / Context Flags
  isAdmin = true; // Toggle true/false to see the UI shift instantly!
  private MOCK_KYC_VERIFIED = true;

  // 2. Mock Databases (Replaces Server Responses)
  accounts = signal([
    { accountId: 'ACC-1001', accountType: 'CHECKING', balance: '2450.75', accountNumber: '123456781001', status: 'ACTIVE' },
    { accountId: 'ACC-2002', accountType: 'SAVINGS', balance: '18500.20', accountNumber: '123456782002', status: 'ACTIVE' },
    { accountId: 'ACC-3003', accountType: 'TFSA', balance: '7000.00', accountNumber: '123456783003', status: 'FROZEN' }
  ]);

  customer = signal({
    customerId: 'CUST-88',
    name: 'Alex Mercer',
    dateOfBirth: '1995-04-15'
  });

  customerList = signal([
    { customerId: 'CUST-88', name: 'Alex Mercer' },
    { customerId: 'CUST-92', name: 'Sam Taylor' },
    { customerId: 'CUST-14', name: 'Morgan Reed' }
  ]);

  interestRates = signal({
    TFSA: 4.25,
    RRSP: 5.00
  });

  goals = signal([
    { goal_id: 'G-1', account_id: 'ACC-2002', name: 'New Car Fund', target: 25000, current: 18500 }
  ]);

  // 3. UI Messaging / Loading States
  isLoadingAccounts = signal(false);
  goalsLoading = signal(false);
  error = signal<{ message: string } | null>(null);
  actionMessage = signal<string | null>(null);
  goalActionMessage = signal<string | null>(null);

  // 4. Modal Interactivity Triggers
  isCreateModalOpen = false;
  isGoalCreationOpen = false;
  goalCreationAccountId: string | null = null;
  editingGoal: any = null;

  // 5. Form Backing Model State
  formState = { accountType: 'SAVINGS', balance: '0.00', interestRate: '2.5' };

  // 6. Reactive Computed Properties
  totalBalance = computed(() => {
    return this.accounts().reduce((sum, acc) => sum + (parseFloat(acc.balance) || 0), 0);
  });

  existingTypes = computed(() => {
    return new Set(this.accounts().map(a => a.accountType));
  });

  goalsByAccountId = computed(() => {
    return this.goals().reduce((map, goal) => {
      map[goal.account_id] = goal;
      return map;
    }, {} as any);
  });

  // UI Evaluation Helpers
  showInterestRate() {
    return this.formState.accountType === 'SAVINGS' || (this.formState.accountType === 'TFSA' && this.isAdmin);
  }
  showBalance() {
    return this.formState.accountType !== 'RRSP' && (this.formState.accountType !== 'TFSA' || this.isAdmin);
  }
  get tfsaRate() { return this.interestRates()?.TFSA ?? null; }
  get rrspRate() { return this.interestRates()?.RRSP ?? null; }
  get isTfsa() { return this.formState.accountType === 'TFSA'; }
  
  get isTooYoung() {
    if (!this.isTfsa) return false;
    const age = this.calculateAge(this.customer()?.dateOfBirth);
    return age !== null && age < 18;
  }
  get isKycBlocked() {
    return this.isTfsa && !this.MOCK_KYC_VERIFIED;
  }

  ngOnInit() {
    // Component mounts cleanly with standard state
  }

  // Local Mock Actions (Manipulates local signal lists to simulate backend responses)
  handleCreateAccount(event: Event) {
    event.preventDefault();
    this.error.set(null);

    if (this.existingTypes().has(this.formState.accountType)) {
      this.error.set({ message: `You already have an active ${this.formState.accountType} account.` });
      return;
    }

    const mockId = `ACC-${Math.floor(1000 + Math.random() * 9000)}`;
    const newAccount = {
      accountId: mockId,
      accountType: this.formState.accountType,
      balance: this.formState.balance || '0.00',
      accountNumber: `12345678${Math.floor(1000 + Math.random() * 9000)}`,
      status: 'ACTIVE'
    };

    // Update state array reactively
    this.accounts.update(prev => [...prev, newAccount]);
    this.actionMessage.set(`Account ${mockId} simulated successfully!`);
    this.closeCreateModal();
  }

  handleCreateGoal(goalData: any) {
    if (!this.goalCreationAccountId) return;
    const newGoal = {
      goal_id: `G-${Math.random()}`,
      account_id: this.goalCreationAccountId,
      name: goalData.name || 'Custom Goal',
      target: goalData.target || 5000,
      current: 0
    };
    this.goals.update(prev => [...prev, newGoal]);
    this.isGoalCreationOpen = false;
    this.goalActionMessage.set('Savings goal simulated successfully.');
  }

  handleDeleteGoal(goal: any) {
    this.goals.update(prev => prev.filter(g => g.goal_id !== goal.goal_id));
    this.goalActionMessage.set('Savings goal removed.');
  }

  handleCustomerSwitch(event: Event) {
    const selectedId = (event.target as HTMLSelectElement).value;
    const match = this.customerList().find(c => c.customerId === selectedId);
    if (match) {
      this.customer.set({ ...match, dateOfBirth: '1990-01-01' });
      this.actionMessage.set(`Switched to mock customer views: ${match.name}`);
    }
  }

  openCreateModal() { this.isCreateModalOpen = true; }
  closeCreateModal() {
    this.isCreateModalOpen = false;
    this.formState = { accountType: 'SAVINGS', balance: '0.00', interestRate: '2.5' };
  }

  calculateAge(dateOfBirth: string): number | null {
    if (!dateOfBirth) return null;
    const dob = new Date(dateOfBirth);
    const today = new Date();
    let age = today.getFullYear() - dob.getFullYear();
    const m = today.getMonth() - dob.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < dob.getDate())) { age--; }
    return age;
  }
}
