import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsDashboardDTO } from '../models/compliance.model';
import { ComplianceService } from '../../../core/services/compliance';

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './analytics-dashboard.html'
})
export class AnalyticsDashboardComponent implements OnInit {
  dashboard: AnalyticsDashboardDTO | null = null;

  constructor(
    private complianceService: ComplianceService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.complianceService.getAnalyticsDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;

        // Force UI refresh
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Failed to load analytics dashboard', error);
      }
    });
  }
}