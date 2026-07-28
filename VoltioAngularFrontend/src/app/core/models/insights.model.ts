import { TransactionItemResponse } from './transaction-item.model';


/**
 * Request body for changing a transaction category.
 *
 * Backend:
 * RecategoriseRequest
 */
export interface RecategoriseRequest {

  category: string;

}


/**
 * Response after changing a transaction category.
 *
 * Backend:
 * RecategoriseResponse
 */
export interface RecategoriseResponse {

  transactionId: string;

  previousCategory: string | null;

  updatedCategory: string;

  updatedTotalDebitSpend: number;

  updatedCategoryBreakdown: CategoryBreakdownItem[];

}


/**
 * Main spending insights response.
 *
 * Backend:
 * SpendingInsightResponse
 */
export interface SpendingInsightResponse {

  accountId: number;

  period: PeriodInfo;

  totalDebitSpend: number;

  transactionCount: number;

  hasUncategorised: boolean;

  hasExcludedDisputes: boolean;

  dataFresh: boolean;

  categoryBreakdown: CategoryBreakdownItem[];

  topTransactions: TransactionItemResponse[];

  sixMonthTrend: MonthTrendItem[];

}


/**
 * Represents the requested month.
 *
 * Backend:
 * SpendingInsightResponse.PeriodInfo
 */
export interface PeriodInfo {

  year: number;

  month: number;

  isComplete: boolean;

}


/**
 * Spending grouped by category.
 *
 * Backend:
 * CategoryBreakdownItem
 */
export interface CategoryBreakdownItem {

  category: string;

  totalAmount: number;

  percentage: number;

  transactionCount: number;

}


/**
 * Historical monthly spending trend.
 *
 * Backend:
 * MonthTrendItem
 */
export interface MonthTrendItem {

  year: number;

  month: number;

  totalSpend: number;

  isComplete: boolean;

  accountExisted: boolean;

}