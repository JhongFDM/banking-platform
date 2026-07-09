import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule} from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Header } from "../header/header";

@Component({
  selector: 'app-login-page',
  imports: [CommonModule, FormsModule, RouterModule, Header],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  loginData ={
    email: '',
    password: ''
  }
  emailEntered : boolean = true;
  passwordEntered : boolean = true;
  loginPage : boolean = true;
  accountType: boolean = false;

  login() {
    console.log('Login data:', this.loginData);
    this.emailEntered = true;
    this.passwordEntered = true;

    if(this.loginData.email === '') {
      this.emailEntered = false;
    } else if(this.loginData.password === '') {
      this.passwordEntered = false;
    } else {
      console.log('Login successful!');
    }
  }

}