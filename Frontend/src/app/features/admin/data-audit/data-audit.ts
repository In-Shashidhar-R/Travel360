import { Component, OnInit, Input, OnChanges, SimpleChanges, ChangeDetectorRef, ViewEncapsulation } from '@angular/core'; 
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../services/admin.service';
import { forkJoin } from 'rxjs';
import { UserDirectoryDTO } from '../../../shared/models/user.model';
import { InventoryDataset } from '../../../shared/models/inventory.model';
import { InventoryEditModal } from '../inventory/inventory-edit-modal/inventory-edit-modal';

export type AuditInventory = InventoryDataset & {
  uiExpanded: boolean;
  isActive?: boolean;
  status?: string;  
  active?: string;  
};

@Component({
  selector: 'app-data-audit',
  standalone: true,
  imports: [CommonModule, FormsModule, InventoryEditModal], 
  templateUrl: './data-audit.html',
  styleUrl: './data-audit.scss',
  encapsulation: ViewEncapsulation.None
})
export class DataAudit implements OnInit, OnChanges {
  @Input() layoutSection: string = '';

  mockUsers: UserDirectoryDTO[] = [];
  backendFilteredInventories: AuditInventory[] = []; 
  subViewTab: 'users' | 'inventories' = 'users';

  // Filter Engine Bound States
  filterItemType: string = ''; 
  filterState: string = ''; 
  filterDistrict: string = ''; 
  filterCity: string = '';
  filterSource: string = ''; 
  filterDestination: string = ''; 
  filterMaxPrice: number | null = null;
  
  userSearchId: string = ''; 
  userSearchRole: string = '';

  // Operational State Management
  isEditing: boolean = false;
  editingItem: any = null;

  constructor(private adminService: AdminService, public cdr: ChangeDetectorRef) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['layoutSection']) {
      const currentVal = changes['layoutSection'].currentValue;
      this.subViewTab = currentVal === 'view-inventories' ? 'inventories' : 'users';
      this.cdr.detectChanges(); 
    }
  }

  ngOnInit(): void {
    this.loadSystemUsers();
    this.executeInventoryQuery();
  }

  private extractContentArray(response: any): any[] {
    if (!response) return [];
    if (Array.isArray(response)) return response;
    if (response.content && Array.isArray(response.content)) return response.content;
    if (response.data) {
      if (Array.isArray(response.data)) return response.data;
      if (response.data.content && Array.isArray(response.data.content)) return response.data.content;
    }
    return [];
  }

  loadSystemUsers(): void {
    forkJoin({
      users: this.adminService.getAllUsers(),
      partners: this.adminService.getAllPartners()
    }).subscribe({
      next: ({ users, partners }) => {
        const userArray = this.extractContentArray(users);
        const partnerArray = this.extractContentArray(partners);

        const userData = userArray
          .filter((u: any) => u.role !== 'PARTNER') 
          .map((u: any) => ({
            userId: u.userId, name: u.name, email: u.email, phone: u.phone, role: u.role,
            gender: u.gender, address: u.address, city: u.city, state: u.state, country: u.country,
            agentBio: u.agentBio, agentExperienceYears: u.agentExperienceYears
          }));

        const partnerData = partnerArray.map((p: any) => ({
          userId: p.partnerId, name: p.name, email: p.email, phone: p.contactNumber, role: 'PARTNER',
          gender: p.gender, 
          address: p.address, city: p.city, state: p.state, country: p.country,
          partnerType: p.type, gstNumber: p.gstNumber, commissionRate: p.commissionRate
        }));

        this.mockUsers = [...userData, ...partnerData];
        this.cdr.detectChanges(); 
      },
      error: (err) => console.error('💥 User Directory ForkJoin Exception:', err)
    });
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
        this.cdr.detectChanges(); 
      },
      error: (err) => console.error('💥 REST Framework Filter Exception:', err)
    });
  }

  get filteredUsers(): UserDirectoryDTO[] {
    if (!Array.isArray(this.mockUsers)) return [];
    return this.mockUsers.filter(u => {
      const matchId = this.userSearchId ? u.userId.toString() === this.userSearchId : true;
      const matchRole = this.userSearchRole ? u.role === this.userSearchRole : true;
      return matchId && matchRole;
    });
  }

  // FIXED: Clears irrelevant filter inputs when switching target item types 
  // to avoid sending conflicting parameters to the backend REST query endpoint.
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
        const targetIndex = this.backendFilteredInventories.findIndex(item => item.inventoryId === inv.inventoryId);
        if (targetIndex !== -1) {
          const nextState = !this.backendFilteredInventories[targetIndex].isActive;
          this.backendFilteredInventories[targetIndex] = {
            ...this.backendFilteredInventories[targetIndex],
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
      const targetEndpointType = segmentMap[inv.itemType];
      if (!targetEndpointType) return;

      this.adminService.deleteInventory(inv.inventoryId, targetEndpointType).subscribe({
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