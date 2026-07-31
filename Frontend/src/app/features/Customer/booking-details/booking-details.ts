import { Component, Input, Output, EventEmitter, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TravelService } from '../../../core/services/travel-service';
import { InvoiceResponseDTO } from '../../../shared/models/finance.model';

@Component({
  selector: 'app-booking-details-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking-details.html'
})
export class BookingDetails implements OnInit {
  private travelService = inject(TravelService);
  private cdr = inject(ChangeDetectorRef);
  private router = inject(Router);

  @Input() bookingId!: number;
  @Input() customerInvoicesList: InvoiceResponseDTO[] = [];
  @Input() currentUserId!: number;

  @Output() onClose = new EventEmitter<void>();
  @Output() onDataMutated = new EventEmitter<void>();

  selectedBooking: any | null = null;
  selectedBookingRefunds: InvoiceResponseDTO[] = [];
  originalBookingPriceBeforeCancel = 0;
  totalRefundedAmount = 0; 

  passengerCancellationMap: { [idProofNumber: string]: boolean } = {};
  cancellationRemarks = '';

  isLoading = false;
  isActionProcessing = false;
  errorMessage = '';

  activeBookingComplaints: any[] = [];
  showComplaintForm = false;
  complaintSubject = '';
  complaintDescription = '';
  isComplaintSubmitting = false;
  complaintSuccess = '';
  complaintError = '';

  ngOnInit(): void {
    this.loadModalDetailsContext();
  }

  loadModalDetailsContext(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.passengerCancellationMap = {};
    this.cancellationRemarks = '';
    this.showComplaintForm = false;
    this.resetComplaintFields();
    this.cdr.detectChanges();

    this.travelService.getBookingById(this.bookingId).subscribe({
      next: (data: any) => {
        this.selectedBooking = data;
        console.log(this.selectedBooking)
        if (this.selectedBooking && !this.selectedBooking.requestedSeats) {
          this.selectedBooking.requestedSeats = 
            this.selectedBooking.numberOfPersons || 
            (this.selectedBooking.passengers ? this.selectedBooking.passengers.length : 0);
        }

        const relatedInvoices = this.customerInvoicesList.filter(inv => inv.bookingId === this.bookingId);
        this.selectedBookingRefunds = relatedInvoices.filter(inv => inv.status === 'REFUNDED');
        
        const baseInvoice = relatedInvoices.find(inv => inv.status === 'PAID');
        this.originalBookingPriceBeforeCancel = baseInvoice ? baseInvoice.amount : 0;
        this.totalRefundedAmount = this.selectedBookingRefunds.reduce((sum, inv) => sum + (inv.amount || 0), 0);

        this.fetchComplaintsForSelectedBooking(this.bookingId);

        if (this.selectedBooking && this.selectedBooking.inventoryId) {
          this.travelService.getInventoryById(this.selectedBooking.inventoryId).subscribe({
            next: (invData: any) => {
              this.selectedBooking.inventoryContext = invData;
              this.isLoading = false;
              this.cdr.detectChanges();
            },
            error: () => {
              this.isLoading = false;
              this.cdr.detectChanges();
            }
          });
        } else {
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message || 'Unable to load booking details.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  get selectedPassengersCount(): number {
    return Object.keys(this.passengerCancellationMap)
      .filter(key => this.passengerCancellationMap[key]).length;
  }

  get hasSelectedPassengersForCancellation(): boolean {
    return Object.values(this.passengerCancellationMap).some(isSelected => isSelected);
  }

  togglePassengerSelection(idProofNumber: string): void {
    this.passengerCancellationMap[idProofNumber] = !this.passengerCancellationMap[idProofNumber];
  }

  executePartialPassengerCancellation(): void {
    if (!this.selectedBooking || !this.hasSelectedPassengersForCancellation) return;

    const targetedIdProofs = Object.keys(this.passengerCancellationMap).filter(key => this.passengerCancellationMap[key]);
    const activePassengersOnUI = this.selectedBooking.passengers.filter((p: any) => p.status !== 'CANCELLED').length;

    if (targetedIdProofs.length >= activePassengersOnUI) {
      if (confirm('Cancelling all remaining passengers will cancel the entire reservation. Proceed with full cancellation?')) {
        this.executeEntireBookingCancellation();
      }
      return;
    }

    const payload = {
      customerId: this.currentUserId,
      passengerProfileIdsToCancel: targetedIdProofs.map(key => Number(key)),
      cancellationRemarks: this.cancellationRemarks.trim() || 'Partial passenger cancellation request.'
    };

    this.isActionProcessing = true;
    this.cdr.detectChanges();

    this.travelService.cancelBookingPartial(this.selectedBooking.bookingId, payload).subscribe({
      next: () => {
        alert('Cancellation processed successfully.');
        this.isActionProcessing = false;
        this.passengerCancellationMap = {};
        this.onDataMutated.emit();
        this.loadModalDetailsContext();
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message || 'Partial cancellation request failed.';
        this.isActionProcessing = false;
        this.cdr.detectChanges();
      }
    });
  }

  executeEntireBookingCancellation(): void {
    if (!this.selectedBooking) return;
    if (!confirm('Are you sure you want to cancel this entire booking?')) return;

    const payload = { cancellationRemarks: this.cancellationRemarks.trim() || 'Full reservation cancellation.' };
    this.isActionProcessing = true;
    this.cdr.detectChanges();

    this.travelService.cancelEntireBooking(this.selectedBooking.bookingId, payload).subscribe({
      next: () => {
        alert('Success: This booking is now fully cancelled.');
        this.isActionProcessing = false;
        this.onDataMutated.emit();
        this.loadModalDetailsContext();
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message || 'Whole booking cancellation was denied.';
        this.isActionProcessing = false;
        this.cdr.detectChanges();
      }
    });
  }

  redirectToPaymentGate(): void {
    if (this.selectedBooking?.bookingId) {
      const targetBookingId = this.selectedBooking.bookingId;
      this.onClose.emit();
      this.router.navigate(['/customer/payment', targetBookingId]);
    }
  }

  fetchComplaintsForSelectedBooking(bookingId: number): void {
    this.travelService.getMyComplaints(0, 50).subscribe({
      next: (res: any) => {
        const allComplaints = res.content || res;
        this.activeBookingComplaints = allComplaints.filter((c: any) => c.relatedBookingId === bookingId);
        this.cdr.detectChanges();
      }
    });
  }

  executeComplaintSubmission(): void {
    if (!this.selectedBooking || !this.complaintSubject.trim() || !this.complaintDescription.trim()) return;

    this.isComplaintSubmitting = true;
    this.cdr.detectChanges();

    const payload = {
      subject: this.complaintSubject.trim(),
      description: this.complaintDescription.trim(),
      relatedBookingId: this.selectedBooking.bookingId
    };

    this.travelService.raiseComplaint(payload).subscribe({
      next: () => {
        this.isComplaintSubmitting = false;
        this.complaintSuccess = '🎉 Dispute logged successfully into the tracking service!';
        this.cdr.detectChanges();
        this.fetchComplaintsForSelectedBooking(this.selectedBooking.bookingId);

        setTimeout(() => {
          this.showComplaintForm = false;
          this.resetComplaintFields();
          this.cdr.detectChanges();
        }, 2000);
      },
      error: (err: HttpErrorResponse) => {
        this.isComplaintSubmitting = false;
        this.complaintError = err.error?.message || 'Dispute transaction request failed.';
        this.cdr.detectChanges();
      }
    });
  }

  resetComplaintFields(): void {
    this.complaintSubject = '';
    this.complaintDescription = '';
    this.complaintError = '';
    this.complaintSuccess = '';
  }
}