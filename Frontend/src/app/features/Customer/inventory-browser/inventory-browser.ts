import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { InventoryDataset, FlightInventoryResponseDTO, BusInventoryResponseDTO, HotelInventoryResponseDTO, TourInventoryResponseDTO } from '../../../shared/models/inventory.model'; 
import { TravelSearch } from '../../../shared/models/travelSearch';
import { TravelService } from '../../../core/services/travel-service';
import { PageResponse } from '../../../shared/models/booking.model';

@Component({
  selector: 'app-inventory-browser',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './inventory-browser.html',
  styleUrl: './inventory-browser.scss'
})
export class InventoryBrowser implements OnInit {
  selectedType: 'FLIGHT' | 'HOTEL' | 'BUS' | 'TOUR_PACKAGE' = 'FLIGHT';
  source = ''; destination = ''; state = ''; district = '';
  targetDate = ''; checkInDate = ''; checkOutDate = ''; todayMinDate = '';

  currentPage = 0; pageSize = 5; totalPages = 0; totalElements = 0; isLastPage = false;
  numberOfPersons = 1;

  searchResults: InventoryDataset[] = [];
  searchPerformed = false; message = ''; isLoading = false;

  constructor(private travelService: TravelService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    const today = new Date();
    this.todayMinDate = today.toISOString().split('T')[0];
    this.targetDate = this.checkInDate = this.todayMinDate;
    
    today.setDate(today.getDate() + 1);
    this.checkOutDate = today.toISOString().split('T')[0];
  }

  get isSearchFormInvalid(): boolean {
    if (this.selectedType === 'TOUR_PACKAGE') return !this.targetDate || this.numberOfPersons < 1;
    if (this.selectedType === 'HOTEL') {
      if (!this.checkInDate || !this.checkOutDate || !this.state.trim() || !this.district.trim()) return true;
      return new Date(this.checkOutDate) <= new Date(this.checkInDate);
    }
    return !this.targetDate || !this.source.trim() || !this.destination.trim();
  }

  changeCategory(type: 'FLIGHT' | 'HOTEL' | 'BUS' | 'TOUR_PACKAGE'): void {
    this.selectedType = type;
    this.currentPage = 0;
    this.searchResults = [];
    this.searchPerformed = false;
    this.message = this.source = this.destination = this.state = this.district = '';
    this.numberOfPersons = 1;
    this.ngOnInit(); // Resets dates cleanly
  }

search(pageIndex: number = 0): void {
    if (this.isSearchFormInvalid) return;

    this.currentPage = pageIndex;
    this.searchPerformed = this.isLoading = true;
    this.message = '';
    this.searchResults = [];

    const criteria: TravelSearch = {
      itemType: this.selectedType,
      source: this.source.trim(),
      destination: this.destination.trim(),
      targetDate: this.selectedType === 'HOTEL' ? this.checkInDate : this.targetDate
    };

    this.travelService.filterInventories(criteria, this.currentPage, this.pageSize).subscribe({
      next: (response: PageResponse<InventoryDataset> | any) => {
        const rawList = response.content || response;
        
        // 🛑 CRITICAL FIX: Extract and immediately eliminate 'INACTIVE' items case-insensitively
        const list: InventoryDataset[] = (Array.isArray(rawList) ? rawList : []).filter(
          (item: any) => item.status?.toUpperCase() !== 'INACTIVE'
        );

        console.log(criteria);

        if (this.selectedType === 'HOTEL' && !response.content) {
          this.searchResults = list.filter((item: any) => item.itemType === 'HOTEL' && 
            item.state?.toLowerCase() === this.state.trim().toLowerCase() && 
            item.district?.toLowerCase() === this.district.trim().toLowerCase());
          this.updatePagination(1, this.searchResults.length, true);
        } else if (this.selectedType === 'BUS') {
          const matched = this.executeAdvancedRouteMatching(list as BusInventoryResponseDTO[], this.source, this.destination);
          if (matched.length === 0) return this.runFullInventoryBusFallback();
          
          this.searchResults = matched;
          this.updatePagination(response.totalPages || 1, matched.length, response.last ?? true);
        } else {
          this.searchResults = list;
          this.updatePagination(response.totalPages || 1, this.searchResults.length, response.last ?? true);
        }
        this.updateSearchFeedback();
      },
      error: () => this.handleSearchError()
    });
  }

  private runFullInventoryBusFallback(): void {
    this.travelService.getAllInventories(this.targetDate).subscribe({
      next: (all) => {
        const activeAll = (all || []).filter((item: any) => item.status?.toUpperCase() !== 'INACTIVE');
        
        const rawBuses = activeAll.filter(item => item.itemType === 'BUS') as BusInventoryResponseDTO[];
        this.searchResults = this.executeAdvancedRouteMatching(rawBuses, this.source, this.destination);
        this.updatePagination(1, this.searchResults.length, true);
         
        console.log("⚠️ DATA SOURCE MATRIX: Filter mismatch fallback triggered. Resolved via full inventory stream scan", this.searchResults);
         
        this.updateSearchFeedback();
      },
      error: () => this.handleSearchError()
    });
  }

  private executeAdvancedRouteMatching(buses: BusInventoryResponseDTO[], src: string, dest: string): BusInventoryResponseDTO[] {
    const sQ = src.toLowerCase().trim();
    const dQ = dest.toLowerCase().trim();

    return buses.filter(bus => {
      // Find indexes using native JS find/findIndex array utilities instead of verbose loop counters
      const pIdx = (bus.routeFrom || '').toLowerCase().includes(sQ) ? -1 
                 : bus.routeStops?.findIndex(s => s.stopType === 'PICKUP' && s.stopName?.toLowerCase().includes(sQ)) ?? -2;
      
      const dIdx = (bus.routeTo || '').toLowerCase().includes(dQ) ? (bus.routeStops?.length || 0) 
                 : bus.routeStops?.findIndex(s => s.stopType === 'DROPOFF' && s.stopName?.toLowerCase().includes(dQ)) ?? -2;

      const isValid = pIdx !== -2 && dIdx !== -2 && pIdx < dIdx;
      if (isValid) {
        (bus as any).matchingSource = pIdx === -1 ? bus.routeFrom : bus.routeStops[pIdx].stopName;
        (bus as any).matchingDestination = dIdx === (bus.routeStops?.length || 0) ? bus.routeTo : bus.routeStops[dIdx].stopName;
      }
      return isValid;
    });
  }

  private updatePagination(totalP: number, totalE: number, last: boolean): void {
    this.totalPages = totalP;
    this.totalElements = totalE;
    this.isLastPage = last;
  }

  private updateSearchFeedback(): void {
    this.message = this.searchResults.length === 0 ? `No available ${this.selectedType.toLowerCase().replace('_', ' ')} found.` : '';
    this.isLoading = false;
    this.cdr.detectChanges();
  }

  private handleSearchError(): void {
    this.message = 'Unable to extract inventory models.';
    this.isLoading = false;
    this.searchResults = [];
    this.cdr.detectChanges();
  }

  goToNextPage(): void { if (!this.isLastPage) this.search(this.currentPage + 1); }
  goToPreviousPage(): void { if (this.currentPage > 0) this.search(this.currentPage - 1); }

  isFlight(item: InventoryDataset): item is FlightInventoryResponseDTO { return item.itemType === 'FLIGHT'; }
  isBus(item: InventoryDataset): item is BusInventoryResponseDTO { return item.itemType === 'BUS'; }
  isHotel(item: InventoryDataset): item is HotelInventoryResponseDTO { return item.itemType === 'HOTEL'; }
  isTour(item: InventoryDataset): item is TourInventoryResponseDTO { return item.itemType === 'TOUR_PACKAGE'; }
}