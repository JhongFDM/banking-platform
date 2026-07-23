import { Component, inject, signal } from '@angular/core';
import { NavBar } from '../nav-bar/nav-bar';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Auth } from '../../../core/services/auth';
import { Button } from '../button/button';
import { Theme } from '../../../core/services/theme';
import { Toggle } from '../toggle/toggle';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [NavBar, CommonModule, RouterModule, Button, Toggle],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {

  authService = inject(Auth);
  themeService = inject(Theme);
  voltioLogo: string = '../../assets/images/Voltio_icon_white.png';
  
  isProfileMenuOpen = signal<boolean>(false);

  toggleProfileMenu(): void {
    this.isProfileMenuOpen.update(prev => !prev);
  }

  closeProfileMenu(): void {
    this.isProfileMenuOpen.set(false);
  }

  logout(): void {
    this.closeProfileMenu();
    this.authService.logout();
  }

}
