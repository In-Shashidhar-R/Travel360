import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TravelService } from '../../../core/services/travel-service';
import { InvoiceResponseDTO, PageResponse } from '../../../shared/models/finance.model';
import { BookingDetails } from '../booking-details/booking-details';

@Component({
  selector: 'app-customer-bookings-directory',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, BookingDetails],
  templateUrl: './cust-bookings.html',
  styleUrl: './cust-bookings.scss'
})
export class CustomerBookings implements OnInit {
  private travelService = inject(TravelService);
  private cdr = inject(ChangeDetectorRef);

  bookings: any[] = [];
  selectedBookingId: number | null = null;
  customerInvoicesList: InvoiceResponseDTO[] = [];

  currentPage = 0;
  pageSize = 5;
  totalPages = 0;
  totalElements = 0;
  
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  protected readonly Math = Math;

  ngOnInit(): void {
    this.fetchBookingHistoryPage(this.currentPage);
  }

  get currentUserId(): number {
    const session = localStorage.getItem('travel360_session');
    if (!session) return 0;
    try {
      return JSON.parse(session).userId || 0;
    } catch {
      return 0;
    }
  }

  fetchBookingHistoryPage(pageIndex: number): void {
    const userId = this.currentUserId;
    if (!userId) {
      this.errorMessage = 'Session expired. Please log in again.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.detectChanges();

    this.travelService.getCustomerInvoices(userId, 0, 100).subscribe({
      next: (invoicePage: PageResponse<InvoiceResponseDTO>) => {
        this.customerInvoicesList = invoicePage?.content || [];
        
        this.travelService.getCustomerBookingsPage(userId, pageIndex, this.pageSize).subscribe({
          next: (response: any) => {
            this.bookings = response.content || [];
            console.log(this.bookings)
            this.bookings.forEach(b => {
              b.calculatedDisplayPrice = this.resolveBookingDisplayPrice(b);
            });

            this.totalPages = response.totalPages || 0;
            this.totalElements = response.totalElements || 0;
            this.currentPage = pageIndex;
            this.isLoading = false;
            this.cdr.detectChanges();
          },
          error: (err: HttpErrorResponse) => {
            this.errorMessage = err.error?.message || 'Unable to extract booking records.';
            this.isLoading = false;
            this.cdr.detectChanges();
          }
        });
      },
      error: () => {
        this.errorMessage = 'Could not resolve backend financial audit contexts.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  resolveBookingDisplayPrice(booking: any): number {
    const relatedInvoices = this.customerInvoicesList.filter(inv => inv.bookingId === booking.bookingId);
    const basePurchase = relatedInvoices.find(inv => inv.status === 'PAID');
    if (basePurchase) return basePurchase.amount;
    
    if (booking.status === 'CANCELLED' || booking.totalAmount === 0) {
      const totalRefunded = relatedInvoices
        .filter(inv => inv.status === 'REFUNDED')
        .reduce((sum, inv) => sum + (inv.amount || 0), 0);
      if (totalRefunded > 0) return totalRefunded;
    }
    return booking.totalAmount || booking.totalPrice || 0;
  }

  openDetailsModal(bookingId: number): void {
    this.selectedBookingId = bookingId;
    this.cdr.detectChanges();
  }

  closeDetailsModal(): void {
    this.selectedBookingId = null;
    this.cdr.detectChanges();
  }

  handleDataMutation(): void {
    // Re-sync parent financial ledgers on structural mutation adjustments (Cancellations)
    this.travelService.getCustomerInvoices(this.currentUserId, 0, 100).subscribe({
      next: (invoicePage: PageResponse<InvoiceResponseDTO>) => {
        this.customerInvoicesList = invoicePage?.content || [];
        this.fetchBookingHistoryPage(this.currentPage);
      }
    });
  }
}