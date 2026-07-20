import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import { CreateCustomerRequest, PatchCustomerRequest, CustomerResponse} from '../models/customer.model';

import { MessageResponse } from '../models/common.model';

/**
 * Angular service responsible for communicating
 * with all Customer-related backend endpoints.
 *
 * Mirrors the Spring CustomerController.
 */
@Injectable({
  providedIn: 'root'
})
export class CustomerService {

  /**
   * Base URL for all backend requests.
   */
  private readonly backendBaseUrl = environment.backendBaseUrl;

  constructor(
    private http: HttpClient
  ) {}

  /**
   * POST /api/customers
   *
   * Creates a new customer.
   *
   * Mirrors:
   * CustomerController.createCustomer(...)
   */
  createCustomer(
    payload: CreateCustomerRequest
  ): Observable<CustomerResponse> {

    return this.http.post<CustomerResponse>(

      `${this.backendBaseUrl}/api/customers`,

      payload

    );

  }

  /**
   * PATCH /api/customers/{customerId}
   *
   * Updates an existing customer.
   *
   * Mirrors:
   * CustomerController.updateCustomer(...)
   */
  updateCustomer(
    customerId: number,
    payload: PatchCustomerRequest
  ): Observable<CustomerResponse> {

    return this.http.patch<CustomerResponse>(

      `${this.backendBaseUrl}/api/customers/${customerId}`,

      payload

    );

  }

  /**
   * GET /api/customers/{customerId}
   *
   * Retrieves a single customer.
   *
   * Mirrors:
   * CustomerController.getCustomer(...)
   */
  getCustomer(
    customerId: number
  ): Observable<CustomerResponse> {

    return this.http.get<CustomerResponse>(

      `${this.backendBaseUrl}/api/customers/${customerId}`

    );

  }

  /**
   * GET /api/customers
   *
   * Returns every customer.
   *
   * Mirrors:
   * CustomerController.getAllCustomers(...)
   */
  getAllCustomers(): Observable<CustomerResponse[]> {

    return this.http.get<CustomerResponse[]>(

      `${this.backendBaseUrl}/api/customers`

    );

  }

  /**
   * DELETE /api/customers/{customerId}
   *
   * Deletes a customer.
   *
   * Mirrors:
   * CustomerController.deleteCustomer(...)
   */
  deleteCustomer(
    customerId: number
  ): Observable<MessageResponse> {

    return this.http.delete<MessageResponse>(

      `${this.backendBaseUrl}/api/customers/${customerId}`

    );

  }

}