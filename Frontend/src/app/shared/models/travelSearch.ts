export interface TravelSearch {
  itemType:string;
  source?:string;
  destination?:string;
  state?:string;
  district?:string;
  city?:string;
  requiredSeats?:number;
  durationDays?:number;
  maxPrice?:number;
  targetDate?:string;
  checkInDate?: string; 
  checkOutDate?: string;
}
