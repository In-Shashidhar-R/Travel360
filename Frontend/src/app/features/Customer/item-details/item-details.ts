import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { TravelService } from '../../../core/services/travel-service';
import { FlightDetails } from '../flight-details/flight-details';
import { HotelDetails } from '../hotel-details/hotel-details';
import { BusDetails } from '../bus-details/bus-details';
import { TourDetails } from '../tour-details/tour-details'; // 🚀 Import your Tour Details component

@Component({
  selector: 'app-item-details',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    FlightDetails, 
    HotelDetails, 
    BusDetails,
    TourDetails // 🚀 Register Tour Details in the standalone imports array
  ],
  templateUrl: './item-details.html'
})
export class ItemDetailsComponent implements OnInit {
  item: any | null = null;
  isLoading = true;
  errorMessage = '';
  
  confirmedTravelDate = '';
  confirmedCheckOut = '';
  dynamicSearchPrice = 0; 
  confirmedHeadCount = 1; // 🚀 Added headCount property for tour package manifest sizing

  constructor(
    private route: ActivatedRoute,
    private travelService: TravelService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    
   // 1. Keep your existing string extractions from the query parameters
this.confirmedTravelDate = this.route.snapshot.queryParamMap.get('date') || '';
this.confirmedCheckOut = this.route.snapshot.queryParamMap.get('checkOut') || '';
this.dynamicSearchPrice = Number(this.route.snapshot.queryParamMap.get('price')) || 0;
this.confirmedHeadCount = Number(this.route.snapshot.queryParamMap.get('headCount')) || 1; 

if (id) {
  // 2. Convert the string date to a JavaScript Date object safely for the service call
  let travelDateObj: Date | undefined = undefined;
  
  if (this.confirmedTravelDate) {
    travelDateObj = new Date(this.confirmedTravelDate);
  }

  // 3. Pass the Date object to the method
  this.loadItemDetails(id, travelDateObj);
} else {
  this.errorMessage = 'Invalid Identity Reference Parameter Key.';
  this.isLoading = false;
}
  }

 loadItemDetails(id: number, targetDate?: Date): void {
  this.isLoading = true; // Best practice to set loading state when a request starts!
  
  this.travelService.getInventoryById(id, targetDate).subscribe({
    next: (data: any) => {
      this.item = data;
      this.isLoading = false;
      this.cdr.detectChanges();
      console.log('Inventory Details:', data);
    },
    error: (err) => {
      console.error('Error fetching inventory:', err);
      this.errorMessage = 'Unable to extract detailed itinerary specifications.';
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  });
}

  // 🚀 Expanded type safety getter mapping to detect Tour Packages
  get categoryType(): 'FLIGHT' | 'HOTEL' | 'BUS' | 'TOUR_PACKAGE' | null {
    if (!this.item) return null;
    if ('airlineName' in this.item || 'flightNumber' in this.item) return 'FLIGHT';
    if ('hotelName' in this.item || 'roomType' in this.item) return 'HOTEL';
    if ('operatorName' in this.item || 'routeFrom' in this.item) return 'BUS';
    if ('packageName' in this.item || 'fullItineraryDetails' in this.item) return 'TOUR_PACKAGE'; // 🎯 Duck-typing check for Tour Packages
    return null;
  }
}