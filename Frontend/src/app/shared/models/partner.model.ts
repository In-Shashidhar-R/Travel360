export interface PartnerRequestDTO {
  name: string;
  type: 'FLIGHT' | 'HOTEL' | 'BUS' | 'CAB' | 'TOUR_PACKAGE';
  email: string;
  contactNumber: string;
  password?: string;
  address?: string;
  country: string;
  state: string;
  city: string;
  gender: string;
  gstNumber?: string;
  commissionRate: number;
}

export interface PartnerResponseDTO {
  partnerId: number;
  name: string;
  email: string;
  contactNumber: string;
  type: string;
  gstNumber: string;
  commissionRate: number;
  address: string;
  city: string;
  gender: string;
  state: string;
  country: string;
  dateOfBirth?: string; // FIXED: Added missing property to prevent data truncation
}