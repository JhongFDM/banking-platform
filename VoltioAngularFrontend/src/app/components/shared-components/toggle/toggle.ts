import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-toggle',
  standalone: true,
  imports: [],
  templateUrl: './toggle.html',
  styleUrl: './toggle.css',
})
export class Toggle {
  @Input() checked: boolean = false;
  @Input() label: string = '';
  @Output() toggled = new EventEmitter<boolean>();

  onToggle(): void {
    this.checked = !this.checked;
    this.toggled.emit(this.checked);
  }
}