import { Component, computed, signal, inject, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Subscription } from 'rxjs'; 
import { AuthService } from '../../services/auth.service';
import { BillingService } from '../../services/billing-service';
import { NotificationResponseDTO } from '../../../shared/models/notification-model';

interface NavLink {
  label: string;
  icon: string;
  targetTab?: string;
  routePath?: string;
}

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.scss'
})
export class MainLayoutComponent implements OnInit, OnDestroy { 
  sidebarOpen = signal<boolean>(false);
  dropdownOpen = signal<boolean>(false);
  userMenuOpen = signal<boolean>(false); 
  notificationModalActive = signal<boolean>(false);
  
  notifications = signal<NotificationResponseDTO[]>([]);
  unreadCount = signal<number>(0);
  currentUserId: number | null = null;

  private billingService = inject(BillingService);
  private cdr = inject(ChangeDetectorRef); 
  private refreshSub!: Subscription; 

  constructor(public authService: AuthService) {}

  ngOnInit(): void {
    // 🎯 REFACTORED: Relying directly on core auth service context instead of parsing local storage
    const activeUser = this.authService.currentUser();
    
    if (activeUser && activeUser.userId) {
      this.currentUserId = activeUser.userId;
      this.loadNotifications();
    }

    // Listen for broadcasts across runtime streams to update indicators dynamically
    this.refreshSub = this.billingService.notificationRefresh$.subscribe(() => {
      this.loadNotifications();
    });
  }

  ngOnDestroy(): void {
    if (this.refreshSub) {
      this.refreshSub.unsubscribe();
    }
  }

  loadNotifications(): void {
    if (!this.currentUserId) return;
    this.billingService.getRecentNotifications(this.currentUserId, 0, 50).subscribe({
      next: (res: any) => {
        const list = res.content || res || [];
        this.notifications.set(list);
        
        this.unreadCount.set(
          list.filter((notif: NotificationResponseDTO) => 
            (notif.status || '').toUpperCase() === 'ACTIVE'
          ).length
        );

        this.cdr.detectChanges();
      },
      error: (err) => console.error('Notification synchronization failed:', err)
    });
  }

  dismissNotification(notificationId: number, event: Event | null): void {
    if (event) {
      event.stopPropagation();
    }
    if (!this.currentUserId) return;

    this.billingService.markNotificationAsRead(notificationId, this.currentUserId).subscribe({
      next: () => {
        this.notifications.update(list => 
          list.map(n => n.notificationId === notificationId ? { ...n, status: 'READ' } : n)
        );
        this.unreadCount.update(c => Math.max(0, c - 1));
        this.cdr.detectChanges();
      }
    });
  }

  dismissAllNotifications(event: Event): void {
    event.stopPropagation();
    if (!this.currentUserId || this.unreadCount() === 0) return;

    if (confirm('Mark all pending messages as read?')) {
      this.notifications.update(list => list.map(n => ({ ...n, status: 'READ' })));
      this.unreadCount.set(0);
      this.cdr.detectChanges(); 

      this.notifications().forEach(notif => {
        if (notif.status === 'ACTIVE') { 
          this.billingService.markNotificationAsRead(notif.notificationId, this.currentUserId!).subscribe();
        }
      });
    }
  }
  
  toggleDropdown(event: Event): void {
    event.stopPropagation();
    this.userMenuOpen.set(false); 
    this.dropdownOpen.update(v => !v);
  }

  toggleUserMenu(event: Event): void {
    event.stopPropagation();
    this.dropdownOpen.set(false); 
    this.userMenuOpen.update(v => !v);
  }

  handleBackgroundShellClick(): void {
    this.dropdownOpen.set(false);
    this.userMenuOpen.set(false);
  }

  openNotificationManagerModal(event: Event): void {
    event.stopPropagation();
    this.dropdownOpen.set(false);
    this.notificationModalActive.set(true);
  }

  toggleSingleStatus(notificationItem: NotificationResponseDTO): void {
    if (notificationItem.status === 'ACTIVE') { 
      this.dismissNotification(notificationItem.notificationId, null);
    } else {
      this.notifications.update(list =>
        list.map(n => n.notificationId === notificationItem.notificationId ? { ...n, status: 'ACTIVE' } : n)
      );
      this.unreadCount.update(c => c + 1);
      this.cdr.detectChanges(); 
    }
  }

  // Central Router Mapping matrix routing nodes per administrative access scopes
  private navigationMatrix = computed<Record<string, NavLink[]>>(() => ({
    ADMIN: [
      { label: 'Operations Overview', icon: '🎛️', routePath: '/admin-dashboard', targetTab: 'overview' },
      { label: 'Bookings & Requests', icon: '📋', routePath: '/admin-dashboard', targetTab: 'booking-requests' },
      { label: 'Finance & Analytics', icon: '💰', routePath: '/admin-dashboard', targetTab: 'finance' },
      { label: 'Compliance & Audits', icon: '🛡️', routePath: '/admin-dashboard', targetTab: 'compliance' },
      { label: 'User Directory Node', icon: '👥', routePath: '/admin-dashboard', targetTab: 'users' },       
      { label: 'Inventory Provisioning', icon: '📦', routePath: '/admin-dashboard', targetTab: 'inventories' } 
    ],
    CUSTOMER: [
      { label: 'Travel Dashboard', icon: '📊', routePath: '/cust-dashboard' },
      { label: 'Search Marketplace', icon: '🔍', routePath: '/customer/inventories' },
      { label: 'My Bookings History', icon: '📋', routePath: '/customer/bookings' },
      { label: 'Custom Requests', icon: '✉️', routePath: '/customer/custom-requests' },
      { label: 'Invoices', icon: '🧾', routePath: '/customer/invoices' }
    ],
    PARTNER: [
      { label: 'Inventory Dispatch Core', icon: '📦', routePath: '/partner/inventory' },
      { label: 'Financial Settlement Ledger', icon: '💰', routePath: '/partner/ledger' }
    ]
  }));

  sidebarLinks = computed(() => {
    const role = this.authService.currentUserRole();
    return role ? this.navigationMatrix()[role] || [] : [];
  });

  toggleSidebar() {
    this.sidebarOpen.update(v => !v);
  }

  handleSignOut() {
    this.authService.logout();
  }
}