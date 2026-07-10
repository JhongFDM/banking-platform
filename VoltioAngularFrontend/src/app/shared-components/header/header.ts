import { Component } from '@angular/core';
import { NavBar } from '../nav-bar/nav-bar';

@Component({
  selector: 'app-header',
  imports: [NavBar],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {

  voltioLogo: string = '../../assets/images/Voltio_icon_white.png';

}
