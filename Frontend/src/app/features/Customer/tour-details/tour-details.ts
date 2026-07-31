import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TravelService } from '../../../core/services/travel-service';
import { TourInventoryResponseDTO } from '../../../shared/models/inventory.model';
import { PassengerDirectory } from '../passenger-directory/passenger-directory'; 
import { FormsModule } from '@angular/forms';
import { BillingService } from '../../../core/services/billing-service';

@Component({
  selector: 'app-tour-details',
  standalone: true,
  imports: [CommonModule, RouterModule, PassengerDirectory, FormsModule],
  templateUrl: './tour-details.html',
  styleUrl: './tour-details.scss'
})
export class TourDetails implements OnInit {
  @Input() item: TourInventoryResponseDTO | any = null;
  @Input() date: string = ''; 
  @Input() searchPrice: number = 0;

  isLoading = false;
  bookingErrorMessage: string | null = null;

  targetTravelDate: string = '';
  basePrice: number = 0;
  
  selectedPassengerIds: number[] = [];

  wantsPersonalization = false;
  customerRequirementsText = '';

  constructor(
    private travelService: TravelService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private route: ActivatedRoute,
    private billingService: BillingService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.targetTravelDate = params['date'] || this.date || new Date().toISOString().split('T')[0];
      this.basePrice = params['price'] ? Number(params['price']) : (this.item ? this.item.basePricePerSeat : 0);
    });

    if (!this.item) {
      this.item = history.state?.item || null;
    }
  }

  get parsedItineraryDays(): { dayTitle: string; dayContent: string }[] {
    if (!this.item?.fullItineraryDetails) return [];
    const rawText = this.item.fullItineraryDetails;
    const dayMarkerRegex = /(Day\s+\d+\s*:\s*)/g;
    const textSegments = rawText.split(dayMarkerRegex);
    const parsedDays: { dayTitle: string; dayContent: string }[] = [];

    for (let i = 1; i < textSegments.length; i += 2) {
      const title = textSegments[i].trim();
      let content = textSegments[i + 1] ? textSegments[i + 1].trim() : '';
      if (content.endsWith('.')) content = content.slice(0, -1).trim();
      
      parsedDays.push({ dayTitle: title, dayContent: content });
    }
    return parsedDays;
  }

  get calculateTotalPackagePrice(): number {
    if (!this.item || this.selectedPassengerIds.length === 0) return 0;
    const rate = this.basePrice || this.item.basePricePerSeat || 0;
    return rate * this.selectedPassengerIds.length;
  }

  onPassengerSelected(ids: number[]): void {
    this.selectedPassengerIds = ids;
    this.bookingErrorMessage = null;
    this.cdr.detectChanges();
  }

  executeTourBookingTransaction(): void {
    if (this.selectedPassengerIds.length === 0) {
      this.bookingErrorMessage = 'Manifest Empty: Choose at least one traveler from your directory ledger.';
      return;
    }

    this.bookingErrorMessage = null;
    const sessionUser = localStorage.getItem('travel360_session');
    if (!sessionUser) {
      this.router.navigate(['/login']);
      return;
    }
    const currentUserId = JSON.parse(sessionUser).userId;

    const payload = {
      customerId: currentUserId,
      inventoryId: this.item?.inventoryId,
      targetTravelDate: this.targetTravelDate,
      numberOfPersons: this.selectedPassengerIds.length,
      passengerProfileIds: this.selectedPassengerIds
    };

    this.isLoading = true;
    this.cdr.detectChanges();

    this.travelService.bookTour(payload).subscribe({
      next: (response: any) => {
        alert('Vacation package reservation requested successfully!');
        this.isLoading = false;
        this.cdr.detectChanges();
        const referenceInvoiceId = response.bookingId || response.invoiceId;
        this.router.navigate(['/customer/payment', referenceInvoiceId]);
      },
      error: (err: HttpErrorResponse) => {
        this.bookingErrorMessage = err.error?.message || err.error || 'Allocation constraint breach.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // 🎯 NEW: Submits the user requirements to the backend com.cts.controller API endpoint
  submitAgentPersonalizationRequest(): void {
    if (!this.customerRequirementsText.trim()) {
      this.bookingErrorMessage = 'Please describe your custom adjustments / modification requirements before sending.';
      return;
    }

    this.bookingErrorMessage = null;
    this.isLoading = true;
    this.cdr.detectChanges();

    const payload = {
      inventoryId: this.item?.inventoryId,
      customerRequirements: this.customerRequirementsText.trim()
    };

    this.travelService.createBookingRequest(payload).subscribe({
      next: (res: any) => {
        alert(`Success! Your request has been logged with status: ${res.status}. Agent ${res.assignedAgentName} has been notified.`);
        this.billingService.triggerNotificationRefresh();
        this.isLoading = false;
        this.wantsPersonalization = false;
        this.customerRequirementsText = '';
        this.cdr.detectChanges();
        this.router.navigate(['/customer/inventories']);
      },
      error: (err: HttpErrorResponse) => {
        this.bookingErrorMessage = err.error?.message || 'Failed to submit personalization request to the travel agent.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
}