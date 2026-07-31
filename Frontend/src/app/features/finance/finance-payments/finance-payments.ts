import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FinanceService } from '../services/finance.service';

@Component({
  selector: 'app-finance-payments',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './finance-payments.html',
  styleUrl: './finance-payments.scss'
})
export class FinancePayments implements OnInit {
  payments: any[] = [];
  loading = false;

  // Real-time filter parameters
  typeFilter: string = 'ALL';
  searchQuery: string = '';

  constructor(private financeService: FinanceService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadPaymentsData();
  }

  loadPaymentsData(): void {
    this.loading = true;
    this.financeService.getAllPayments().subscribe({
      next: (res: any) => {
        this.payments = res && res.content ? res.content : (Array.isArray(res) ? res : []);
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('[FinancePayments] Failed to synchronize cash flow layers:', err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  onTypeFilterChange(event: Event): void {
    this.typeFilter = (event.target as HTMLSelectElement).value;
    this.cdr.detectChanges();
  }

  onSearchQueryChange(event: Event): void {
    this.searchQuery = (event.target as HTMLInputElement).value.toLowerCase();
    this.cdr.detectChanges();
  }

  // ✅ DYNAMIC PAYMENTS SEARCH ENGINE
  getFilteredPayments(): any[] {
    return this.payments.filter(p => {
      const matchesType = this.typeFilter === 'ALL' || 
                          (p.paymentType && `+ ${p.paymentType}` === this.typeFilter) || 
                          (p.paymentType && `- ${p.paymentType}` === this.typeFilter) ||
                          (p.paymentType && p.paymentType === this.typeFilter);
      
      const method = (p.method || '').toLowerCase();
      const status = (p.status || '').toLowerCase();
      const pId = String(p.paymentId || '');
      const matchesSearch = !this.searchQuery || 
                            method.includes(this.searchQuery) || 
                            status.includes(this.searchQuery) || 
                            pId.includes(this.searchQuery);

      return matchesType && matchesSearch;
    });
  }
}