import { Component, ChangeDetectorRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AddInventory } from '../add-inventory/add-inventory';
import { InventoriesAudit } from '../inventories-audit/inventories-audit';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-inventory-dashboard',
  imports: [CommonModule, AddInventory, InventoriesAudit],
  templateUrl: './inventory-dashboard.html',
  styleUrl: './inventory-dashboard.scss',
})
export class InventoryDashboard {
  activeSubTab: 'view' | 'add' = 'view';

  @Output() alertTrigger = new EventEmitter<any>();

  constructor(private cdr: ChangeDetectorRef, private route: ActivatedRoute) {}

  switchSubTab(tab: 'view' | 'add'): void {
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

  forwardAlert(event: any): void {
    this.alertTrigger.emit(event);
  }
}
