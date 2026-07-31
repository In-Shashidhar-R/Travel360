import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  AuditLogResponseDTO,
  PageResponse,
  ComplianceReportDTO,
  AnalyticsDashboardDTO,
  ComplaintResponseDTO,
  ComplaintResolveDTO
} from '../../features/compliance/models/compliance.model';


@Injectable({
  providedIn: 'root'
})
export class ComplianceService {

  private baseUrl = 'http://localhost:9095/api/v1';

  constructor(
    private http: HttpClient,
  ) {}

  getAllLogs(page: number, size: number): Observable<PageResponse<AuditLogResponseDTO>> {

    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
console.log(params)
    return this.http.get<PageResponse<AuditLogResponseDTO>>(
      `${this.baseUrl}/audit-logs`,{ params }
    );
  }

  getLogsByUser(userId: number, page: number, size: number): Observable<PageResponse<AuditLogResponseDTO>> {

    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    console.log(params)
    return this.http.get<PageResponse<AuditLogResponseDTO>>(
      `${this.baseUrl}/audit-logs/user/${userId}`,
      { params }
    );
  }

  getLogsByAction(action: string, page: number, size: number): Observable<PageResponse<AuditLogResponseDTO>> {

    const params = new HttpParams()
      .set('action', action)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<AuditLogResponseDTO>>(
      `${this.baseUrl}/audit-logs/by-action`,
      { params }
    );
  }

  getComplianceReport(from?: string, to?: string): Observable<ComplianceReportDTO> {

    let params = new HttpParams();

    if (from) {
      params = params.set('from', from);
    }

    if (to) {
      params = params.set('to', to);
    }

    return this.http.get<ComplianceReportDTO>(
      `${this.baseUrl}/audit-logs/compliance-report`,
      { params }
    );
  }

  getAnalyticsDashboard(): Observable<AnalyticsDashboardDTO> {

    return this.http.get<AnalyticsDashboardDTO>(
      `${this.baseUrl}/analytics/dashboard`,
      // { headers }
    );
  }

  getAllComplaints(page: number, size: number): Observable<PageResponse<ComplaintResponseDTO>> {

    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<ComplaintResponseDTO>>(
      `${this.baseUrl}/complaints`,
     { params }
    );
  }

  markInProgress(
    complaintId: number,
    body: ComplaintResolveDTO
  ): Observable<ComplaintResponseDTO> {


    return this.http.patch<ComplaintResponseDTO>(
      `${this.baseUrl}/complaints/${complaintId}/in-progress`,
      body,
      // { headers }
    );
  }

  resolveComplaint(
    complaintId: number,
    body: ComplaintResolveDTO
  ): Observable<ComplaintResponseDTO> {


    return this.http.patch<ComplaintResponseDTO>(
      `${this.baseUrl}/complaints/${complaintId}/resolve`,
      body,
      // { headers }
    );
  }
}
