import { Injectable } from '@angular/core';

/**
 * The key used to store authentication information in localStorage.
 * This matches the React implementation so the backend contract
 * and stored data remain unchanged.
 */
const AUTH_STORAGE_KEY = 'banking-app-auth';

/**
 * Represents the authentication state stored in localStorage.
 * Add any additional properties your backend returns.
 */
export interface AuthState {
  accessToken: string | null;
  expiresAt: number | null;
  refreshToken?: string | null;
  customerId?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class AuthStorageService {

  /**
   * Reads the authentication object from localStorage.
   *
   * If nothing is stored (or the JSON is invalid),
   * return an empty auth state instead of throwing an error.
   */
  read(): AuthState {

    // Get the JSON string from localStorage
    const storedValue = localStorage.getItem(AUTH_STORAGE_KEY);

    // Nothing stored yet
    if (!storedValue) {
      return {
        accessToken: null,
        expiresAt: null
      };
    }

    try {

      // Convert JSON string into an object
      return JSON.parse(storedValue) as AuthState;

    } catch {

      // Corrupted JSON shouldn't crash the app.
      // Remove it and start fresh.
      localStorage.removeItem(AUTH_STORAGE_KEY);

      return {
        accessToken: null,
        expiresAt: null
      };
    }
  }

  /**
   * Saves the latest authentication state.
   *
   * This would typically be called after a successful login.
   */
write(authState: Partial<AuthState>): void {

  const normalizedState: AuthState = {

    accessToken:
      authState.accessToken ?? null,

    expiresAt:
      authState.expiresAt ?? null,

    refreshToken:
      authState.refreshToken ?? null,

    customerId:
      authState.customerId ?? null

  };

  localStorage.setItem(
    AUTH_STORAGE_KEY,
    JSON.stringify(normalizedState)
  );

}

  /**
   * Removes the stored authentication information.
   *
   * This is used during logout or when the session expires.
   */
  clear(): void {

    localStorage.removeItem(AUTH_STORAGE_KEY);

  }

  /**
   * Convenience method to determine whether a token
   * exists and has not expired.
   *
   * This reproduces the React logic:
   *
   * authState.accessToken &&
   * (!authState.expiresAt || authState.expiresAt > Date.now())
   */
  hasFreshToken(): boolean {

    const auth = this.read();

    return !!(
      auth.accessToken &&
      (!auth.expiresAt || auth.expiresAt > Date.now())
    );

  }

  /**
   * Convenience method to return only the current access token.
   *
   * Returns null if the token is missing or expired.
   */
  getAccessToken(): string | null {

    if (!this.hasFreshToken()) {
      return null;
    }

    return this.read().accessToken;

  }

}