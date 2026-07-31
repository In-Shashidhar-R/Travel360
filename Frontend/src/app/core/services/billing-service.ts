import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs'; // 👈 IMPORTED SUBJECT
import { NotificationResponseDTO } from '../../shared/models/notification-model';

export interface PaymentRequestDTO {
  invoiceId: number;
  method: string;
}

export interface InvoiceResponseDTO {
  invoiceId: number;
  bookingId: number;
  customerName: string;
  amount: number;
  generatedDate: string; 
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class BillingService {
  private API_BASE = 'http://localhost:9095/api/v1';

  // 🎯 NEW: Broadcast channel for global notification updates
  private notificationRefreshSource = new Subject<void>();
  
  // 🎯 NEW: The layout will subscribe to this variable to listen for updates
  notificationRefresh$ = this.notificationRefreshSource.asObservable();

  constructor(private http: HttpClient) {}

  // 🎯 NEW: Call this from any component after a successful booking/cancellation/payment
  triggerNotificationRefresh(): void {
    this.notificationRefreshSource.next();
  }

  getInvoiceById(invoiceId: number): Observable<InvoiceResponseDTO> {
    return this.http.get<InvoiceResponseDTO>(`${this.API_BASE}/invoices/${invoiceId}`);
  }

  executePayment(request: PaymentRequestDTO): Observable<string> {
    return this.http.post(`${this.API_BASE}/payments`, request, { responseType: 'text' });
  }

  getCustomerInvoices(customerId: number, page = 0, size = 5): Observable<any> {
    return this.http.get<any>(`${this.API_BASE}/invoices/customer/${customerId}?page=${page}&size=${size}`);
  }

  // 🎯 UPDATED: unreadOnly=false ensures that read logs are retained inside the payload stream
  getRecentNotifications(userId: number, page = 0, size = 10): Observable<any> {
    return this.http.get<any>(`${this.API_BASE}/notifications/user/${userId}?unreadOnly=false&page=${page}&size=${size}`);
  }

  markNotificationAsRead(notificationId: number, userId: number): Observable<NotificationResponseDTO> {
    return this.http.put<NotificationResponseDTO>(`${this.API_BASE}/notifications/${notificationId}/read?userId=${userId}`, {});
  }
}