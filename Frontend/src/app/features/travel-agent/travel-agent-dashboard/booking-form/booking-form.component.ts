import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { BookingRequestResponseDTO } from '../../../../shared/models/booking-request.model';
import { BookingService } from '../../services/booking-service'; // Adjust path if needed

@Component({
  selector: 'app-booking-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking-form.component.html',
  styleUrls: []
})
export class BookingFormComponent implements OnInit {
  @Input() targetRequest!: BookingRequestResponseDTO;
  
  @Output() onSubmitSuccess = new EventEmitter<{ requestId: number, payload: any }>();
  @Output() onCancel = new EventEmitter<void>();

  formError: string | null = null;
  customerPassengers: any[] = [];
  selectedProfileIds: number[] = [];
  isLoadingPassengers: boolean = false;

  constructor(private bookingService: BookingService) {}

  ngOnInit(): void {
    if (this.targetRequest?.customerId) {
      this.fetchCustomerPassengers(this.targetRequest.customerId);
    }
  }

  minDate: string = (() => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  })();

  fetchCustomerPassengers(customerId: number): void {
    this.isLoadingPassengers = true;
    this.bookingService.getPassengersByCustomerId(customerId).subscribe({
      next: (passengers) => {
        this.customerPassengers = passengers;
        this.isLoadingPassengers = false;
        console.log(passengers)
      },
      error: (err) => {
        console.error('Failed to load passenger directory', err);
        this.formError = 'Failed to load saved passenger profiles for this customer.';
        this.isLoadingPassengers = false;
      }
    });
  }

  onPassengerCheckboxToggle(profileId: number, event: Event): void {
    const isChecked = (event.target as HTMLInputElement).checked;
    if (isChecked) {
      if (!this.selectedProfileIds.includes(profileId)) {
        this.selectedProfileIds.push(profileId);
      }
    } else {
      this.selectedProfileIds = this.selectedProfileIds.filter(id => id !== profileId);
    }
  }

  isPassengerSelected(profileId: number): boolean {
    return this.selectedProfileIds.includes(profileId);
  }

  executeBookingSubmission = (bookingForm: NgForm): void => {
    this.formError = null;

    const travelDate = bookingForm.value.targetTravelDate;
    const personsCount = parseInt(bookingForm.value.numberOfPersons, 10);

    if (!travelDate || !personsCount) {
      this.formError = 'Validation Error: Please fill in all mandatory form input markers (*).';
      return;
    }

    if (this.selectedProfileIds.length === 0) {
      this.formError = 'Validation Error: Please select at least one passenger from the directory.';
      return;
    }

    if (this.selectedProfileIds.length !== personsCount) {
      this.formError = `Validation Mismatch: Headcount set to ${personsCount} must exactly match the number of selected passenger profiles (Selected: ${this.selectedProfileIds.length}).`;
      return;
    }

    const finalizedPayload = {
      customerId: this.targetRequest.customerId,
      inventoryId: this.targetRequest.inventoryId,
      targetTravelDate: travelDate,
      numberOfPersons: personsCount,
      passengerProfileIds: this.selectedProfileIds
    };

    this.onSubmitSuccess.emit({
      requestId: this.targetRequest.requestId,
      payload: finalizedPayload
    });
  };
}