import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookingRequestResponseDTO } from '../../../../shared/models/booking-request.model';

@Component({
  selector: 'app-request-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './request-list.component.html',
  styleUrls: []
})
export class RequestListComponent {
  @Input() requests: BookingRequestResponseDTO[] = [];
  
  @Output() onAccept = new EventEmitter<{ requestId: number, notes: string }>();
  @Output() onReject = new EventEmitter<{ requestId: number, notes: string }>();
  @Output() onBook = new EventEmitter<BookingRequestResponseDTO>();

  triggerAccept(id: number): void {
    const notes = prompt("Enter optional notes for accepting this request:", "Accepted");
    if (notes !== null) {
      this.onAccept.emit({ requestId: id, notes: notes || 'Accepted' });
    }
  }

  triggerReject(id: number): void {
    const notes = prompt("Enter mandatory reason notes for rejecting this request:");
    if (!notes) {
      alert("Action aborted. Explanation notes are required to reject a request.");
      return;
    }
    this.onReject.emit({ requestId: id, notes: notes });
  }
}