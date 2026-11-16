import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { AccessPass } from './access-pass.models';

@Component({
  selector: 'app-access-credential-modal',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './access-credential-modal.html',
  styleUrls: ['./access-credential-modal.css']
})
export class AccessCredentialModal {
  @Input({ required: true }) pass!: AccessPass;
  @Input() isAdmin = false;
  @Input() isRegenerating = false;

  @Output() close = new EventEmitter<void>();
  @Output() regenerate = new EventEmitter<void>();

  onClose(): void {
    this.close.emit();
  }

  onRegenerate(): void {
    this.regenerate.emit();
  }
}
