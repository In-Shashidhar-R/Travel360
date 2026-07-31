import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TravelService } from '../../../core/services/travel-service';
import { PassengerDirectory } from '../passenger-directory/passenger-directory';

@Component({
  selector: 'app-flight-details',
  standalone: true,
  imports: [CommonModule, RouterModule, PassengerDirectory],
  templateUrl: './flight-details.html',
  styleUrl: './flight-details.scss',
})
export class FlightDetails implements OnInit {
  @Input() item: any = null;
  @Input() date: string = '';
  @Input() searchPrice: number = 0; 

  isLoading = false;
  bookingErrorMessage: string | null = null; 

  selectedPassengerIds: number[] = [];
  chosenSeatType: string | null = null;

  constructor(
    private travelService: TravelService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (this.item && this.item.seatTiers && this.item.seatTiers.length > 0) {
      this.chosenSeatType = this.item.seatTiers[0].seatType;
    }
  }

  get calculateTotalBookingPrice(): number {
    if (!this.item || !this.chosenSeatType || this.selectedPassengerIds.length === 0) {
      return 0;
    }
    
    // If searchPrice was passed from the search hub, use it as the final computed tier price directly
    if (this.searchPrice) {
      return this.searchPrice * this.selectedPassengerIds.length;
    }

    // Fallback calculation framework if accessed without query params
    const activeTier = this.item.seatTiers.find((t: any) => t.seatType === this.chosenSeatType);
    const multiplier = activeTier ? activeTier.priceMultiplier : 1;
    return (this.item.basePricePerSeat * multiplier) * this.selectedPassengerIds.length;
  }
        
  selectSeatTier(seatTypeKey: string): void {
    this.chosenSeatType = seatTypeKey;
    this.bookingErrorMessage = null;
    this.cdr.detectChanges();
  }

  onPassengerSelected(ids: number[]): void {
    this.selectedPassengerIds = ids;
    this.bookingErrorMessage = null;
    this.cdr.detectChanges();
  }

  executeBookingTransaction(): void {
    if (!this.item || !this.chosenSeatType || this.selectedPassengerIds.length === 0) return;
    
    this.bookingErrorMessage = null; 
    const sessionUser = localStorage.getItem('travel360_session');
    let currentUserId: number | null = null;

    if (sessionUser) {
      try {
        currentUserId = JSON.parse(sessionUser).userId; 
      } catch (e) {
        console.error('Session matching parsing failure.', e);
      }
    }

    if (!currentUserId) {
      this.bookingErrorMessage = 'Your session has expired. Please log in again.';
      this.router.navigate(['/login']);
      return;
    }

    const payload = {
      customerId: currentUserId,
      inventoryId: this.item.inventoryId,
      targetTravelDate: this.date || new Date().toISOString().split('T')[0], 
      chosenSeatType: this.chosenSeatType,
      passengerProfileIds: this.selectedPassengerIds
    };

    this.isLoading = true;
    this.cdr.detectChanges();

    this.travelService.bookFlight(payload).subscribe({
      next: (bookingResponse: any) => {
        this.bookingErrorMessage = null;
        
        // 🎯 THE STRATEGIC ALIGNMENT: 
        // Force routing using bookingId. The PaymentScreen will safely use this to pull booking 
        // data first, find its real nested invoiceId, and then load the billing service details.
        const targetBookingId = bookingResponse.bookingId;
        
        if (!targetBookingId) {
          this.bookingErrorMessage = 'Critical: System failed to assign a Booking Tracking Reference ID.';
          this.isLoading = false;
          this.cdr.detectChanges();
          return;
        }

        this.router.navigate(['/customer/payment', targetBookingId]);
      },
      error: (err: HttpErrorResponse) => {
        if (err.error && typeof err.error === 'object' && err.error.message) {
          this.bookingErrorMessage = err.error.message;
        } else {
          this.bookingErrorMessage = 'Booking transaction failed. Check seat capacity limits bounds parameters.';
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
}