import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TravelService } from '../../../core/services/travel-service';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-cust-requests',
  standalone: true,
  imports: [CommonModule,RouterLink],
  templateUrl: './cust-requests.html',
  styleUrl: './cust-requests.scss'
})
export class CustRequests implements OnInit {
  requests: any[] = [];
  isLoading = false;
  errorMessage: string | null = null;

  // Pagination mechanics state parameters
  currentPage = 0;
  pageSize = 5;
  totalPages = 0;
  totalElements = 0;
  isLastPage = false;

  // 🎯 NEW: Selected Row Active Object Tracker For Dialog Renders
  selectedRequest: any | null = null;
 
  constructor(
    private travelService: TravelService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.fetchRequests(0);
  }

  fetchRequests(pageIndex: number = 0): void {
    this.currentPage = pageIndex;
    this.isLoading = true;
    this.errorMessage = null;
    this.requests = [];

    this.travelService.getMyBookingRequests(this.currentPage, this.pageSize).subscribe({
      next: (response: any) => {
        this.requests = response.content || [];
        this.totalPages = response.totalPages || 1;
        this.totalElements = response.totalElements || this.requests.length;
        this.isLastPage = response.last !== undefined ? response.last : (this.currentPage >= this.totalPages - 1);
        
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message || 'Failed to retrieve your custom requirements logs.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  openDetailsModal(requestItem: any): void {
    this.selectedRequest = requestItem;
    this.cdr.detectChanges();

    console.log(this.selectedRequest)
  }

  closeDetailsModal(): void {
    this.selectedRequest = null;
    this.cdr.detectChanges();
  }

  goToNextPage(): void { if (!this.isLastPage) this.fetchRequests(this.currentPage + 1); }
  goToPreviousPage(): void { if (this.currentPage > 0) this.fetchRequests(this.currentPage - 1); }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'bg-warning text-dark';
      case 'APPROVED': return 'bg-info text-white';
      case 'REJECTED': return 'bg-danger text-white';
      case 'COMPLETED': return 'bg-success text-white';
      default: return 'bg-secondary text-white';
    }
  }
}