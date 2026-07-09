import { Component } from '@angular/core';
import { NavBar } from '../nav-bar/nav-bar';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-header',
  imports: [NavBar, RouterModule],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {

  voltioLogo: string = '../../assets/images/Voltio_icon_white.png';
}
