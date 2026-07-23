import { Injectable, signal, computed } from '@angular/core';
import { Router } from '@angular/router';


export interface User {
  name: string;
  email: string;
  role: 'ADMIN' | 'CUSTOMER';
  avatarUrl?: string;
}

@Injectable({
  providedIn: 'root',
})

export class Auth {

  currentUser = signal<User | null>({
    name: 'Alex Johnson',
    email: 'alex@voltio.com',
    role: 'ADMIN'
  });

  isLoggedIn = computed(() => this.currentUser() !== null);
  userRole = computed(() => this.currentUser()?.role ?? null);

  constructor(private router: Router) {}

  logout(): void {
    localStorage.removeItem('token');
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }
}
