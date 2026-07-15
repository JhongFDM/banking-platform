/**
 * Request body for:
 *
 * POST /api/auth/login
 *
 * Mirrors the backend LoginRequest DTO.
 */
export interface LoginRequest {

  username: string;

  password: string;

}


/**
 * Request body for:
 *
 * POST /api/auth/register
 *
 * Update this interface to exactly match your
 * backend RegisterRequest DTO.
 *
 * (The fields below are examples if you haven't
 * looked at the backend DTO yet.)
 */
export interface RegisterRequest {

  username: string;

  password: string;

  roles?: RoleName[];

}


/**
 * Response returned by:
 *
 * POST /api/auth/login
 * POST /api/auth/register
 *
 * Mirrors the backend AuthResponse DTO.
 */
export interface AuthResponse {

  accessToken: string;

  refreshToken: string;

  tokenType: string;

  /**
   * Number of seconds until the access token expires.
   */
  expiresIn: number;

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

/**
 * Matches backend:
 *
 * com.group1.banking.enums.RoleName
 *
 * Update these values if your backend enum
 * contains additional roles.
 */
export enum RoleName {

  CUSTOMER = 'CUSTOMER',

  ADMIN = 'ADMIN'

}