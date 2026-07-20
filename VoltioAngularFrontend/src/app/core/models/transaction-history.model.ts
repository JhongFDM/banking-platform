import { TransactionItemResponse } from '../models/transaction-item.model'

export interface TransactionHistoryResponse{

    accountId: number;

    startDate: string;

    endDate: string;

    transactionCount: number;

    transactions: TransactionItemResponse[];

}