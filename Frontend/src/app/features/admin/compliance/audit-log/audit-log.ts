import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { AuditLogResponseDTO } from '../../../../shared/models/compliance.model';

@Component({
  selector: 'app-audit-log',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './audit-log.html',
  styleUrl: './audit-log.scss',
})
export class AuditLog implements OnInit {
  logs: AuditLogResponseDTO[] = [];
  filteredLogs: AuditLogResponseDTO[] = [];
  loading = false;
  errorMsg = '';

  levelFilter = 'ALL';
  resourceFilter = 'ALL';
  userSearch = '';
  
  startDateFilter: string = '';
  endDateFilter: string = '';
  
  maxDate: string = '';

  currentPage: number = 1;
  pageSize: number = 10;

  constructor(private adminService: AdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.calculateMaxDateConstraint();
    this.fetchAuditTrail();
  }

  calculateMaxDateConstraint(): void {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    this.maxDate = `${yyyy}-${mm}-${dd}`;
  }

  fetchAuditTrail(): void {
    this.loading = true;
    this.errorMsg = '';
    this.adminService.getSystemAuditLogs(0, 1000).subscribe({
      next: (res) => {
        this.logs = res?.content || [];
        this.applyFilters();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'Failed to fetch global security log data records.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFilters(): void {
    this.currentPage = 1;

    this.filteredLogs = this.logs.filter(log => {
      const matchLevel = this.levelFilter === 'ALL' || log.eventLevel?.toUpperCase() === this.levelFilter.toUpperCase();
      const matchResource = this.resourceFilter === 'ALL' || log.resourceType?.toUpperCase() === this.resourceFilter.toUpperCase();
      
      const q = this.userSearch.trim().toLowerCase();
      const matchUser = !q || 
                        log.userName?.toLowerCase().includes(q) || 
                        log.userId?.toString().includes(q) || 
                        log.action?.toLowerCase().includes(q);
      
      let matchDate = true;
      if (log.timestamp) {
        const logTime = new Date(log.timestamp).getTime();
        
        if (this.startDateFilter) {
          const startTime = new Date(this.startDateFilter + 'T00:00:00').getTime();
          if (logTime < startTime) matchDate = false;
        }
        
        if (this.endDateFilter) {
          const endTime = new Date(this.endDateFilter + 'T23:59:59').getTime();
          if (logTime > endTime) matchDate = false;
        }
      }

      return matchLevel && matchResource && matchUser && matchDate;
    });
    this.cdr.detectChanges();
  }

  get paginatedLogs(): AuditLogResponseDTO[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredLogs.slice(start, start + this.pageSize);
  }

  getStartIndex(): number {
    return this.filteredLogs.length === 0 ? 0 : (this.currentPage - 1) * this.pageSize + 1;
  }

  getEndIndex(totalItems: number): number {
    return Math.min(this.currentPage * this.pageSize, totalItems);
  }

  getTotalPages(totalItems: number): number {
    return Math.ceil(totalItems / this.pageSize);
  }

  getVisiblePages(totalItems: number): number[] {
    const totalPages = this.getTotalPages(totalItems);
    const visibleCount = 5; 
    
    if (totalPages <= visibleCount) {
      return Array.from({ length: totalPages }, (_, i) => i + 1);
    }

    let startPage = this.currentPage - 2;
    let endPage = this.currentPage + 2;

    if (startPage <= 0) {
      startPage = 1;
      endPage = visibleCount;
    } else if (endPage > totalPages) {
      endPage = totalPages;
      startPage = totalPages - visibleCount + 1;
    }

    const pages: number[] = [];
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    return pages;
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.getTotalPages(this.filteredLogs.length)) {
      this.currentPage = page;
      this.cdr.detectChanges();
    }
  }
}