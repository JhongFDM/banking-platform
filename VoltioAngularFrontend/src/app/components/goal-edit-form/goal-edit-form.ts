import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SavingsGoalResponse, SavingsGoalRequest } from '../../core/models/savings-goal.model';

export type GoalFormData = SavingsGoalRequest;

@Component({
  selector: 'app-goal-edit-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './goal-edit-form.html',
  styleUrl: './goal-edit-form.css'
})
export class GoalEditFormComponent implements OnInit {
  @Input({ required: true }) goal!: SavingsGoalResponse;
  @Input() isLoading = false;

  @Output() submitEdit = new EventEmitter<GoalFormData>();
  @Output() cancel = new EventEmitter<void>();

  formData: GoalFormData = {
    goalName: '',
    targetAmount: 0,
    targetDate: ''
  };

  ngOnInit() {
    if (this.goal) {
      this.formData = {
        goalName: this.goal.goalName,
        targetAmount: this.goal.targetAmount,
        targetDate: this.goal.targetDate
      };
    }
  }

  onSubmit(event: Event) {
    event.preventDefault();
    this.submitEdit.emit({ ...this.formData });
  }

  onCancel() {
    this.cancel.emit();
  }
}