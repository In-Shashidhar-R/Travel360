import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { ComplaintResponseDTO } from '../../../../shared/models/compliance.model';

@Component({
  selector: 'app-complaints',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './complaints.html',
  styleUrl: './complaints.scss',
})
export class Complaints implements OnInit {
  complaints: ComplaintResponseDTO[] = [];
  filteredComplaints: ComplaintResponseDTO[] = [];
  loading = false;
  errorMsg = '';

  statusFilter = 'ALL';
  searchQuery = '';

  currentPage: number = 1;
  pageSize: number = 10; 

  constructor(private adminService: AdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.fetchComplaintsRecords();
  }

  fetchComplaintsRecords(): void {
    this.loading = true;
    this.errorMsg = '';
    this.adminService.getSystemComplaints(0, 1000).subscribe({
      next: (res) => {
        this.complaints = res?.content || [];
        this.applyFilters();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'Failed to synchronize administrative complaints registry.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFilters(): void {
    this.currentPage = 1;

    this.filteredComplaints = this.complaints.filter(c => {
      const matchStatus = this.statusFilter === 'ALL' || c.status?.toUpperCase() === this.statusFilter.toUpperCase();
      const q = this.searchQuery.trim().toLowerCase();
      const matchQuery = !q || c.raisedByName?.toLowerCase().includes(q) || c.subject?.toLowerCase().includes(q) || c.complaintId?.toString().includes(q);
      
      return matchStatus && matchQuery;
    });
    this.cdr.detectChanges();
  }

  get paginatedComplaints(): ComplaintResponseDTO[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredComplaints.slice(start, start + this.pageSize);
  }

  getStartIndex(): number {
    return this.filteredComplaints.length === 0 ? 0 : (this.currentPage - 1) * this.pageSize + 1;
  }

  getEndIndex(totalItems: number): number {
    return Math.min(this.currentPage * this.pageSize, totalItems);
  }

  getTotalPages(totalItems: number): number {
    return Math.ceil(totalItems / this.pageSize);
  }

  getPagesArray(totalItems: number): any[] {
    return new Array(this.getTotalPages(totalItems));
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.getTotalPages(this.filteredComplaints.length)) {
      this.currentPage = page;
      this.cdr.detectChanges();
    }
  }
}