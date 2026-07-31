import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FinanceService } from '../services/finance.service';

@Component({
  selector: 'app-finance-invoices',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './finance-invoices.html',
  styleUrl: './finance-invoices.scss'
})
export class FinanceInvoices implements OnInit {
  invoices: any[] = [];
  loading = false;

  // Real-time filter parameters
  statusFilter: string = 'ALL';
  searchQuery: string = '';

  constructor(private financeService: FinanceService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadInvoicesData();
  }

  loadInvoicesData(): void {
    this.loading = true;
    this.financeService.getAllInvoices().subscribe({
      next: (res: any) => {
        // Safely extracts the array from the paginated PageResponse or flat array
        this.invoices = res && res.content ? res.content : (Array.isArray(res) ? res : []);
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('[FinanceInvoices] Failed to resolve ledger index drops:', err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  onStatusFilterChange(event: Event): void {
    this.statusFilter = (event.target as HTMLSelectElement).value;
    this.cdr.detectChanges();
  }

  onSearchQueryChange(event: Event): void {
    this.searchQuery = (event.target as HTMLInputElement).value.toLowerCase().trim();
    this.cdr.detectChanges();
  }

  // ✅ FIXED DYNAMIC FILTER ENGINE: Maps strictly to DTO fields
  getFilteredInvoices(): any[] {
    return this.invoices.filter(inv => {
      // 1. Status Filter Check (PAID, UNPAID, REFUNDED)
      const matchesStatus = this.statusFilter === 'ALL' || 
                            (inv.status && inv.status.toUpperCase() === this.statusFilter.toUpperCase());
      
      // 2. Search Query String Check (Safe string conversion to prevent crashes)
      const customer = inv.customerName ? String(inv.customerName).toLowerCase() : 'harash thapa';
      const bookingId = inv.bookingId ? String(inv.bookingId).toLowerCase() : '';
      const invoiceId = inv.invoiceId ? String(inv.invoiceId).toLowerCase() : '';
      
      const matchesSearch = !this.searchQuery || 
                            customer.includes(this.searchQuery) || 
                            bookingId.includes(this.searchQuery) || 
                            invoiceId.includes(this.searchQuery);

      return matchesStatus && matchesSearch;
    });
  }
}