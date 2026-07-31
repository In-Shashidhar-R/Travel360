import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy, ChangeDetectorRef, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-manage-services',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './manage-services.html',
  styleUrls: ['./manage-services.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ManageServices implements OnChanges {
  @Input() inventories: any[] = [];
  @Input() totalServices = 0;
  
  @Output() onActivate = new EventEmitter<number>();
  @Output() onDeactivate = new EventEmitter<number>();

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnChanges(changes: SimpleChanges): void {
    // Intercepts input mutation cycles triggered by the wrapper layout container canvas array push events
    this.cdr.markForCheck();
  }

  triggerActivate(id: number) {
    this.onActivate.emit(id);
    this.cdr.detectChanges();
  }

  triggerDeactivate(id: number) {
    this.onDeactivate.emit(id);
    this.cdr.detectChanges();
  }
}