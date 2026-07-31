/**
 * Exact frontend match for com.cts.enumeration.BookingRequestStatus
 */
export type BookingRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'COMPLETED';

/**
 * Exact frontend match for com.cts.dto.BookingRequestResponseDTO
 * This handles the structural responses returned by the backend for list, 
 * details, accept, reject, and booking actions.
 */
export interface BookingRequestResponseDTO {
  requestId: number;
  status: BookingRequestStatus;

  customerId: number;
  customerName: string;

  assignedAgentId: number;
  assignedAgentName: string;
  assignedAgentEmail: string;

  inventoryId: number;
  inventoryItemType: string; 
  packageName: string;

  customerRequirements: string;
  agentNotes: string | null; 

  requestedDate: string; 
  updatedDate: string;  

  
  resultingBookingId: number | null; 
}


export interface BookingRequestDecisionDTO {
  agentNotes: string;
}


export interface TourBookingRequestDTO {
  customerId: number;
  inventoryId: number;
  targetTravelDate: string; 
  numberOfPersons: number;
  passengerProfileIds: number[]; 
}


export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}


export interface LoginModel {
  emailId: string;
  password: string;
}

export interface AuthResponseDTO {
  token: string;
  name: string;
  email: string;
  role: string;
}