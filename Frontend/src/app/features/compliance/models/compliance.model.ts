export interface AuditLogResponseDTO {
  auditId: number;
  userId: number | null;
  userName: string;
  action: string;
  resourceType: string;
  resourceId: number;
  details: string;
  timestamp: string;
  eventLevel: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface ComplianceReportDTO {
  windowStart: string;
  windowEnd: string;
  totalAuditEvents: number;
  eventCountByAction: { [key: string]: number };
  eventCountByResourceType: { [key: string]: number };
  eventCountByUser: { [key: string]: number };
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
  bookingCountByInventoryType: { [key: string]: number };
}

export interface ComplaintResponseDTO {
  complaintId: number;
  raisedByUserId: number;
  raisedByName: string;
  relatedBookingId: number | null;
  subject: string;
  description: string;
  status: string;
  resolutionNote: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface ComplaintResolveDTO {
  resolutionNote: string;
}