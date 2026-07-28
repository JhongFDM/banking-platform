import { AccountResponse } from './account.model';


export enum RoleName {
  CUSTOMER = 'CUSTOMER',
  ADMIN = 'ADMIN'
}


export interface LoginRequest {

  username: string;

  password: string;

}


export interface RegisterRequest {

  username: string;

  password: string;

  roles?: RoleName[];

}


export interface AuthResponse {

  accessToken: string;

  refreshToken: string;

  tokenType: string;

  expiresIn: number;

}


export interface UserResponse {

  userId: string;

  username: string;

  roles: RoleName[];

  externalSubjectId?: string;

  customerId?: number;

  active: boolean;

  createdAt: string;

}


/**
 * Authentication state stored in localStorage.
 *
 * Not the same as AuthResponse as AuthResponse represents what the backend sends.
 *
 * AuthState represents how the Angular application
 * stores authentication information.
 */
export interface AuthState {

  /**
   * JWT access token.
   */
  accessToken: string | null;

  refreshToken: string | null;

  /**
   * Absolute expiration timestamp (milliseconds since epoch).
   *
   * expiresAt =
   *   Date.now() + (expiresIn * 1000)
   */
  expiresAt: number | null;

}
