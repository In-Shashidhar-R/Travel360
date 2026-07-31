import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import {
  ComplaintResolveDTO,
  ComplaintResponseDTO
} from '../models/compliance.model';
import { ComplianceService } from '../../../core/services/compliance';

@Component({
  selector: 'app-complaints',
  imports: [CommonModule, FormsModule],
  templateUrl: './complaints.html',
  styleUrl: './complaints.scss',
})
export class Complaints {
  complaints: ComplaintResponseDTO[] = [];
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  loading = false;

  selectedComplaintId: number | null = null;
  noteText: string = '';

  constructor(
    private complianceService: ComplianceService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadComplaints();
  }

  loadComplaints(): void {
    this.loading = true;
    this.cdr.detectChanges();

    this.complianceService
      .getAllComplaints(this.currentPage, this.pageSize)
      .subscribe({
        next: (res) => {
          this.complaints = res.content;
          this.totalPages = res.totalPages;
          this.loading = false;

          this.cdr.detectChanges();
        },

        error: (err) => {
          this.loading = false;

          console.error('Error loading complaints:', err);
          alert(err.error?.message || 'Failed to load complaints');

          this.cdr.detectChanges();
        },

        complete: () => {
          this.cdr.detectChanges();
        }
      });
  }

  selectComplaint(id: number): void {
    this.selectedComplaintId = id;
    this.noteText = '';
  }

  updateStatus(action: 'process' | 'resolve'): void {
    if (!this.selectedComplaintId || !this.noteText.trim()) {
      alert('Please enter a handling/resolution note first.');
      return;
    }

    const payload: ComplaintResolveDTO = {
      resolutionNote: this.noteText
    };

    this.loading = true;
    this.cdr.detectChanges();

    if (action === 'process') {
      this.complianceService
        .markInProgress(this.selectedComplaintId, payload)
        .subscribe({
          next: () => {
            this.loading = false;

            alert('Complaint marked as In-Progress.');
            this.resetFormAndReload();

            this.cdr.detectChanges();
          },

          error: (err) => {
            this.loading = false;

            console.error('Mark In-Progress Error:', err);
            alert(err.error?.message || 'Failed to update complaint');

            this.cdr.detectChanges();
          },

          complete: () => {
            this.cdr.detectChanges();
          }
        });
    } else {
      this.complianceService
        .resolveComplaint(this.selectedComplaintId, payload)
        .subscribe({
          next: () => {
            this.loading = false;

            alert('Complaint marked as Resolved.');
            this.resetFormAndReload();

            this.cdr.detectChanges();
          },

          error: (err) => {
            this.loading = false;

            console.error('Resolve Complaint Error:', err);
            alert(err.error?.message || 'Failed to resolve complaint');

            this.cdr.detectChanges();
          },

          complete: () => {
            this.cdr.detectChanges();
          }
        });
    }
  }

  resetFormAndReload(): void {
    this.selectedComplaintId = null;
    this.noteText = '';

    this.cdr.detectChanges();

    this.loadComplaints();
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadComplaints();
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadComplaints();
    }
  }
}