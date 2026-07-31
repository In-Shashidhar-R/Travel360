import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuditLog } from '../audit-log/audit-log';
import { Complaints } from '../complaints/complaints';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-compliance-dashboard',
  imports: [CommonModule, AuditLog, Complaints],
  templateUrl: './compliance-dashboard.html',
  styleUrl: './compliance-dashboard.scss',
})
export class ComplianceDashboard {
  currentSubTab: 'audit-log' | 'complaints' = 'audit-log';

  constructor(private cdr: ChangeDetectorRef,  private route: ActivatedRoute) {}

  switchSubTab(tab: 'audit-log' | 'complaints'): void {
    this.currentSubTab = tab;
    this.cdr.detectChanges();
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['tab']) {
        this.currentSubTab = params['tab']; 
      }
    });
  }
}
