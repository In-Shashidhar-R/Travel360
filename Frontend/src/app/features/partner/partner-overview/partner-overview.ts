import { Component, Input, OnInit, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-partner-overview',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './partner-overview.html',
  styleUrls: ['./partner-overview.scss']
})
export class PartnerOverview implements OnInit, OnChanges {
  @Input() partnerId = 0;
  @Input() partnerName = '';
  @Input() totalServices = 0;
  @Input() activeServices = 0;
  @Input() inactiveServices = 0;

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    // Ensures accurate programmatic variable calculation immediately on setup layout pass
    this.cdr.markForCheck();
  }

  ngOnChanges(changes: SimpleChanges) {
    // Forces rendering runtime compilation loops to eliminate double clicks on asynchronous data loads
    this.cdr.detectChanges();
  }
}