import { Component, OnInit, ChangeDetectorRef, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PassengerProfile } from '../../../shared/models/passenger-profile';
import { TravelService } from '../../../core/services/travel-service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-passenger-directory',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './passenger-directory.html',
  styleUrls: ['./passenger-directory.scss']
})
export class PassengerDirectory implements OnInit {
  constructor(private travelService: TravelService, private cdr: ChangeDetectorRef) {}

  @Input() showSelection: boolean = false; 
  @Output() selectionChange = new EventEmitter<number[]>();

  savedPassengers: PassengerProfile[] = [];
  selectedPassengerIds: number[] = [];
  showPassengerForm = false;

  formErrorMessage: string | null = null; 

  newPassenger: PassengerProfile = {
    name: '', age: 0, gender: 'Male', idProofType: 'PASSPORT', idProofNumber: ''
  };

  ngOnInit(): void {
    this.refreshDirectory();
  }

  refreshDirectory(): void {
    this.travelService.getPassengerDirectory().subscribe({
      next: (data: PassengerProfile[]) => {
        this.savedPassengers = data || [];
        this.cdr.detectChanges();
      }
    });
  }

  togglePassenger(id: number): void {
    if (!this.showSelection) return;
    
    const index = this.selectedPassengerIds.indexOf(id);
    if (index > -1) {
      this.selectedPassengerIds.splice(index, 1);
    } else {
      this.selectedPassengerIds.push(id);
    }
    this.selectionChange.emit([...this.selectedPassengerIds]);
    this.cdr.detectChanges();
  }

  addPassenger(): void {
    this.formErrorMessage = null; 

    if (!this.newPassenger.name || !this.newPassenger.age || !this.newPassenger.idProofNumber) {
      this.formErrorMessage = 'Please fill out all mandatory identity fields before saving.';
      this.cdr.detectChanges();
      return;
    }
    
    this.travelService.savePassengerProfile(this.newPassenger).subscribe({
      next: () => {
        this.showPassengerForm = false;
        this.formErrorMessage = null;
        this.newPassenger = { name: '', age: 0, gender: 'Male', idProofType: 'PASSPORT', idProofNumber: '' };
        this.refreshDirectory();
      },
      error: (err: HttpErrorResponse) => {
        console.error('Passenger save intercept diagnostics log:', err);

        if (err.error) {
          if (typeof err.error === 'object') {
            this.formErrorMessage = err.error.message || err.error.error || JSON.stringify(err.error);
          } else if (typeof err.error === 'string') {
            try {
              const convertedToken = JSON.parse(err.error);
              this.formErrorMessage = convertedToken.message || convertedToken.error || err.error;
            } catch {
              this.formErrorMessage = err.error;
            }
          }
        } else {
          this.formErrorMessage = err.message || 'An unexpected verification error occurred on the server.';
        }
        
        this.cdr.markForCheck();
        this.cdr.detectChanges();
      }
    });
  }

  deletePassenger(profileId: number | undefined): void {
    if (!profileId) return;
    this.formErrorMessage = null;

    if (confirm('Remove companion profile from directory roster?')) {
      this.travelService.deletePassengerProfile(profileId).subscribe({
        next: () => {
          this.selectedPassengerIds = this.selectedPassengerIds.filter(id => id !== profileId);
          this.selectionChange.emit([...this.selectedPassengerIds]);
          this.formErrorMessage = null;
          this.refreshDirectory();
        },
        error: (err: HttpErrorResponse) => {
          if (err.status === 409 || (err.message && err.message.includes('constraint'))) {
            this.formErrorMessage = 'Cannot delete profile: This traveler is currently linked to an active or historical trip reservation record.';
          } else {
            this.formErrorMessage = 'Server execution failed. Unable to drop profile record instance.';
          }
          this.cdr.markForCheck();
          this.cdr.detectChanges(); 
        }
      });
    }
  }

  // 🎯 REFACTORED: Synchronized change detection updates when toggling form state layers manually
  toggleFormWindow(): void {
    this.showPassengerForm = !this.showPassengerForm;
    this.formErrorMessage = null;
    this.cdr.detectChanges();
  }
}