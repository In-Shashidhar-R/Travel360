import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { PaymentResponseDTO } from '../../../../shared/models/finance.model';

@Component({
  selector: 'app-finance-payments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './finance-payments.html',
  styleUrl: './finance-payments.scss',
})
export class FinancePayments implements OnInit {
  payments: PaymentResponseDTO[] = [];
  filteredPayments: PaymentResponseDTO[] = [];
  loading = false;
  errorMsg = '';

  typeFilter = 'ALL';
  invoiceSearch = '';

  constructor(private adminService: AdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.fetchPaymentsBook();
  }

  fetchPaymentsBook(): void {
    this.loading = true;
    this.errorMsg = '';
    this.adminService.getAllPayments(0, 100).subscribe({
      next: (res) => {
        this.payments = res?.content || [];
        this.applyFilters();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.errorMsg = 'Failed to extract administrative settlement logs.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  applyFilters(): void {
    this.filteredPayments = this.payments.filter(pay => {
      const matchType = this.typeFilter === 'ALL' || pay.paymentType?.toUpperCase() === this.typeFilter.toUpperCase();
      const matchInvoice = !this.invoiceSearch.trim() || pay.invoiceId?.toString().includes(this.invoiceSearch.trim());
      return matchType && matchInvoice;
    });
    this.cdr.detectChanges();
  }
}