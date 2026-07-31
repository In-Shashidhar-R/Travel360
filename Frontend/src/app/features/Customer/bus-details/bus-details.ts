import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TravelService } from '../../../core/services/travel-service';
import { PassengerDirectory } from '../passenger-directory/passenger-directory';
import { BillingService } from '../../../core/services/billing-service';

@Component({
  selector: 'app-bus-details',
  standalone: true,
  imports: [CommonModule, RouterModule, PassengerDirectory],
  templateUrl: './bus-details.html',
  styleUrl: './bus-details.scss',
})
export class BusDetails implements OnInit {
  @Input() item: any = null; // Expects a BusInventoryResponseDTO payload
  @Input() date: string = '';
  @Input() searchPrice: number = 0; 

  isLoading = false;
  bookingErrorMessage: string | null = null; 

  selectedPassengerIds: number[] = [];
  chosenSeatType: string | null = null;

  constructor(
    private travelService: TravelService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private billingService: BillingService
  ) {}

  ngOnInit(): void {
    console.log("LIVE BUS DETAIL OBJECT LAYOUT:", this.item);
    if (this.item && this.item.seatTiers && this.item.seatTiers.length > 0) {
      this.chosenSeatType = this.item.seatTiers[0].seatType;
    }
  }

    get calculateTotalBookingPrice(): number {
      if (!this.item || !this.chosenSeatType || this.selectedPassengerIds.length === 0) {
        return 0;
      }

      const activeTier = this.item.seatTiers.find((t: any) => t.seatType === this.chosenSeatType);
      const finalSeatPrice = activeTier ? activeTier.pricePerSeat : this.item.basePricePerSeat;

      return finalSeatPrice * this.selectedPassengerIds.length;
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
        console.error('Session parsing failure.', e);
      }
    }

    if (!currentUserId) {
      this.bookingErrorMessage = 'Your session has expired. Please log in again.';
      this.router.navigate(['/login']);
      return;
    }
    
    const selectedPickup = this.item.matchingSource || this.item.routeFrom;
    const selectedDropoff = this.item.matchingDestination || this.item.routeTo;

    const payload = {
      customerId: currentUserId,
      inventoryId: this.item.inventoryId,
      targetTravelDate: this.date || new Date().toISOString().split('T')[0], 
      chosenSeatType: this.chosenSeatType,
      passengerProfileIds: this.selectedPassengerIds,
      pickupLocation: selectedPickup, 
      dropoffLocation: selectedDropoff   
    };

    this.isLoading = true;
    this.cdr.detectChanges();

    this.travelService.bookBus(payload).subscribe({
      next: (bookingResponse: any) => {
        this.bookingErrorMessage = null;
        const targetBookingId = bookingResponse.bookingId;
        
        if (!targetBookingId) {
          this.bookingErrorMessage = 'Critical: System failed to assign a Booking Tracking Reference ID.';
          this.isLoading = false;
          this.cdr.detectChanges();
          return;
        }
        this.billingService.triggerNotificationRefresh();
        this.router.navigate(['/customer/payment', targetBookingId]);
      },
      error: (err: HttpErrorResponse) => {
        this.bookingErrorMessage = err.error?.message || 'Booking transaction failed. Check seat capacity limits.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
}