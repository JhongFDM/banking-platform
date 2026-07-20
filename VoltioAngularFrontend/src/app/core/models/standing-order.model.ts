/**
 * Request body for creating a standing order.
 *
 * Backend:
 * CreateStandingOrderRequest
 */
export interface CreateStandingOrderRequest {

  payeeAccount?: number | null;

  payeeName: string;

  amount: number;

  frequency: string;

  startDate: string;

  endDate?: string | null;

  reference: string;

}


/**
 * Response representing a single standing order.
 *
 * Backend:
 * StandingOrderResponse
 */
export interface StandingOrderResponse {

  standingOrderId: string;

  sourceAccountId: number;

  payeeAccount?: number | null;

  payeeName: string;

  amount: number;

  frequency: string;

  startDate: string;

  endDate?: string | null;

  reference: string;

  status: string;

  nextRunDate?: string | null;

  message?: string | null;

}


/**
 * Response when listing standing orders.
 *
 * Backend:
 * StandingOrderListResponse
 */
export interface StandingOrderListResponse {

  accountId: number;

  standingOrderCount: number;

  standingOrders: StandingOrderResponse[];

}


/**
 * Response after cancelling a standing order.
 *
 * Backend:
 * CancelStandingOrderResponse
 */
export interface CancelStandingOrderResponse {

  standingOrderId: string;

  status: string;

  message: string;

}