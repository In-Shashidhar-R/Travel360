import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PartnerService } from '../partner.service';
import { AuthService } from '../../../core/services/auth.service';
import { PartnerOverview } from '../partner-overview/partner-overview';
import { ManageServices } from '../manage-services/manage-services';

@Component({
  selector: 'app-partner-dashboard',
  standalone: true,
  imports: [CommonModule, PartnerOverview, ManageServices],
  templateUrl: './partner-dashboard.html',
  styleUrls: ['./partner-dashboard.scss']
})
export class PartnerDashboard implements OnInit {
  activeSection = 'overview';
  loading = true;
  inventories: any[] = [];
  partnerName = '';
  partnerId = 0;
  totalServices = 0;
  activeServices = 0;
  inactiveServices = 0;
  displayUserName = 'Partner Workspace Account';

  constructor(
    private partnerService: PartnerService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUser();
    if (user && user.name) {
      this.displayUserName = user.name;
    }

    this.loadData();
    this.route.queryParams.subscribe(params => {
      this.activeSection = params['section'] || 'overview';
      this.cdr.markForCheck();
    });
  }

  setSection(section: string): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { section: section },
      queryParamsHandling: 'merge'
    });
  }

  loadData(): void {
    this.loading = true;
    this.cdr.markForCheck();

    this.partnerService.getMyInventories().subscribe({
      next: (response: any) => {
        this.inventories = response.content || [];
        if (this.inventories.length > 0) {
          this.partnerName = this.inventories[0].partnerName;
          this.partnerId = this.inventories[0].partnerId;
        }
        this.totalServices = this.inventories.length;
        this.activeServices = this.inventories.filter(x => x.status === 'ACTIVE').length;
        this.inactiveServices = this.inventories.filter(x => x.status === 'INACTIVE').length;
        this.loading = false;
        this.cdr.detectChanges(); 
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  activate(id: number): void {
    this.partnerService.activateInventory(id).subscribe({
      next: () => this.loadData(),
      error: (err) => console.error(err)
    });
  }

  deactivate(id: number): void {
    this.partnerService.deactivateInventory(id).subscribe({
      next: () => this.loadData(),
      error: (err) => console.error(err)
    });
  }

  onSignOut(): void {
    this.authService.logout();
  }
}