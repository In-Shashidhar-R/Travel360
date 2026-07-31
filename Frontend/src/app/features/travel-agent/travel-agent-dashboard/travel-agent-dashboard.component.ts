import { Component, OnInit, ChangeDetectorRef } from '@angular/core'; 
import { CommonModule } from '@angular/common';
import { BookingService } from '../services/booking-service';
import { AuthService } from '../../../core/services/auth.service';
import { BookingRequestResponseDTO } from '../../../shared/models/booking-request.model';
import { RequestListComponent } from './request-list/request-list.component';
import { BookingFormComponent } from './booking-form/booking-form.component';
import { AgentProfileComponent } from './agent-profile/agent-profile.component';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-travel-agent-dashboard',
  standalone: true,
  imports: [CommonModule, RequestListComponent, BookingFormComponent, AgentProfileComponent, FormsModule],
  templateUrl: './travel-agent-dashboard.component.html',
  styleUrls: ['./travel-agent-dashboard.component.scss']
})
export class TravelAgentDashboardComponent implements OnInit {
  currentWorkspaceView: 'QUEUE' | 'PROFILE' = 'QUEUE';
  isBookingViewActive: boolean = false;
  selectedRequestForBooking: BookingRequestResponseDTO | null = null;
  
  bookingRequests: BookingRequestResponseDTO[] = [];
  errorMessage: string | null = null;

  totalPending = 0;
  totalApproved = 0;
  totalCompleted = 0;
  selectedStatusFilter: string = 'ALL'; 
  displayUserName: string = 'Travel Agent';

  constructor(
    private bookingService: BookingService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    const activeSession = localStorage.getItem("travel360_session") || sessionStorage.getItem("travel360_session");
    if (activeSession) {
      const user = this.authService.currentUser();
      if (user && user.name) {
        this.displayUserName = user.name;
      }
      this.loadAssignedQueue();
    } else {
      console.warn("Unauthorized access detected. Triaging back to authentication...");
      this.router.navigate(['/login']);
    }
  }

  loadAssignedQueue(): void {
    this.errorMessage = null;
    this.bookingService.getAssignedRequests(0, 10).subscribe({
      next: (result) => {
        this.bookingRequests = result.content || [];
        this.calculateMetrics();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Failed to load queue from server:', err);
        this.errorMessage = 'Could not load your work queue. Session may have expired.';
        this.cdr.detectChanges();
      }
    });
  }

  calculateMetrics(): void {
    this.totalPending = this.bookingRequests.filter(r => r.status === 'PENDING').length;
    this.totalApproved = this.bookingRequests.filter(r => r.status === 'APPROVED').length;
    this.totalCompleted = this.bookingRequests.filter(r => r.status === 'COMPLETED').length;
    this.cdr.detectChanges();
  }

  toggleView(view: 'QUEUE' | 'PROFILE'): void {
    this.currentWorkspaceView = view;
    this.isBookingViewActive = false;
    this.cdr.detectChanges();
  }

  handleNavigateToBooking(request: BookingRequestResponseDTO): void {
    this.selectedRequestForBooking = request;
    this.isBookingViewActive = true;
    this.cdr.detectChanges();
  }

  handleCancelBookingForm(): void {
    this.isBookingViewActive = false;
    this.selectedRequestForBooking = null;
    this.cdr.detectChanges();
  }
  
  handleRequestAccepted(event: { requestId: number, notes: string }): void {
    this.bookingService.acceptRequest(event.requestId, event.notes).subscribe({
      next: () => this.loadAssignedQueue(),
      error: (err) => {
        this.errorMessage = err.error?.message || 'Unable to update request status.';
        this.cdr.detectChanges();
      }
    });
  }

  handleRequestRejected(event: { requestId: number, notes: string }): void {
    this.bookingService.rejectRequest(event.requestId, event.notes).subscribe({
      next: () => this.loadAssignedQueue(),
      error: (err) => {
        this.errorMessage = err.error?.message || 'Unable to update request status.';
        this.cdr.detectChanges();
      }
    });
  }

  handleBookingSubmitted(event: { requestId: number, payload: any }): void {
    this.bookingService.bookTourPackage(event.requestId, event.payload).subscribe({
      next: (response) => {
        alert(`Booking Successful! Record ID: ${response.resultingBookingId}`);
        this.isBookingViewActive = false;
        this.selectedRequestForBooking = null;
        this.loadAssignedQueue();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Validation failed or internal server error.';
        this.cdr.detectChanges();
      }
    });
  }

  onSignOut(): void {
    this.authService.logout();
  }

  get filteredBookingRequests(): BookingRequestResponseDTO[] {
    if (this.selectedStatusFilter === 'ALL') {
      return this.bookingRequests;
    }
    return this.bookingRequests.filter(r => 
      r.status && r.status.toUpperCase() === this.selectedStatusFilter.toUpperCase()
    );
  }
}