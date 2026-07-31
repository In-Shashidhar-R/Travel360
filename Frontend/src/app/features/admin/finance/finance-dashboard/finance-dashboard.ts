import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FinanceAnalytics } from '../../../finance/finance-analytics/finance-analytics';
import { FinanceInvoices } from '../finance-invoices/finance-invoices';
import { FinancePayments } from '../finance-payments/finance-payments';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-finance-dashboard',
  imports: [CommonModule, FinanceAnalytics, FinanceInvoices, FinancePayments],
  templateUrl: './finance-dashboard.html',
  styleUrl: './finance-dashboard.scss',
})
export class FinanceDashboard {
  activeSubTab: 'analytics' | 'invoices' | 'payments' = 'analytics';

  constructor(private cdr: ChangeDetectorRef,  private route: ActivatedRoute) {}

  switchSubTab(tab: 'analytics' | 'invoices' | 'payments'): void {
    this.activeSubTab = tab;
    this.cdr.detectChanges(); 
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['tab']) {
        this.activeSubTab = params['tab']; 
      }
    });
  }
}
