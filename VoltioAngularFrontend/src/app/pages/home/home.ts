import { Component } from '@angular/core';
import { Header } from '../../shared-components/header/header';
import { Footer } from '../../shared-components/footer/footer';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  imports: [Header, Footer, RouterModule, CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home {}
