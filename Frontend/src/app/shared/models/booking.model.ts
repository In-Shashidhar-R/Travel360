// src/app/shared/models/booking.model.ts

export interface PassengerSnapshotDTO {
  name: string;
  age: number;
  gender: string;
  idProofType: 'PAN' | 'AADHAAR' | 'DRIVING_LICENSE' | 'PASSPORT' | string;
  idProofNumber: string;
}

export interface BaseBookingResponseDTO {
  bookingId: number;
  customerId: number;
  customerName: string;
  partnerId: number;
  partnerName: string;
  inventoryId: number;
  itemType: 'FLIGHT' | 'HOTEL' | 'BUS' | 'CAB' | 'TOUR_PACKAGE' | string;
  bookingDate: string;
  status: string;
  totalAmount: number;
  passengers: PassengerSnapshotDTO[];
  
  // Polymorphic flat properties
  requestedSeats?: number;
  pickupLocation?: string;
  dropoffLocation?: string;
  numberOfPersons?: number;
  checkInDate?: string;
  checkOutDate?: string;
  chosenSeatType?: string;
  targetTravelDate?: string;
  requestedRooms?: number;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}