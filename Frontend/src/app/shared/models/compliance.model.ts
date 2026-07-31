// src/app/shared/models/compliance.model.ts

export interface AuditLogResponseDTO {
  auditId: number;
  userId: number;
  userName: string;
  action: string;
  resourceType: string;
  resourceId: number;
  details: string;
  timestamp: string;
  eventLevel: 'INFO' | 'WARN' | 'ERROR' | 'CRITICAL' | string;
}

export interface ComplaintResponseDTO {
  complaintId: number;
  raisedByUserId: number;
  raisedByName: string;
  relatedBookingId: number | null;
  subject: string;
  description: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'RESOLVED' | string;
  resolutionNote: string | null;
  createdAt: string;
  updatedAt: string;
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