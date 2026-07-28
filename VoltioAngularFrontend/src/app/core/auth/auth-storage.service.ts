import { Injectable } from '@angular/core';
import { AuthState } from '../models/auth.model'


const AUTH_STORAGE_KEY = 'banking-app-auth';


@Injectable({
  providedIn: 'root'
})
export class AuthStorageService {


  read(): AuthState {

    const storedValue =
      localStorage.getItem(AUTH_STORAGE_KEY);


    if (!storedValue) {

      return this.emptyState();

    }


    try {

      return JSON.parse(storedValue) as AuthState;

    } catch {

      localStorage.removeItem(AUTH_STORAGE_KEY);

      return this.emptyState();

    }

  }



  write(
    authState: AuthState
  ): void {

    localStorage.setItem(

      AUTH_STORAGE_KEY,

      JSON.stringify(authState)

    );

  }



  clear(): void {

    localStorage.removeItem(AUTH_STORAGE_KEY);

  }



  hasFreshToken(): boolean {

    const auth = this.read();


    return !!(

      auth.accessToken &&

      (
        !auth.expiresAt ||
        auth.expiresAt > Date.now()
      )

    );

  }



  getAccessToken(): string | null {

    if (!this.hasFreshToken()) {

      return null;

    }


    return this.read().accessToken;

  }



  private emptyState(): AuthState {

    return {

      accessToken: null,

      expiresAt: null,

      refreshToken: null

    };

  }

}