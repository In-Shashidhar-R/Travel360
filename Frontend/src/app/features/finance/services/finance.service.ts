import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class FinanceService {
  private baseUrl = 'http://localhost:9095/api/v1'; 

  constructor(private http: HttpClient) {}

  getAllInvoices(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/invoices`);
  }

  getAllPayments(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/payments`);
  }
  
  getFinanceMetricsFiltered(month: string, year: string): Observable<any> {
    const params = new HttpParams()
      .set('month', month)
      .set('year', year);
    return this.http.get<any>(`${`${this.baseUrl}/analytics/dashboard`}`, { params });
  }
}