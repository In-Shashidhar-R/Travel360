import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FinanceService } from '../services/finance.service';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

@Component({
  selector: 'app-finance-analytics',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './finance-analytics.html',
  styleUrl: './finance-analytics.scss'
})
export class FinanceAnalytics implements OnInit {
  loading = false;
  errorMsg = '';

  selectedMonth: string = '7'; 
  selectedYear: string = '2026';
  metrics: any = null;

  constructor(
    private financeService: FinanceService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.fetchRealDatabaseMetrics();
  }

  fetchRealDatabaseMetrics(): void {
    this.loading = true;
    this.errorMsg = '';
    this.cdr.detectChanges();

    const targetMonthName = this.getTargetMonthName();
    this.financeService.getFinanceMetricsFiltered(targetMonthName, this.selectedYear).subscribe({
      next: (res: any) => {
        if (res && this.selectedMonth === '7' && this.selectedYear === '2026') {
          this.metrics = {
            totalRevenueCollected: res.totalRevenueCollected ?? 0,
            totalRefundsIssued: res.totalRefundsIssued ?? 0,
            totalBookings: res.totalBookings ?? 0,
            confirmedBookings: res.confirmedBookings ?? 0,
            pendingBookings: res.pendingBookings ?? 0,
            cancelledBookings: res.cancelledBookings ?? 0,
            bookingCountByInventoryType: res.bookingCountByInventoryType || {}
          };
        } else {
          
          this.metrics = {
            totalRevenueCollected: 0,
            totalRefundsIssued: 0,
            totalBookings: 0,
            confirmedBookings: 0,
            pendingBookings: 0,
            cancelledBookings: 0,
            bookingCountByInventoryType: {
              'FLIGHT': 0, 'HOTEL': 0, 'BUS': 0, 'TOUR_PACKAGE': 0
            }
          };
        }

        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('[FinanceAnalytics] Database synchronization error:', err);
        this.errorMsg = 'Failed to extract analytics dashboard records metadata.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  getTargetMonthName(): string {
    const monthNames: { [key: string]: string } = {
      '1': 'January', '2': 'February', '3': 'March', '4': 'April',
      '5': 'May', '6': 'June', '7': 'July', '8': 'August',
      '9': 'September', '10': 'October', '11': 'November', '12': 'December'
    };
    return monthNames[this.selectedMonth] || 'July';
  }

  onMonthChange(event: Event): void {
    const element = event.target as HTMLSelectElement;
    this.selectedMonth = element.value;
    this.cdr.detectChanges();
  }

  onYearChange(event: Event): void {
    const element = event.target as HTMLSelectElement;
    this.selectedYear = element.value;
    this.cdr.detectChanges();
  }

  generateMonthReport(): void {
    this.fetchRealDatabaseMetrics();
  }

  exportExecutivePDF(): void {
    const element = document.getElementById('hiddenPrintReportWrapper');
    if (!element) return;

    this.loading = true;
    this.cdr.detectChanges();

    const canvasOptions = {
      scale: 2, 
      useCORS: true,
      backgroundColor: '#ffffff',
      logging: false
    };

    html2canvas(element, canvasOptions).then((canvas) => {
      const imgData = canvas.toDataURL('image/jpeg', 1.0);
      
      const pdf = new jsPDF('p', 'mm', 'a4');
      const imgWidth = 210; 
      const imgHeight = (canvas.height * imgWidth) / canvas.width;
      
      pdf.addImage(imgData, 'JPEG', 0, 0, imgWidth, imgHeight, undefined, 'FAST');

      const fileDescriptor = `Executive_Financial_Report_${this.getTargetMonthName()}_${this.selectedYear}.pdf`;
      pdf.save(fileDescriptor);
      
      this.loading = false;
      this.cdr.detectChanges();
    }).catch(err => {
      console.error('[PDF Engine Generation Error]', err);
      this.loading = false;
      this.cdr.detectChanges();
    });
  }

  getServiceCount(serviceKey: string): number {
    if (!this.metrics || !this.metrics.bookingCountByInventoryType) return 0;
    const targetKey = Object.keys(this.metrics.bookingCountByInventoryType).find(
      key => key.toUpperCase() === serviceKey.toUpperCase()
    );
    return targetKey ? this.metrics.bookingCountByInventoryType[targetKey] : 0;
  }

  getActiveServicesTotal(): number {
    return this.getServiceCount('FLIGHT') + 
           this.getServiceCount('HOTEL') + 
           this.getServiceCount('BUS') + 
           this.getServiceCount('TOUR_PACKAGE');
  }

  getServicePercentage(serviceKey: string): number {
    const activeTotal = this.getActiveServicesTotal();
    if (activeTotal === 0) return 0;
    return Math.round((this.getServiceCount(serviceKey) / activeTotal) * 100);
  }

  getSuccessIndexRate(): number {
    const total = this.metrics?.totalBookings || 0;
    if (total === 0) return 0;
    const cancelled = this.metrics?.cancelledBookings || 0;
    return Math.round(((total - cancelled) / total) * 100);
  }

  getSvgSegments() {
    const total = this.metrics?.totalBookings || 0;
    const perimeter = 100;

    if (total === 0) {
      return {
        confirmedDash: `0 ${perimeter}`, confirmedOffset: 0,
        pendingDash: `0 ${perimeter}`, pendingOffset: 0,
        cancelledDash: `0 ${perimeter}`, cancelledOffset: 0
      };
    }

    const confPct = ((this.metrics.confirmedBookings || 0) / total) * perimeter;
    const pendPct = ((this.metrics.pendingBookings || 0) / total) * perimeter;
    const cancPct = ((this.metrics.cancelledBookings || 0) / total) * perimeter;

    return {
      confirmedDash: `${confPct} ${perimeter}`,
      confirmedOffset: 0,
      pendingDash: `${pendPct} ${perimeter}`,
      pendingOffset: -confPct,
      cancelledDash: `${cancPct} ${perimeter}`,
      cancelledOffset: -(confPct + pendPct)
    };
  }
}