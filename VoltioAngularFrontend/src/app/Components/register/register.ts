import { Component } from '@angular/core';
import {Header} from "../header/header";
import { RouterLink } from "@angular/router";
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-register',
  imports: [Header, RouterLink, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {}
