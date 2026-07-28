import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SavingsGoalRequest } from '../../core/models/savings-goal.model';

export type GoalFormData = SavingsGoalRequest;

@Component({
  selector: 'app-goal-creation-flow',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './goal-creation-flow.html',
  styleUrl: './goal-creation-flow.css'
})
export class GoalCreationFlowComponent {
  @Input() accountNumber = '';
  @Input() isLoading = false;

  @Output() submitFlow = new EventEmitter<GoalFormData>();
  @Output() cancel = new EventEmitter<void>();

  formData: GoalFormData = {
    goalName: '',
    targetAmount: 1000,
    targetDate: ''
  };

  onSubmit(event: Event) {
    event.preventDefault();
    if (!this.formData.goalName || !this.formData.targetAmount || !this.formData.targetDate) {
      return;
    }
    this.submitFlow.emit({ ...this.formData });
  }

  onCancel() {
    this.cancel.emit();
  }
}