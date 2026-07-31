import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TravelService } from '../../../core/services/travel-service';
import { HotelInventoryResponseDTO } from '../../../shared/models/inventory.model';

@Component({
  selector: 'app-hotel-details',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './hotel-details.html',
  styleUrl: './hotel-details.scss'
})
export class HotelDetails implements OnInit {
  @Input() item: HotelInventoryResponseDTO | any = null;
  @Input() checkIn: string = '';
  @Input() checkOut: string = '';
  @Input() searchPrice: number = 0; 

  isLoading = false;
  bookingErrorMessage: string | null = null;

  checkInDate: string = '';
  checkOutDate: string = '';
  requestedRooms: number = 1;

  constructor(
    private travelService: TravelService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    // 🔍 STRUCTURAL COMPONENT PAYLOAD LOG INSPECTION
    console.group('🏨 HotelDetails Data Input Verification');
    console.log('1. Complete item Data Map Payload:', this.item);
    console.log('2. Available Rooms Value Check:', this.item?.availableRooms);
    console.log('3. Room Capacity Field Check (Fallback Names):', {
      totalRooms: this.item?.totalRooms,
      roomsAvailable: this.item?.roomsAvailable,
      vacantRooms: this.item?.vacantRooms
    });
    console.log('4. Parent Component Search Context:', {
      checkInDate: this.checkIn,
      checkOutDate: this.checkOut,
      searchPrice: this.searchPrice
    });
    console.groupEnd();

    const today = new Date().toISOString().split('T')[0];
    this.checkInDate = this.checkIn || today;
    
    if (this.checkOut) {
      this.checkOutDate = this.checkOut;
    } else {
      const nextDay = new Date(this.checkInDate);
      nextDay.setDate(nextDay.getDate() + 1);
      this.checkOutDate = nextDay.toISOString().split('T')[0];
    }
    
    this.cdr.detectChanges();
  }

  incrementRooms(): void {
    this.requestedRooms++;
    this.cdr.detectChanges(); 
  }

  decrementRooms(): void {
    if (this.requestedRooms > 1) {
      this.requestedRooms--;
      this.cdr.detectChanges(); 
    }
  }

  get calculateStayDurationDays(): number {
    if (!this.checkInDate || !this.checkOutDate) return 0;
    const start = new Date(this.checkInDate);
    const end = new Date(this.checkOutDate);
    const differenceInTime = end.getTime() - start.getTime();
    const duration = Math.ceil(differenceInTime / (1000 * 3600 * 24));
    return duration > 0 ? duration : 0;
  }

  get calculateTotalBookingPrice(): number {
    if (this.calculateStayDurationDays <= 0) return 0;
    const nightlyRate = this.searchPrice || (this.item ? this.item.basePricePerSeat : 0);
    return nightlyRate * this.requestedRooms * this.calculateStayDurationDays;
  }

  executeBookingTransaction(): void {
    if (this.calculateStayDurationDays <= 0) {
      this.bookingErrorMessage = 'Check-out date must succeed your check-in date criteria windows.';
      this.cdr.detectChanges();
      return;
    }

    this.bookingErrorMessage = null;
    const sessionUser = localStorage.getItem('travel360_session');
    let currentUserId: number | null = null;

    if (sessionUser) {
      try {
        currentUserId = JSON.parse(sessionUser).userId;
      } catch (e) {
        console.error('Session access parsing failure.', e);
      }
    }

    if (!currentUserId) {
      this.bookingErrorMessage = 'Session expired. Please log in again.';
      this.router.navigate(['/login']);
      this.cdr.detectChanges();
      return;
    }

    const payload = {
      customerId: currentUserId,
      inventoryId: this.item?.inventoryId,
      checkInDate: this.checkInDate,
      checkOutDate: this.checkOutDate,
      requestedRooms: this.requestedRooms,
      passengerProfileIds: []
    };

    // 🔍 TRANSACTION PAYLOAD SEND INSPECTION
    console.log('🚀 Finalizing Hotel Booking Service Request Payload:', payload);

    this.isLoading = true;
    this.cdr.detectChanges();

    this.travelService.bookHotel(payload).subscribe({
      next: (bookingResponse: any) => {
        const invoiceId = bookingResponse.invoiceId || bookingResponse.bookingId;
        alert('Hotel room reserved successfully!');
        
        this.isLoading = false;
        this.cdr.detectChanges();
        
        this.router.navigate(['/customer/payment', invoiceId]);
      },
      error: (err: HttpErrorResponse) => {
        this.bookingErrorMessage = err.error?.message || 'Allocation limit exceeded or invalid timeline selection.';
        this.isLoading = false; 
        this.cdr.detectChanges();
      }
    });
  }
}