import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { BookingRequests } from '../booking-requests/booking-requests';
import { BookingsComponent } from '../bookings/bookings';

@Component({
  selector: 'app-booking-dashboard',
  imports: [CommonModule, BookingRequests, BookingsComponent],
  templateUrl: './booking-dashboard.html',
  styleUrl: './booking-dashboard.scss',
})
export class BookingDashboard {
  currentSubTab: 'requests' | 'active-bookings' = 'requests';

  setSubTab(tab: 'requests' | 'active-bookings'): void {
    this.currentSubTab = tab;
  }
}
