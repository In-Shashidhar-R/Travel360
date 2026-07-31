import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { InvoiceResponseDTO } from '../../../../shared/models/finance.model';

@Component({
  selector: 'app-finance-invoices',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './finance-invoices.html',
  styleUrl: './finance-invoices.scss',
})
export class FinanceInvoices implements OnInit {
  invoices: InvoiceResponseDTO[] = [];
  filteredInvoices: InvoiceResponseDTO[] = [];
  loading = false;
  errorMsg = '';

  statusFilter = 'ALL';
  clientSearch = '';

  constructor(private adminService: AdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.fetchInvoicesRecord();
  }

  fetchInvoicesRecord(): void {
    this.loading = true;
    this.errorMsg = '';
    this.adminService.getAllInvoices(0, 100).subscribe({
      next: (res) => {
        this.invoices = res?.content || [];
        this.applyFilters();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'Failed to download master invoice registry.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFilters(): void {
    this.filteredInvoices = this.invoices.filter(inv => {
      const matchStatus = this.statusFilter === 'ALL' || inv.status?.toUpperCase() === this.statusFilter.toUpperCase();
      const q = this.clientSearch.trim().toLowerCase();
      const matchClient = !q || inv.customerName?.toLowerCase().includes(q) || inv.bookingId?.toString().includes(q);
      return matchStatus && matchClient;
    });
    this.cdr.detectChanges();
  }
}