import { Component } from '@angular/core';
import {Header} from "../header/header";
import { RouterLink } from "@angular/router";
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-register',
  imports: [Header, RouterLink, RouterModule, FormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  registerData ={
    email: '',
    password: '',
    fullName: '',
    address: '',
    dateOfBirth: 'mm-dd-yyyy',
    governmentBusinessNumber: ''
  }

  selectedAccountType: string = 'personal';
  registerAccount : boolean = false;

  continue() {
    this.registerAccount = true;
  }

  back(){
    this.registerAccount = false;
  }
  
  register(){

  }
}
