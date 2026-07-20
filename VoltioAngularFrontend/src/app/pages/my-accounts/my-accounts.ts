import { Component, OnInit, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { AccountService } from '../../core/services/account.service';
import { CustomerService } from '../../core/services/customer.service';
import { JwtService } from '../../core/auth/jwt.service';
import { SavingsGoalService } from '../../core/services/savings-goal.service';

import {
  AccountResponse,
  CreateAccountRequest
} from '../../core/models/account.model';

import { CustomerResponse } from '../../core/models/customer.model';
import {
  SavingsGoalRequest,
  SavingsGoalResponse
} from '../../core/models/savings-goal.model';

// Child components
import { GoalCreationFlowComponent, GoalFormData } from '../../components/goal-creation-flow/goal-creation-flow';
import { GoalEditFormComponent } from '../../components/goal-edit-form/goal-edit-form';
import {SavingsGoalCardComponent} from '../../components/savings-goal-card/savings-goal-card';

@Component({
  selector: 'app-my-accounts',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    SavingsGoalCardComponent,
    GoalCreationFlowComponent,
    GoalEditFormComponent
  ],
  templateUrl: './my-accounts.html',
  styleUrl: './my-accounts.css',
})
export class MyAccounts implements OnInit {
  constructor(
    private accountService: AccountService,
    private customerService: CustomerService,
    private jwtService: JwtService,
    private savingsGoalService: SavingsGoalService
  ) {}

  isAdmin = false;
  private MOCK_KYC_VERIFIED = true;

  // Signals
  accounts = signal<AccountResponse[]>([]);
  customer = signal<CustomerResponse | null>(null);
  customerList = signal<CustomerResponse[]>([]);
  goals = signal<SavingsGoalResponse[]>([]);

  isLoadingAccounts = signal(false);
  goalsLoading = signal(false);
  error = signal<{ message: string } | null>(null);
  actionMessage = signal<string | null>(null);
  goalActionMessage = signal<string | null>(null);

  // Modal / Form States
  isCreateModalOpen = false;
  isGoalCreationOpen = false;
  goalCreationAccountId: number | null = null;
  goalCreationAccountNumber = '';
  editingGoal: SavingsGoalResponse | null = null;

  formState = {
    accountType: 'SAVINGS',
    balance: '0.00',
    interestRate: '2.5'
  };

  // Computed Properties
  totalBalance = computed(() => {
    return this.accounts().reduce(
      (sum, account) => sum + Number(account.balance ?? 0),
      0
    );
  });

  existingTypes = computed(() => {
    return new Set(this.accounts().map(a => a.accountType));
  });

  // Maps accountId -> SavingsGoalResponse
  goalsByAccountId = computed(() => {
    return this.goals().reduce((map: Record<number, SavingsGoalResponse>, goal) => {
      if (goal.accountId) {
        map[goal.accountId] = goal;
      }
      return map;
    }, {});
  });

  get tfsaRate() {
    const account = this.accounts().find(a => a.accountType === 'TFSA');
    return account?.interestRate ?? null;
  }

  get rrspRate() {
    const account = this.accounts().find(a => a.accountType === 'RRSP');
    return account?.interestRate ?? null;
  }

  get isTfsa() {
    return this.formState.accountType === 'TFSA';
  }

  get isTooYoung() {
    if (!this.isTfsa) return false;
    const dob = this.customer()?.dateOfBirth;
    const age = this.calculateAge(dob);
    return age !== null && age < 18;
  }

  get isKycBlocked() {
    return this.isTfsa && !this.MOCK_KYC_VERIFIED;
  }

  ngOnInit() {
    const customerId = this.jwtService.getCustomerId();

    if (!customerId) {
      this.error.set({
        message: 'Unable to determine logged in customer.'
      });
      return;
    }

    this.loadCustomer(customerId);
    this.loadAccounts(customerId);
    this.loadGoals(customerId);
  }

  // --- Data Loading Methods ---

  loadCustomer(customerId: number) {
    this.customerService.getCustomer(customerId).subscribe({
      next: (customer) => this.customer.set(customer),
      error: (err) => this.error.set({ message: err.message ?? 'Failed to load customer.' })
    });
  }

  loadAccounts(customerId: number) {
    this.isLoadingAccounts.set(true);

    this.accountService.listCustomerAccounts(customerId).subscribe({
      next: (accounts) => {
        this.accounts.set(accounts);
        this.isLoadingAccounts.set(false);
      },
      error: (err) => {
        this.error.set({ message: err.message ?? 'Failed to load accounts.' });
        this.isLoadingAccounts.set(false);
      }
    });
  }

  loadGoals(customerId: number) {
    this.goalsLoading.set(true);

    this.savingsGoalService.getCustomerGoals(customerId).subscribe({
      next: (goals) => {
        this.goals.set(goals);
        this.goalsLoading.set(false);
      },
      error: (err) => {
        this.goalActionMessage.set(err.message ?? 'Failed to load savings goals.');
        this.goalsLoading.set(false);
      }
    });
  }

  // --- Account Actions ---

  handleCreateAccount(event: Event) {
    event.preventDefault();
    const customerId = this.customer()?.customerId;

    if (!customerId) {
      this.error.set({ message: 'Customer not found.' });
      return;
    }

    const payload: CreateAccountRequest = {
      accountType: this.formState.accountType as any,
      balance: Number(this.formState.balance),
      interestRate: Number(this.formState.interestRate)
    };

    this.accountService.createAccount(customerId, payload).subscribe({
      next: (account) => {
        this.accounts.update(current => [...current, account]);
        this.actionMessage.set('Account created successfully.');
        this.closeCreateModal();
      },
      error: (err) => {
        this.error.set({ message: err.message ?? 'Unable to create account.' });
      }
    });
  }

  handleCustomerSwitch(event: Event) {
    const select = event.target as HTMLSelectElement;
    const customerId = Number(select.value);
    if (customerId) {
      this.loadCustomer(customerId);
      this.loadAccounts(customerId);
      this.loadGoals(customerId);
    }
  }

  showBalance(): boolean {
    return true;
  }

  showInterestRate(): boolean {
    return ['SAVINGS', 'TFSA', 'RRSP'].includes(this.formState.accountType);
  }

  // --- Goal Actions ---

  openGoalCreation(account: AccountResponse) {
    this.goalCreationAccountId = account.accountId;
    this.goalCreationAccountNumber = (account as any).accountNumber || `ACC-${account.accountId}`;
    this.isGoalCreationOpen = true;
  }

  handleCreateGoal(formData: GoalFormData) {
    if (!this.goalCreationAccountId) return;

    this.goalsLoading.set(true);

    const payload: SavingsGoalRequest = {
      goalName: formData.goalName,
      targetAmount: formData.targetAmount,
      targetDate: formData.targetDate
    };

    this.savingsGoalService.createGoal(this.goalCreationAccountId, payload).subscribe({
      next: (newGoal) => {
        this.goals.update(current => [...current, newGoal]);
        this.goalActionMessage.set('Savings goal created successfully.');
        this.isGoalCreationOpen = false;
        this.goalCreationAccountId = null;
        this.goalsLoading.set(false);
      },
      error: (err) => {
        this.goalActionMessage.set(err.message ?? 'Failed to create savings goal.');
        this.goalsLoading.set(false);
      }
    });
  }

  handleEditGoal(goal: SavingsGoalResponse) {
    this.editingGoal = goal;
  }

  handleUpdateGoal(formData: GoalFormData) {
    if (!this.editingGoal || !this.editingGoal.accountId || !this.editingGoal.goalId) return;

    this.goalsLoading.set(true);

    const payload: SavingsGoalRequest = {
      goalName: formData.goalName,
      targetAmount: formData.targetAmount,
      targetDate: formData.targetDate
    };

    this.savingsGoalService.updateGoal(this.editingGoal.accountId, this.editingGoal.goalId, payload).subscribe({
      next: (updatedGoal) => {
        this.goals.update(current =>
          current.map(g => (g.goalId === updatedGoal.goalId ? updatedGoal : g))
        );
        this.goalActionMessage.set('Savings goal updated successfully.');
        this.editingGoal = null;
        this.goalsLoading.set(false);
      },
      error: (err) => {
        this.goalActionMessage.set(err.message ?? 'Failed to update savings goal.');
        this.goalsLoading.set(false);
      }
    });
  }

  handleDeleteGoal(goal: SavingsGoalResponse) {
    if (!goal.accountId || !goal.goalId) return;

    this.goalsLoading.set(true);

    this.savingsGoalService.deleteGoal(goal.accountId, goal.goalId).subscribe({
      next: () => {
        this.goals.update(current => current.filter(g => g.goalId !== goal.goalId));
        this.goalActionMessage.set('Savings goal deleted successfully.');
        this.goalsLoading.set(false);
      },
      error: (err) => {
        this.goalActionMessage.set(err.message ?? 'Failed to delete savings goal.');
        this.goalsLoading.set(false);
      }
    });
  }

  // --- Helpers & Modals ---

  openCreateModal() {
    this.isCreateModalOpen = true;
  }

  closeCreateModal() {
    this.isCreateModalOpen = false;
    this.formState = {
      accountType: 'SAVINGS',
      balance: '0.00',
      interestRate: '2.5'
    };
  }

  calculateAge(dateOfBirth: string | undefined): number | null {
    if (!dateOfBirth) return null;
    const dob = new Date(dateOfBirth);
    const today = new Date();
    let age = today.getFullYear() - dob.getFullYear();
    const month = today.getMonth() - dob.getMonth();
    if (month < 0 || (month === 0 && today.getDate() < dob.getDate())) {
      age--;
    }
    return age;
  }
}