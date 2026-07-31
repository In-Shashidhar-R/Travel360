import { Component, OnInit, ChangeDetectorRef, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { InventoryDataset } from '../../../../shared/models/inventory.model';
import { InventoryEditModal } from '../inventory-edit-modal/inventory-edit-modal';

export type AuditInventory = InventoryDataset & {
  uiExpanded: boolean;
  isActive?: boolean;
  status?: string;  
  active?: string;  
};

@Component({
  selector: 'app-inventories-audit',
  standalone: true,
  imports: [CommonModule, FormsModule, InventoryEditModal], 
  templateUrl: './inventories-audit.html',
  styleUrls: ['./inventories-audit.scss'],
  encapsulation: ViewEncapsulation.None
})
export class InventoriesAudit implements OnInit {
  backendFilteredInventories: AuditInventory[] = []; 

  filterItemType: string = ''; 
  filterState: string = ''; 
  filterDistrict: string = ''; 
  filterCity: string = '';
  filterSource: string = ''; 
  filterDestination: string = ''; 
  filterMaxPrice: number | null = null;

  currentPage: number = 1;
  pageSize: number = 6; 

  isEditing: boolean = false;
  editingItem: any = null;

  constructor(private adminService: AdminService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.executeInventoryQuery();
  }

  executeInventoryQuery(): void {
    const filterCriteria = {
      itemType: this.filterItemType, state: this.filterState, district: this.filterDistrict,
      city: this.filterCity, source: this.filterSource, destination: this.filterDestination, maxPrice: this.filterMaxPrice
    };
    
    this.adminService.getFilteredInventories(filterCriteria).subscribe({
      next: (data: any[]) => {
        this.backendFilteredInventories = data.map(inv => ({
          ...inv,
          uiExpanded: false,
          isActive: inv.status === 'ACTIVE' || inv.active === 'ACTIVE'
        })) as AuditInventory[];
        this.resetPagination();
      },
      error: (err) => console.error('💥 REST Framework Filter Exception:', err)
    });
  }

  onFilterSearchTrigger(): void {
    if (this.filterItemType === 'FLIGHT' || this.filterItemType === 'BUS') {
      this.filterState = ''; this.filterDistrict = ''; this.filterCity = '';
    } else if (this.filterItemType === 'CAB') {
      this.filterCity = ''; this.filterSource = ''; this.filterDestination = '';
    } else if (this.filterItemType === 'HOTEL') {
      this.filterSource = ''; this.filterDestination = '';
    } else if (this.filterItemType === 'TOUR_PACKAGE') {
      this.filterState = ''; this.filterDistrict = ''; this.filterCity = '';
      this.filterSource = ''; this.filterDestination = '';
    }
    this.executeInventoryQuery();
  }

  resetPagination(): void {
    this.currentPage = 1;
    this.cdr.detectChanges();
  }

  getBasePrice(inv: any): number {
  if (!inv) return 0;
  return inv.basePricePerSeat || inv.basePricePerRoom || inv.basePricePerPersonForPackage || 0;
  }

  get paginatedInventories(): AuditInventory[] {
    if (!Array.isArray(this.backendFilteredInventories)) return [];
    const startIndex = (this.currentPage - 1) * this.pageSize;
    return this.backendFilteredInventories.slice(startIndex, startIndex + this.pageSize);
  }

  getStartIndex(): number {
    if (this.backendFilteredInventories.length === 0) return 0;
    return (this.currentPage - 1) * this.pageSize + 1;
  }

  getEndIndex(totalLength: number): number {
    const end = this.currentPage * this.pageSize;
    return end > totalLength ? totalLength : end;
  }

  getTotalPages(totalLength: number): number {
    return Math.ceil(totalLength / this.pageSize) || 1;
  }

  getPagesArray(totalLength: number): number[] {
    const pages = this.getTotalPages(totalLength);
    return Array(pages).fill(0);
  }

  goToPage(page: number): void {
    const maxPage = this.getTotalPages(this.backendFilteredInventories.length);
    if (page >= 1 && page <= maxPage) {
      this.currentPage = page;
      this.cdr.detectChanges();
    }
  }

  toggleCard(item: AuditInventory): void {
    item.uiExpanded = !item.uiExpanded;
    this.cdr.detectChanges(); 
  }

  onToggleActivation(inv: AuditInventory, event: Event): void {
    event.stopPropagation(); 
    const targetObservable = inv.isActive 
      ? this.adminService.deactivateInventory(inv.inventoryId)
      : this.adminService.activateInventory(inv.inventoryId);

    targetObservable.subscribe({
      next: () => {
        const idx = this.backendFilteredInventories.findIndex(item => item.inventoryId === inv.inventoryId);
        if (idx !== -1) {
          const nextState = !this.backendFilteredInventories[idx].isActive;
          this.backendFilteredInventories[idx] = {
            ...this.backendFilteredInventories[idx],
            isActive: nextState,
            status: nextState ? 'ACTIVE' : 'INACTIVE',
            active: nextState ? 'ACTIVE' : 'INACTIVE'
          };
          this.backendFilteredInventories = [...this.backendFilteredInventories];
          this.cdr.detectChanges(); 
        }
      },
      error: (err) => console.error('💥 Runtime Operational Toggle Error:', err)
    });
  }

  onDeleteInventory(inv: AuditInventory, event: Event): void {
    event.stopPropagation();
    if (confirm(`Are you sure you want to permanently delete inventory element REF #${inv.inventoryId}?`)) {
      const segmentMap: Record<string, 'flight' | 'hotel' | 'bus' | 'cab' | 'tour-package'> = {
        'FLIGHT': 'flight', 'HOTEL': 'hotel', 'BUS': 'bus', 'CAB': 'cab', 'TOUR_PACKAGE': 'tour-package'
      };
      const type = segmentMap[inv.itemType];
      if (!type) return;

      this.adminService.deleteInventory(inv.inventoryId, type).subscribe({
        next: (msg) => {
          alert(msg || 'Resource purged successfully.');
          this.executeInventoryQuery(); 
        },
        error: (err) => alert('Cannot delete this resource. Verify that no active customer bookings reference it.')
      });
    }
  }

  onEditInventory(inv: AuditInventory, event: Event): void {
    event.stopPropagation();
    this.editingItem = { ...inv };
    this.isEditing = true;
    this.cdr.detectChanges(); 
  }

  onModalDismiss(): void {
    this.isEditing = false;
    this.editingItem = null;
    this.cdr.detectChanges(); 
  }

  onModalSaveSuccess(): void {
    this.isEditing = false;
    this.editingItem = null;
    this.executeInventoryQuery(); 
  }

  asFlight(item: any) { return item; }
  asBus(item: any) { return item; }
  asHotel(item: any) { return item; }
  asCab(item: any) { return item; }
  asTour(item: any) { return item; }
}