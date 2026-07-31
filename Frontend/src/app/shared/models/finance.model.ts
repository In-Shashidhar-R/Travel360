// src/app/shared/models/finance.model.ts

export interface InvoiceResponseDTO {
  invoiceId: number;
  bookingId: number;
  customerName: string;
  amount: number;
  generatedDate: string;
  status: 'PAID' | 'UNPAID' | 'REFUNDED' | string;
}

export interface PaymentResponseDTO {
  paymentId: number;
  invoiceId: number;
  amount: number;
  paymentDate: string;
  paymentType: 'CREDIT' | 'REFUND' | string;
  method: string;
  status: string;
}

export interface AnalyticsDashboardDTO {
  totalUsers: number;
  totalCustomers: number;
  totalTravelAgents: number;
  totalPartners: number;
  totalInventoryItems: number;
  totalBookings: number;
  confirmedBookings: number;
  cancelledBookings: number;
  pendingBookings: number;
  totalInvoices: number;
  paidInvoices: number;
  unpaidInvoices: number;
  refundedInvoices: number;
  totalRevenueCollected: number;
  totalRefundsIssued: number;
  bookingCountByInventoryType: Record<string, number>;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}