import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookingRequestResponseDTO } from '../../../shared/models/booking-request.model';
import { UserDirectoryDTO } from '../../../shared/models/user.model';
import { PartnerResponseDTO, PartnerRequestDTO } from '../../../shared/models/partner.model';
import { AgentRequestDTO } from '../../../shared/models/agent.model';
import { AuditLogResponseDTO, ComplaintResponseDTO } from '../../../shared/models/compliance.model';
import { 
  FlightInventoryRequestDTO, 
  FlightInventoryResponseDTO,
  HotelInventoryRequestDTO, 
  HotelInventoryResponseDTO,
  BusInventoryRequestDTO, 
  BusInventoryResponseDTO,
  CabInventoryRequestDTO, 
  CabInventoryResponseDTO,
  TourInventoryRequestDTO, 
  TourInventoryResponseDTO
} from '../../../shared/models/inventory.model';
import { BaseBookingResponseDTO, PageResponse } from '../../../shared/models/booking.model';
import { AnalyticsDashboardDTO, InvoiceResponseDTO, PaymentResponseDTO } from '../../../shared/models/finance.model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly BASE_API = 'http://localhost:9095/api/v1';
  private readonly INVENTORY_API = `${this.BASE_API}/inventories`;

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<any> {
    return this.http.get<any>(`${this.BASE_API}/users?page=0&size=10000`);
  }

  getAllPartners(): Observable<any> {
    return this.http.get<any>(`${this.BASE_API}/partners?page=0&size=10000`);
  }

  getUserById(userId: number): Observable<any> {
    return this.http.get<any>(`${this.BASE_API}/users/${userId}`);
  }

  registerPartner(payload: PartnerRequestDTO): Observable<any> {
    return this.http.post(`${this.BASE_API}/partners`, payload);
  }

  registerAgent(payload: AgentRequestDTO, adminId: number): Observable<any> {
    const params = new HttpParams().set('adminId', adminId.toString());
    return this.http.post(`${this.BASE_API}/users/agent`, payload, { params });
  }

  updateUserProfile(userId: number, payload: any): Observable<any> {
    return this.http.put<any>(`${this.BASE_API}/users/${userId}`, payload);
  }

  updatePartnerProfile(partnerId: number, payload: any): Observable<any> {
    return this.http.put<any>(`${this.BASE_API}/partners/${partnerId}`, payload);
  }

  changePassword(payload: any): Observable<any> {
    return this.http.put<any>(`${this.BASE_API}/users/change-password`, payload, { responseType: 'text' as 'json' });
  }

  provisionFlight(payload: FlightInventoryRequestDTO): Observable<FlightInventoryResponseDTO> {
    return this.http.post<FlightInventoryResponseDTO>(`${this.INVENTORY_API}/flight`, payload);
  }

  provisionHotel(payload: HotelInventoryRequestDTO): Observable<HotelInventoryResponseDTO> {
    return this.http.post<HotelInventoryResponseDTO>(`${this.INVENTORY_API}/hotel`, payload);
  }

  provisionBus(payload: BusInventoryRequestDTO): Observable<BusInventoryResponseDTO> {
    return this.http.post<BusInventoryResponseDTO>(`${this.INVENTORY_API}/bus`, payload);
  }

  provisionCab(payload: CabInventoryRequestDTO): Observable<CabInventoryResponseDTO> {
    return this.http.post<CabInventoryResponseDTO>(`${this.INVENTORY_API}/cab`, payload);
  }

  provisionTour(payload: TourInventoryRequestDTO): Observable<TourInventoryResponseDTO> {
    return this.http.post<TourInventoryResponseDTO>(`${this.INVENTORY_API}/tour-package`, payload);
  }

  updateFlight(inventoryId: number, payload: FlightInventoryRequestDTO): Observable<FlightInventoryResponseDTO> {
    return this.http.put<FlightInventoryResponseDTO>(`${this.INVENTORY_API}/flight/${inventoryId}`, payload);
  }

  updateHotel(inventoryId: number, payload: HotelInventoryRequestDTO): Observable<HotelInventoryResponseDTO> {
    return this.http.put<HotelInventoryResponseDTO>(`${this.INVENTORY_API}/hotel/${inventoryId}`, payload);
  }

  updateBus(inventoryId: number, payload: BusInventoryRequestDTO): Observable<BusInventoryResponseDTO> {
    return this.http.put<BusInventoryResponseDTO>(`${this.INVENTORY_API}/bus/${inventoryId}`, payload);
  }

  updateCab(inventoryId: number, payload: CabInventoryRequestDTO): Observable<CabInventoryResponseDTO> {
    return this.http.put<CabInventoryResponseDTO>(`${this.INVENTORY_API}/cab/${inventoryId}`, payload);
  }

  updateTour(inventoryId: number, payload: TourInventoryRequestDTO): Observable<TourInventoryResponseDTO> {
    return this.http.put<TourInventoryResponseDTO>(`${this.INVENTORY_API}/tour-package/${inventoryId}`, payload);
  }

  getInventoryById(inventoryId: number, targetDate?: string): Observable<any> {
    let params = new HttpParams();
    if (targetDate) params = params.set('targetDate', targetDate);
    return this.http.get<any>(`${this.INVENTORY_API}/${inventoryId}`, { params });
  }

  activateInventory(inventoryId: number): Observable<any> {
    return this.http.put(`${this.INVENTORY_API}/${inventoryId}/activate`, {});
  }

  deactivateInventory(inventoryId: number): Observable<any> {
    return this.http.put(`${this.INVENTORY_API}/${inventoryId}/deactivate`, {});
  }

  deleteInventory(inventoryId: number, type: 'flight' | 'hotel' | 'bus' | 'cab' | 'tour-package'): Observable<string> {
    return this.http.delete(`${this.INVENTORY_API}/${type}/${inventoryId}`, { responseType: 'text' });
  }

  searchInventories(source: string, destination: string, targetDate?: string): Observable<any[]> {
    let params = new HttpParams().set('source', source).set('destination', destination);
    if (targetDate) params = params.set('targetDate', targetDate);
    return this.http.get<any[]>(`${this.INVENTORY_API}/search`, { params });
  }

  getFilteredInventories(filters: Record<string, any>): Observable<any[]> {
    let params = new HttpParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, value.toString());
      }
    });
    return this.http.get<any[]>(`${this.INVENTORY_API}/filter`, { params });
  }

  getAllBookingRequests(): Observable<BookingRequestResponseDTO[]> {
    return this.http.get<BookingRequestResponseDTO[]>(`${this.BASE_API}/booking-requests/all`);
  }

  getAllBookings(page: number, size: number, sortBy: string = 'bookingId', direction: string = 'desc'): Observable<PageResponse<BaseBookingResponseDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.get<PageResponse<BaseBookingResponseDTO>>(`${this.BASE_API}/bookings`, { params });
  }

  getAnalyticsDashboard(): Observable<AnalyticsDashboardDTO> {
    return this.http.get<AnalyticsDashboardDTO>(`${this.BASE_API}/analytics/dashboard`);
  }

  getAllInvoices(page: number, size: number, sortBy: string = 'invoiceId', direction: string = 'desc'): Observable<PageResponse<InvoiceResponseDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.get<PageResponse<InvoiceResponseDTO>>(`${this.BASE_API}/invoices`, { params });
  }

  getAllPayments(page: number, size: number, sortBy: string = 'paymentId', direction: string = 'desc'): Observable<PageResponse<PaymentResponseDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.get<PageResponse<PaymentResponseDTO>>(`${this.BASE_API}/payments`, { params });
  }

  getSystemAuditLogs(page: number, size: number, sortBy: string = 'timestamp', direction: string = 'desc'): Observable<PageResponse<AuditLogResponseDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.get<PageResponse<AuditLogResponseDTO>>(`${this.BASE_API}/audit-logs`, { params });
  }

  getSystemComplaints(page: number, size: number, sortBy: string = 'createdAt', direction: string = 'desc'): Observable<PageResponse<ComplaintResponseDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);
    return this.http.get<PageResponse<ComplaintResponseDTO>>(`${this.BASE_API}/complaints`, { params });
  }
}