import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { BaseBookingResponseDTO } from '../../../../shared/models/booking.model';

@Component({
  selector: 'app-bookings',
  imports: [CommonModule, FormsModule],
  templateUrl: './bookings.html',
  styleUrl: './bookings.scss',
})
export class BookingsComponent implements OnInit {
 bookings: BaseBookingResponseDTO[] = [];
  filteredBookings: BaseBookingResponseDTO[] = [];
  loading = false;
  errorMessage = '';

  page = 0;
  size = 50; 
  totalElements = 0;

  typeFilter = 'ALL';
  statusFilter = 'ALL';
  customerSearch = '';
  partnerSearch = '';

  constructor(
    private adminService: AdminService, 
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadServerBookings();
  }

  loadServerBookings(): void {
    this.loading = true;
    this.errorMessage = '';
    this.adminService.getAllBookings(this.page, this.size).subscribe({
      next: (res) => {
        this.bookings = res?.content || [];
        this.totalElements = res?.totalElements || 0;
        this.applyFiltersMatrix();
        this.loading = false;
        
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to load global transaction records from server node.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFiltersMatrix(): void {
    this.filteredBookings = this.bookings.filter(b => {
      const matchType = this.typeFilter === 'ALL' || b.itemType === this.typeFilter;
      const matchStatus = this.statusFilter === 'ALL' || b.status?.toUpperCase() === this.statusFilter.toUpperCase();
      
      const cQuery = this.customerSearch.trim().toLowerCase();
      const matchCustomer = !cQuery || b.customerId?.toString().includes(cQuery) || b.customerName?.toLowerCase().includes(cQuery);

      const pQuery = this.partnerSearch.trim().toLowerCase();
      const matchPartner = !pQuery || b.partnerId?.toString().includes(pQuery) || b.partnerName?.toLowerCase().includes(pQuery);

      return matchType && matchStatus && matchCustomer && matchPartner;
    });
  }
}