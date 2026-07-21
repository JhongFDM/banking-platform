import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Auth } from '../../../core/services/auth';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile {
  authService = inject(Auth);
  private fb = inject(FormBuilder);

  isEditingAddress = signal(false);
  addressUpdatedMessage = signal(false);

  // Form for editable fields
  addressForm: FormGroup = this.fb.group({
    address: ['41 cowie hill rd', [Validators.required]]
  });

  toggleEditAddress(): void {
    this.isEditingAddress.update(v => !v);
  }

  saveAddress(): void {
    if (this.addressForm.valid) {
      this.isEditingAddress.set(false);
      this.addressUpdatedMessage.set(true);

      setTimeout(() => {
        this.addressUpdatedMessage.set(false);
      }, 3000);
    }
  }
}