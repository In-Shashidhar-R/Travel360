import { Component, OnInit, inject, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TravelService } from '../../../core/services/travel-service';
import { AuthService } from '../../../core/services/auth.service';
import { ComplaintResponseDTO, PageResponse } from '../../../shared/models/compliance.model';

@Component({
  selector: 'app-cust-compliants',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cust-compliants.html',
  styleUrl: './cust-compliants.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CustCompliants implements OnInit {
  private travelService = inject(TravelService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  complaints: ComplaintResponseDTO[] = [];
  isLoading: boolean = false;
  errorMessage: string = '';

  currentPage: number = 0;
  pageSize: number = 5;
  totalPages: number = 0;
  totalElements: number = 0;

  ngOnInit(): void {
    this.loadComplaints();
  }

  loadComplaints(page: number = 0): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.currentPage = page;
    
    this.cdr.markForCheck();

    this.travelService.getMyComplaints(this.currentPage, this.pageSize).subscribe({
      next: (response: PageResponse<ComplaintResponseDTO>) => {
        this.complaints = response.content || [];
        this.totalPages = response.totalPages || 0;
        this.totalElements = response.totalElements || 0;
        this.isLoading = false;

        // Force Angular Change Detection to update view immediately
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.errorMessage = 'Failed to load support complaints statement.';
        this.isLoading = false;

        // Force Angular Change Detection to render error view
        this.cdr.markForCheck();
      }
    });
  }

  onRefresh(): void {
    this.loadComplaints(this.currentPage);
  }

  onPageChange(newPage: number): void {
    if (newPage >= 0 && newPage < this.totalPages) {
      this.loadComplaints(newPage);
    }
  }

  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }
}