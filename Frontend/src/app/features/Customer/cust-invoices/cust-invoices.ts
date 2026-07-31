import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BillingService } from '../../../core/services/billing-service';
import { TravelService } from '../../../core/services/travel-service';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize, switchMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-cust-invoices',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cust-invoices.html',
  styleUrl: './cust-invoices.scss'
})
export class CustInvoices implements OnInit {
  invoices: any[] = [];
  isLoading = false;
  errorMessage: string | null = null;

  currentPage = 0;
  pageSize = 5;
  totalPages = 0;
  totalElements = 0;
  isLastPage = false;

  constructor(
    private billingService: BillingService,
    private travelService: TravelService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.fetchInvoices(0);
  }

  fetchInvoices(pageIndex: number = 0): void {
    const sessionUser = localStorage.getItem('travel360_session');
    if (!sessionUser) return;
    
    const currentUserId = JSON.parse(sessionUser).userId;
    this.currentPage = pageIndex;
    this.isLoading = true;
    this.errorMessage = null;

    this.billingService.getCustomerInvoices(currentUserId, this.currentPage, this.pageSize)
      .pipe(
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (response: any) => {
          this.invoices = response.content || [];
          this.totalPages = response.totalPages || 1;
          this.totalElements = response.totalElements || this.invoices.length;
          this.isLastPage = response.last !== undefined ? response.last : (this.currentPage >= this.totalPages - 1);
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage = err.error?.message || 'Failed to sync your accounting statement list entries.';
        }
      });
  }

  getInvoiceStatusClass(status: string): string {
    switch ((status || '').toUpperCase()) {
      case 'PAID': return 'bg-success-soft text-success border border-success-subtle';
      case 'UNPAID': return 'bg-warning-soft text-warning border border-warning-subtle';
      case 'REFUNDED': return 'bg-info-soft text-info border border-info-subtle';
      case 'CANCELLED': return 'bg-danger-soft text-danger border border-danger-subtle';
      default: return 'bg-light text-secondary border';
    }
  }

  downloadInvoicePDF(invoiceSummary: any): void {
    this.isLoading = true;
    this.cdr.detectChanges();
    
    this.billingService.getInvoiceById(invoiceSummary.invoiceId)
      .pipe(
        switchMap((invoiceData: any) => {
          return this.travelService.getBookingById(invoiceData.bookingId).pipe(
            switchMap((bookingData: any) => {
              return of({ invoice: invoiceData, booking: bookingData });
            }),
            catchError(() => {
              return of({ invoice: invoiceData, booking: null });
            })
          );
        }),
        finalize(() => {
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (compositePayload: { invoice: any, booking: any }) => {
          this.renderPrintCanvasWindow(compositePayload.invoice, compositePayload.booking);
        },
        error: () => {
          this.renderPrintCanvasWindow(invoiceSummary, null);
        }
      });
  }

  private renderPrintCanvasWindow(invoice: any, booking: any): void {
    const iframe = document.createElement('iframe');
    iframe.style.position = 'absolute';
    iframe.style.width = '0px';
    iframe.style.height = '0px';
    iframe.style.border = 'none';
    iframe.name = `print_frame_${invoice.invoiceId}`;

    document.body.appendChild(iframe);

    const iframeDoc = iframe.contentWindow?.document || iframe.contentDocument;
    if (!iframeDoc) {
      document.body.removeChild(iframe);
      return;
    }

    const itemType = booking?.itemType || invoice.itemType || 'SERVICE RESERVATION';
    const partnerName = booking?.partnerName || 'Travel 360° Network Operator';
    
    const statusUpper = (invoice.status || '').toUpperCase();
    const statusColorClass = statusUpper === 'PAID' ? 'text-success' : statusUpper === 'UNPAID' ? 'text-warning' : 'text-secondary';

    iframeDoc.open();
    iframeDoc.write(`
      <html>
        <head>
          <title>Invoice_Statement_#${invoice.invoiceId}</title>
          <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
          <style>
            @page { size: auto; margin: 0mm; }
            body { 
              font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; 
              color: #0f172a; 
              padding: 50px; 
              background: #ffffff !important; 
              font-size: 0.85rem; 
            }
            .invoice-title { font-weight: 800; letter-spacing: -0.02em; color: #0f172a; font-size: 1.35rem; }
            .meta-label { text-transform: uppercase; font-size: 0.68rem; color: #64748b; font-weight: 700; letter-spacing: 0.05em; margin-bottom: 2px;}
            .fs-8 { font-size: 0.78rem !important; }
            .border-thick { border-top: 2px solid #0f172a !important; }
          </style>
        </head>
        <body>
          <div class="d-flex justify-content-between align-items-center border-bottom pb-3 mb-4">
            <div>
              <h2 class="invoice-title mb-0">TRAVEL 360° NETWORK STATEMENT</h2>
            </div>
            <div>
              <span class="fw-extrabold text-uppercase ${statusColorClass}" style="font-size: 0.9rem; letter-spacing: 0.5px;">
                ● STATUS: <strong>${statusUpper}</strong>
              </span>
            </div>
          </div>
          
          <div class="row mb-4 g-3 text-start">
            <div class="col-6">
              <div class="meta-label">Billed Customer Profile</div>
              <div class="fw-bold text-dark text-capitalize" style="font-size: 0.95rem;">${invoice.customerName || booking?.customerName || 'Statement Guest'}</div>
              <div class="small text-secondary mt-1">Invoice Record: <b>#${invoice.invoiceId}</b></div>
              <div class="small text-muted" style="font-size:0.72rem;">Issue Timestamp: ${invoice.generatedDate || ''}</div>
            </div>
            <div class="col-6 text-end">
              <div class="meta-label">Transaction Reference</div>
              <div class="fw-bold text-dark">Booking Registry: #${invoice.bookingId}</div>
              <div class="small text-muted mt-1">Category Segment: <span class="badge bg-light text-dark border font-monospace text-uppercase">${itemType}</span></div>
            </div>
          </div>

          <div class="card p-3 mb-4 bg-light bg-opacity-50 border shadow-none rounded-3 text-start">
            <div class="row text-center font-monospace">
              <div class="col-6 text-start border-end ps-3">
                <span class="text-muted d-block small" style="font-size:0.68rem;">OPERATOR PROVIDER ENGINE</span>
                <span class="fw-bold text-dark text-uppercase">${partnerName}</span>
              </div>
              <div class="col-6 text-start ps-3">
                <span class="text-muted d-block small" style="font-size:0.68rem;">CABIN SELECTION SECTOR</span>
                <span class="fw-bold text-dark text-uppercase">${booking?.chosenSeatType || 'STANDARD ALLOCATION TIER'}</span>
              </div>
            </div>
          </div>

          <table class="table align-middle shadow-none mt-2 text-center">
            <thead class="table-light text-uppercase font-monospace text-muted" style="font-size:0.7rem;">
              <tr>
                <th class="py-2 px-3 text-start">Billing Allocation Target Description</th>
                <th class="text-end py-2 px-3">Total Cost</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td class="p-3 text-start fs-8">
                  <div class="fw-bold text-dark">Travel 360° Core Management Pipeline Inventory Resource Allotment</div>
                  <small class="text-muted">Automated ledger generation parameters calculation summary.</small>
                </td>
                <td class="text-end fw-extrabold font-monospace px-3"><strong>₹${Number(invoice.amount).toFixed(2)}</strong></td>
              </tr>
              <tr class="table-light border-thick">
                <td class="text-end fw-bold text-uppercase small p-3 fs-8 text-secondary">Statement Sum Ledger:</td>
                <td class="text-end fw-extrabold text-success font-monospace px-3 fs-4"><strong>₹${Number(invoice.amount).toFixed(2)}</strong></td>
              </tr>
            </tbody>
          </table>

          <div class="mt-5 pt-3 border-top text-center text-muted" style="font-size:0.68rem; letter-spacing: 0.3px;">
            <p class="mb-0">This document represents an automated financial transaction calculation compiled by Travel 360° Core Ledger Systems.</p>
          </div>
        </body>
      </html>
    `);
    iframeDoc.close();

    setTimeout(() => {
      iframe.contentWindow?.focus();
      iframe.contentWindow?.print();
      
      setTimeout(() => {
        document.body.removeChild(iframe);
      }, 1000);
    }, 500);
  }

  goToNextPage(): void { if (!this.isLastPage) this.fetchInvoices(this.currentPage + 1); }
  goToPreviousPage(): void { if (this.currentPage > 0) this.fetchInvoices(this.currentPage - 1); }
}