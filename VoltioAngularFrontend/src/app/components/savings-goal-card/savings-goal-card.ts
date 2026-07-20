import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SavingsGoalResponse } from '../../core/models/savings-goal.model';
import { SavingsGoalStatus } from '../../core/enums/savings-goal-status.enum'

@Component({
  selector: 'app-savings-goal-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './savings-goal-card.html',
  styleUrl: './savings-goal-card.css'
})
export class SavingsGoalCardComponent {
  @Input({ required: true }) goal!: SavingsGoalResponse;
  @Input() isLoading = false;

  @Output() edit = new EventEmitter<SavingsGoalResponse>();
  @Output() delete = new EventEmitter<SavingsGoalResponse>();

  // Expose the enum so the template can reference it
  readonly SavingsGoalStatus = SavingsGoalStatus;

  onEdit() {
    this.edit.emit(this.goal);
  }

  onDelete() {
    this.delete.emit(this.goal);
  }
}