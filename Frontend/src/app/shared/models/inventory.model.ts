export interface SeatTierDTO {
  seatType: string;
  totalSeatsAllocated: number;
  priceMultiplier: number;
}

export interface RouteStopDTO {
  stopName: string;
  stopType: string;
  scheduledTime: string;
}

// ==========================================
// INVENTORY PROVISIONING REQUEST DTOs
// ==========================================

export interface FlightInventoryRequestDTO {
  partnerId: number;
  basePricePerSeat: number;
  flightNumber: string;
  airlineName: string;
  departureAirport: string;
  arrivalAirport: string;
  layoverDetails?: string;
  startTime: string;
  endTime: string;
  seatTiers: SeatTierDTO[];
  connecting: boolean;
}

export interface HotelInventoryRequestDTO {
  partnerId: number;
  totalRooms: number;
  basePricePerRoom: number;
  hotelName: string;
  roomType: string;
  hotelRating: number;
  addressLocation: string;
  district: string;
  state: string;
  country: string;
}

export interface BusInventoryRequestDTO {
  partnerId: number;
  basePricePerSeat: number;
  busNumberPlate: string;
  operatorName: string;
  routeFrom: string;
  routeTo: string;
  startTime: string;
  endTime: string;
  routeStops: RouteStopDTO[];
  seatTiers: SeatTierDTO[];
}

export interface CabInventoryRequestDTO {
  partnerId: number;
  basePricePerSeat: number;
  vehicleRegistrationNumber: string;
  carModel: string;
  fuelType: string;
  seaterCount: number;
  district: string;
  state: string;
}

export interface TourInventoryRequestDTO {
  partnerId: number;
  basePricePerPersonForPackage: number;
  packageName: string;
  fullItineraryDetails: string;
  durationDays: number;
  travelAgentId: number;
}

// ==========================================
// INVENTORY RESPONSE DTOs (STRONGLY TYPED)
// ==========================================

export interface BaseInventoryResponseDTO {
  inventoryId: number;
  partnerId: number;
  partnerName: string;
  itemType: 'FLIGHT' | 'BUS' | 'CAB' | 'HOTEL' | 'TOUR_PACKAGE';
  basePricePerSeat: number;
  status: string;
  uiExpanded?: boolean; // UI tracking state indicator
}

export interface BusInventoryResponseDTO extends BaseInventoryResponseDTO {
  totalSeats: number;
  busNumberPlate: string;
  operatorName: string;
  routeFrom: string;
  routeTo: string;
  startTime: string;
  endTime: string;
  numberOfHours: number;
  routeStops: RouteStopDTO[]; // Upgraded from any[] to strict DTO matching
  seatTiers: SeatTierDTO[];   // Upgraded from any[] to strict DTO matching
}

export interface CabInventoryResponseDTO extends BaseInventoryResponseDTO {
  vehicleRegistrationNumber: string;
  carModel: string;
  fuelType: string;
  seaterCount: number;
  district: string;
  state: string;
}

export interface FlightInventoryResponseDTO extends BaseInventoryResponseDTO {
  totalSeats: number;
  flightNumber: string;
  airlineName: string;
  departureAirport: string;
  arrivalAirport: string;
  isConnecting: boolean;
  layoverDetails: string;
  startTime: string;
  endTime: string;
  numberOfHours: number;
  seatTiers: SeatTierDTO[];   // Upgraded from any[] to strict DTO matching
}

export interface HotelInventoryResponseDTO {
  inventoryId: number;
  partnerId: number;
  partnerName: string;
  itemType: 'HOTEL';
  totalRooms: number;
  basePricePerSeat: number;
  status: string;
  hotelName: string;
  roomType: string;
  hotelRating: number;
  addressLocation: string;
  district: string;
  state: string;
  country: string;
  uiExpanded?: boolean; // Maintained inline tracking property for view synchronization
}

export interface TourInventoryResponseDTO extends BaseInventoryResponseDTO {
  packageName: string;
  fullItineraryDetails: string;
  durationDays: number;
  travelAgentId: number;
  travelAgentName: string;
  travelAgentEmail: string;
}

// Polymorphic Union Datatype Context
export type InventoryDataset = 
  | FlightInventoryResponseDTO 
  | BusInventoryResponseDTO 
  | CabInventoryResponseDTO 
  | HotelInventoryResponseDTO 
  | TourInventoryResponseDTO;