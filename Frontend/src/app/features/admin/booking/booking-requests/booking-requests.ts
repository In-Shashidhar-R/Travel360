import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { BookingRequestResponseDTO } from '../../../../shared/models/booking-request.model';

@Component({
  selector: 'app-booking-requests',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking-requests.html',
  styleUrls: ['./booking-requests.scss']
})
export class BookingRequests implements OnInit {
  bookingRequests: BookingRequestResponseDTO[] = [];
  filteredRequests: BookingRequestResponseDTO[] = [];
  loading = false;
  errorMessage = '';
  
  statusFilter = 'ALL';
  customerSearch = '';
  agentSearch = '';

  constructor(
    private adminService: AdminService, 
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.fetchRequests();
  }

  fetchRequests(): void {
    this.loading = true;
    this.errorMessage = '';
    this.adminService.getAllBookingRequests().subscribe({
      next: (data) => {
        this.bookingRequests = data || [];
        this.applyFilter();
        this.loading = false;
    
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching booking requests:', err);
        this.errorMessage = 'Failed to load system booking requests. Please check connection credentials.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFilter(): void {
    this.filteredRequests = this.bookingRequests.filter(req => {
      const matchesStatus = this.statusFilter === 'ALL' || 
        req.status?.toUpperCase() === this.statusFilter.toUpperCase();
      
      const cQuery = this.customerSearch.trim().toLowerCase();
      const matchesCustomer = !cQuery || 
        req.customerId?.toString().includes(cQuery) || 
        req.customerName?.toLowerCase().includes(cQuery);
        
      const aQuery = this.agentSearch.trim().toLowerCase();
      const matchesAgent = !aQuery || 
        req.assignedAgentId?.toString().includes(aQuery) || 
        req.assignedAgentName?.toLowerCase().includes(aQuery);

      return matchesStatus && matchesCustomer && matchesAgent;
    });
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'PENDING': return 'badge-pending';
      case 'APPROVED': return 'badge-approved';
      case 'REJECTED': return 'badge-rejected';
      case 'COMPLETED': return 'badge-completed';
      default: return 'badge-secondary';
    }
  }
}