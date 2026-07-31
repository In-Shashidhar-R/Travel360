import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AdminService } from '../services/admin.service'; 
import { AuthService } from '../../../core/services/auth.service'; // 👈 Added AuthService import
import { forkJoin, of } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './overview.html',
  styleUrl: './overview.scss',
})
export class Overview implements OnInit {
  loading = true;
  systemStatus = 'Operational';
  userName = 'Team Member'; 

  grossRevenue = 0;
  totalUsersCount = 0;
  pendingRequestsCount = 0;
  totalBookingsCount = 0;

  constructor(
    private adminService: AdminService,
    private authService: AuthService, 
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.fetchDashboardData();
    
    const user = this.authService.currentUser();
    this.userName = user?.name || 'Admin';
  }

  fetchDashboardData(): void {
    this.loading = true;
    const MAX_TIMEOUT = 4000;

    forkJoin({
      analytics: this.adminService.getAnalyticsDashboard().pipe(
        timeout(MAX_TIMEOUT),
        catchError((err) => {
          console.warn('Analytics data timed out or failed to load', err);
          return of({});
        })
      ),
      users: this.adminService.getAllUsers().pipe(
        timeout(MAX_TIMEOUT),
        catchError((err) => {
          console.warn('Users directory timed out or failed to load', err);
          return of([]);
        })
      ),
      requests: this.adminService.getAllBookingRequests().pipe(
        timeout(MAX_TIMEOUT),
        catchError((err) => {
          console.warn('Booking requests timed out or failed to load', err);
          return of([]);
        })
      )
    }).subscribe({
      next: (res) => {
        const analyticsData = res.analytics as any;
        
        this.grossRevenue = analyticsData?.totalRevenueCollected ?? 0;
        this.totalBookingsCount = analyticsData?.totalBookings ?? 0;
        this.totalUsersCount = analyticsData?.totalUsers ?? 0;
        this.pendingRequestsCount = analyticsData?.pendingBookings ?? res.requests?.length ?? 0;
        
        this.systemStatus = 'Operational';
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Critical dashboard data fetch breakdown:', err);
        this.systemStatus = 'Degraded Performance';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }
}