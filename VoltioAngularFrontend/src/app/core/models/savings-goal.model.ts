import { SavingsGoalStatus } from '../enums/savings-goal-status.enum'

export interface SavingsGoalRequest{

    goalName: string;

    targetAmount: number;

    // yyyy-mm-dd
    targetDate: string;
}

export interface SavingsGoalResponse {

  goalId: number;

  accountId: number;

  accountNumber: string;

  accountType: string;

  goalName: string;

  targetAmount: number;

  targetDate: string;

  currentBalance: number;

  progressPercentage: number;

  timeRemainingDays: number;

  status: SavingsGoalStatus;

  createdAt: string;

  updatedAt: string;

}