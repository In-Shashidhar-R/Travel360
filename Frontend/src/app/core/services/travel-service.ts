import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { TravelSearch } from '../../shared/models/travelSearch';
import { PassengerProfile } from '../../shared/models/passenger-profile';
import { InvoiceResponseDTO, PageResponse } from '../../shared/models/finance.model';
import { ComplaintResponseDTO } from '../../shared/models/compliance.model';
import { UserResponseDTO } from '../../shared/models/user.model';

@Injectable({
  providedIn: 'root'
})
export class TravelService {
  private http = inject(HttpClient);
  private readonly API_BASE = 'http://localhost:9095/api/v1'; 

  // ==========================================
  // 🎯 REAL-TIME SYSTEM NOTIFICATION SYNC BUS
  // ==========================================
  private refreshNotificationsSubject = new Subject<void>();
  refreshNotifications$ = this.refreshNotificationsSubject.asObservable();

  /** Broadcasts a signal to sync counts across the app layer */
  triggerNotificationRefresh(): void {
    this.refreshNotificationsSubject.next();
  }

  // ==========================================
  // 1. INVENTORY ENDPOINTS
  // ==========================================
  getAllInventories(targetDate?: string): Observable<any[]> {
      let params = new HttpParams();
      if (targetDate) {
        params = params.set('targetDate', targetDate);
      }
      return this.http.get<any[]>(`${this.API_BASE}/inventories`,{params});
    }

  filterInventories(criteria: TravelSearch, page: number = 0, size: number = 5): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    Object.keys(criteria).forEach(key => {
      const value = (criteria as any)[key];
      if (value !== null && value !== undefined && value !== '') {
        params = params.append(key, value);
      }
    });
    
    return this.http.get<any>(`${this.API_BASE}/inventories/filter`, { params });
  }
  
  getInventoryById(id: number, targetDate?: Date): Observable<any> {
  let params = new HttpParams();
  
  if (targetDate) {
    // Converts the Date object to YYYY-MM-DD format strings (e.g., "2026-07-13")
    // which matches Spring's expected ISO format.
    const formattedDate = targetDate.toISOString().split('T')[0];
    params = params.set('targetDate', formattedDate);
  }

  return this.http.get<any>(`${this.API_BASE}/inventories/${id}`, { params });
}

  // ==========================================
  // 2. BOOKING ENDPOINTS
  // ==========================================
  bookFlight(payload: any): Observable<any> {
    return this.http.post(`${this.API_BASE}/bookings/flight`, payload);
  }

  bookHotel(payload: any): Observable<any> {
    return this.http.post(`${this.API_BASE}/bookings/hotel`, payload);
  }

  bookBus(payload: any): Observable<any> {
    return this.http.post(`${this.API_BASE}/bookings/bus`, payload);
  }

  bookTour(payload: any): Observable<any> {
    return this.http.post(`${this.API_BASE}/bookings/tour-package`, payload);
  }

  getBookingById(bookingId: number): Observable<any> {
    return this.http.get<any>(`${this.API_BASE}/bookings/${bookingId}`);
  }

  getCustomerBookingsPage(customerId: number, page = 0, size = 5): Observable<any> {
    return this.http.get<any>(
      `${this.API_BASE}/bookings/customer/${customerId}?page=${page}&size=${size}&sortBy=bookingId&direction=desc`
    );
  }

  cancelBookingPartial(bookingId: number, payload: any): Observable<any> {
    return this.http.put<any>(`${this.API_BASE}/bookings/cancel/${bookingId}`, payload);
  }

  cancelEntireBooking(bookingId: number, payload: any = {}): Observable<any> {
    return this.http.put<any>(`${this.API_BASE}/bookings/cancel-all/${bookingId}`, payload);
  }

  // ==========================================
  // 3. ITINERARY ENDPOINTS
  // ==========================================
  getUpcomingItineraries(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API_BASE}/itineraries/upcoming`);
  }

  // ==========================================
  // 4. PASSENGER DIRECTORY ENDPOINTS
  // ==========================================
  getPassengerDirectory(): Observable<PassengerProfile[]> {
    return this.http.get<PassengerProfile[]>(`${this.API_BASE}/passengers`);
  }

  savePassengerProfile(passenger: PassengerProfile): Observable<any> {
    return this.http.post(`${this.API_BASE}/passengers`, passenger);
  }

  deletePassengerProfile(profileId: number): Observable<any> {
    return this.http.delete(`${this.API_BASE}/passengers/${profileId}`);
  }

  // ==========================================
  // 5. UNIFIED BILLING & INVOICE ENDPOINTS
  // ==========================================
  getInvoiceById(invoiceId: number): Observable<InvoiceResponseDTO> {
    return this.http.get<InvoiceResponseDTO>(`${this.API_BASE}/invoices/${invoiceId}`);
  }

  getCustomerInvoices(customerId: number, page = 0, size = 50): Observable<PageResponse<InvoiceResponseDTO>> {
    return this.http.get<PageResponse<InvoiceResponseDTO>>(
      `${this.API_BASE}/invoices/customer/${customerId}?page=${page}&size=${size}&sortBy=invoiceId&direction=desc`
    );
  }

  executePayment(request: { invoiceId: number; method: string }): Observable<string> {
    return this.http.post(`${this.API_BASE}/payments`, request, { responseType: 'text' });
  }

  // ==========================================
  // 6. CUSTOMER ↔ AGENT WORKFLOW ENDPOINTS
  // ==========================================
  createBookingRequest(payload: { inventoryId: number; customerRequirements: string }): Observable<any> {
    return this.http.post(`${this.API_BASE}/booking-requests`, payload);
  }

  getMyBookingRequests(page = 0, size = 10): Observable<any> {
    return this.http.get(`${this.API_BASE}/booking-requests/mine?page=${page}&size=${size}`);
  }

  // ==========================================
  // 🎯 7. COMPLAINTS ENGINE API INTEGRATION
  // ==========================================
  
  /** Customer raises a new complaint dispute record optionally bound to a booking id */
  raiseComplaint(payload: { subject: string; description: string; relatedBookingId?: number | null }): Observable<ComplaintResponseDTO> {
    return this.http.post<ComplaintResponseDTO>(`${this.API_BASE}/complaints`, payload);
  }

  /** Pulls a customer's logged complaints workspace array history containing status parameters */
  getMyComplaints(page = 0, size = 10): Observable<PageResponse<ComplaintResponseDTO>> {
    return this.http.get<PageResponse<ComplaintResponseDTO>>(
      `${this.API_BASE}/complaints?page=${page}&size=${size}&sortBy=createdAt&direction=desc`
    );
  }

  getUserById(userId: number): Observable<UserResponseDTO> {
    return this.http.get<UserResponseDTO>(`${this.API_BASE}/users/${userId}`);
  }

    /** Updates a user profile structure matching explicit RBAC permissions */
  updateUserProfile(userId: number, payload: any): Observable<UserResponseDTO> {
    return this.http.put<UserResponseDTO>(`${this.API_BASE}/users/${userId}`, payload);
  }

  /** Challenges structural credentials to rotate account password sequences safely */
  changePassword(payload: any): Observable<string> {
    return this.http.put(`${this.API_BASE}/users/change-password`, payload, { responseType: 'text' });
  }
}