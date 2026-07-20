export interface TransactionItemResponse{
    transactionId: string;
    amount: number;
    direction: string;
    status: string;
    timestamp: string //Instant
    description: string;
    idempotencyKey: string;
    category: string;
    senderInfo: string;
    receiverInfo: string;
    externalTransactionId: string;
}