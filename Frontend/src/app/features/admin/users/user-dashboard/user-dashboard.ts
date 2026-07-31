import { Component, ChangeDetectorRef, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PartnerRegister } from '../partner-register/partner-register';
import { AgentRegister } from '../agent-register/agent-register';
import { UserAudit } from '../user-audit/user-audit';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-user-dashboard',
  imports: [CommonModule, PartnerRegister, AgentRegister, UserAudit],
  templateUrl: './user-dashboard.html',
  styleUrl: './user-dashboard.scss',
})
export class UserDashboard {
  activeSubTab: 'view' | 'add-partner' | 'add-agent' = 'view';
  
  @Output() alertTrigger = new EventEmitter<any>();

  constructor(private cdr: ChangeDetectorRef, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['tab']) {
        this.activeSubTab = params['tab']; 
      }
    });
  }

  switchSubTab(tab: 'view' | 'add-partner' | 'add-agent'): void {
    this.activeSubTab = tab;
    this.cdr.detectChanges();
  }

  forwardAlert(event: any): void {
    this.alertTrigger.emit(event);
  }

  dashboardMessage = signal<string>('');

  handleAlert(message: string): void {
    this.dashboardMessage.set(message);
    
    setTimeout(() => {
      this.dashboardMessage.set('');
    }, 4000);
  }
}
