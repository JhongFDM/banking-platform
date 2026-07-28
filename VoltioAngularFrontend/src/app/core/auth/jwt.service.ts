import { Injectable } from '@angular/core';
import { AuthStorageService } from './auth-storage.service';

@Injectable({
  providedIn: 'root'
})
export class JwtService {

  constructor(
    private authStorage: AuthStorageService
  ) {}


  getCustomerId(): number | null {

    const token =
      this.authStorage.getAccessToken();

    if (!token) {
      return null;
    }


    try {

      const payload =
        JSON.parse(
          atob(
            token.split('.')[1]
          )
        );


      return payload.customerId ?? null;

    } catch {

      return null;

    }

  }


  getRoles(): string[] {

    const token =
      this.authStorage.getAccessToken();

    if (!token) {
      return [];
    }


    try {

      const payload =
        JSON.parse(
          atob(
            token.split('.')[1]
          )
        );


      return payload.roles ?? [];

    } catch {

      return [];

    }

  }

}