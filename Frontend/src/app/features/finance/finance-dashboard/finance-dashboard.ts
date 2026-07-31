import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { FinanceAnalytics } from '../finance-analytics/finance-analytics';
import { FinanceInvoices } from '../finance-invoices/finance-invoices';
import { FinancePayments } from '../finance-payments/finance-payments';

@Component({
  selector: 'app-finance-dashboard',
  standalone: true,
  imports: [
    CommonModule, 
    FinanceAnalytics, 
    FinanceInvoices, 
    FinancePayments
  ],
  templateUrl: './finance-dashboard.html',
  styleUrl: './finance-dashboard.scss'

})

export class FinanceDashboard implements OnInit {

  activeView: 'analytics' | 'invoices' | 'payments' = 'analytics';
  currentUserRole: string = 'FINANCE'; 
  currentUserName: string = 'Finance Officer'; 
  currentUserId: number | null = null;
  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {

    this.currentUserId = this.authService.getCurrentUserId();
    const user = this.authService.currentUser();

    if (user && user.name) {
      this.currentUserName = user.name;
    } else if (this.currentUserId) {
      this.currentUserName = `Officer ID: #${this.currentUserId}`;

    }

  }
  setView(view: 'analytics' | 'invoices' | 'payments'): void {
    this.activeView = view;
  }

  onSignOut(): void {
    this.authService.logout();
  }

}

