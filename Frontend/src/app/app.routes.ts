import { Routes } from '@angular/router';
import { Index } from './components/index/index';
import { Login } from './components/login/login';
import { SignUp } from './components/sign-up/sign-up';
import { ForgotPassword } from './components/forgot-password/forgot-password';
import { Dashboard } from './features/admin/dashboard/dashboard';
import { authGuard } from './core/guards/auth.guard';
import { TravelAgentDashboardComponent } from './features/travel-agent/travel-agent-dashboard/travel-agent-dashboard.component';
import { AuditLogsComponent } from './features/compliance/audit-logs/audit-logs';
import { AnalyticsDashboardComponent } from './features/compliance/analytics-dashboard/analytics-dashboard';
import { Complaints } from './features/compliance/complaints/complaints';
import { DashboardC } from './features/compliance/dashboard/dashboard';
import { FinanceDashboard } from './features/finance/finance-dashboard/finance-dashboard';
import { PartnerDashboard } from './features/partner/partner-dashboard/partner-dashboard';
import { MainLayoutComponent } from './core/layout/main-layout/main-layout';
import { Home } from './features/Customer/home/home';
import { InventoryBrowser } from './features/Customer/inventory-browser/inventory-browser';
import { Profile } from './features/Customer/profile/profile';
import { ItemDetailsComponent } from './features/Customer/item-details/item-details';
import { PaymentScreen } from './features/Customer/payment-screen/payment-screen';
import { CustomerBookings } from './features/Customer/cust-bookings/cust-bookings';
import { CustRequests } from './features/Customer/cust-requests/cust-requests';
import { CustInvoices } from './features/Customer/cust-invoices/cust-invoices';
import { CustCompliants } from './features/Customer/cust-compliants/cust-compliants';


export const routes: Routes = [
  { path: '', component: Index },
  { path: 'login', component: Login },
  { path: 'signup', component: SignUp },
  { path: 'forgot-password', component: ForgotPassword },

  //Travel Agent
  {
    path: 'agent-dashboard',
    component: TravelAgentDashboardComponent,
    canActivate: [authGuard],
    data: { roles: ['TRAVEL_AGENT'] }
  },

  {
    path: 'admin-dashboard',
    component: Dashboard,
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
    children: [
      { path: '', redirectTo: 'overview', pathMatch: 'full' },
      {
        path: 'myprofile',
        loadComponent: () => import('./features/admin/myprofile/myprofile').then(m => m.MyProfile)
      },
      {
        path: 'overview',
        loadComponent: () => import('./features/admin/overview/overview').then(m => m.Overview)
      },
      {
        path: 'booking-requests',
        loadComponent: () => import('./features/admin/booking/booking-dashboard/booking-dashboard').then(m => m.BookingDashboard)
      },
      {
        path: 'finance',
        loadComponent: () => import('./features/admin/finance/finance-dashboard/finance-dashboard').then(m => m.FinanceDashboard)
      },
      {
        path: 'compliance',
        loadComponent: () => import('./features/admin/compliance/compliance-dashboard/compliance-dashboard').then(m => m.ComplianceDashboard)
      },
      {
        path: 'users',
        loadComponent: () => import('./features/admin/users/user-dashboard/user-dashboard').then(m => m.UserDashboard)
      },
      {
        path: 'inventories',
        loadComponent: () => import('./features/admin/inventory/inventory-dashboard/inventory-dashboard').then(m => m.InventoryDashboard)
      },
    ]
  },

  //compliance officer

  {
    path: 'compliance',
    component: DashboardC,
    canActivate: [authGuard],
    data: { roles: ['COMPLIANCE_OFFICER'] },
    children: [
      {
        path: 'audit-logs',
        component: AuditLogsComponent
      },
      {
        path: 'analytics',
        component: AnalyticsDashboardComponent
      },
      {
        path: 'complaints',
        component: Complaints
      },
      {
        path: '',
        redirectTo: 'audit-logs',
        pathMatch: 'full'
      }
    ]
  },

  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: 'cust-dashboard',
        component: Home,
        canActivate: [authGuard],
        data: { roles: ['CUSTOMER'] }
      },
      {
        path: 'customer/inventories',
        component: InventoryBrowser,
        canActivate: [authGuard],
        data: { roles: ['CUSTOMER'] }
      },
      {
        path: 'customer/profile',
        component: Profile,
        canActivate: [authGuard],
        data: { roles: ['CUSTOMER'] }
      },
      {
        path: 'customer/complaints',
        component: CustCompliants,
        canActivate: [authGuard],
        data: { roles: ['CUSTOMER'] }
      },
      {
        path: 'customer/details/:id',
        component: ItemDetailsComponent,
        canActivate: [authGuard],
        data: { roles: ['CUSTOMER'] }
      },
      {
        path: 'customer/payment/:bookingId',
        component: PaymentScreen,
        canActivate: [authGuard],
        data: { roles: ['CUSTOMER'] }
      },
      {
        path: 'customer/bookings',
        component: CustomerBookings,
        canActivate: [authGuard],
        data: { roles: ['CUSTOMER'] }
      },
      {
        path: 'customer/custom-requests',
        component: CustRequests,
        canActivate: [authGuard],
        data: { roles: ['CUSTOMER'] }
      },
      {
        path: 'customer/invoices',
        component: CustInvoices,
        canActivate: [authGuard],
        data: { roles: ['CUSTOMER'] }
      }
    ]
  },


  {
    path: 'partner-dashboard',
    component: PartnerDashboard,
    canActivate: [authGuard],
    data: { roles: ['PARTNER'] },
  },

  {
    path: 'finance-dashboard',
    component: FinanceDashboard,
    canActivate: [authGuard],
    data: { roles: ['FINANCE_OFFICER'] },
  },

  { path: '**', redirectTo: 'login' }
];
