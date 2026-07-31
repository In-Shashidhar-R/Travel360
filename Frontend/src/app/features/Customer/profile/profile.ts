import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TravelService } from '../../../core/services/travel-service';
import { UserResponseDTO } from '../../../shared/models/user.model';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css' // 🎯 Scoped styles block removed completely from metadata configuration parameters
})
export class Profile implements OnInit {
  user: UserResponseDTO = {
    userId: 0,
    name: '',
    email: '',
    role: '',
    phone: '',
    address: '',
    city: '',
    state: '',
    country: '',
    dateOfBirth: '',
    gender: 'MALE'
  };

  isEditingPersonal = false;
  isEditingAddress = false;
  isChangingPassword = false;
  isLoading = false;

  successMessage = '';
  errorMessage = '';

  editForm = {
    name: '',
    phone: '',
    address: '',
    city: '',
    state: '',
    country: '',
    gender: '',
    dateOfBirth: ''
  };

  passwordForm = {
    currentPassword: '',
    newPassword: ''
  };

  constructor(
    private router: Router, 
    private travelService: TravelService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const session = localStorage.getItem('travel360_session');
    if (session) {
      this.user.userId = JSON.parse(session).userId;
    }
    this.syncProfileFromServer();
  }

  syncProfileFromServer(): void {
    this.isLoading = true;
    this.travelService.getUserById(this.user.userId).subscribe({
      next: (data: UserResponseDTO) => {
        this.user = data;
        this.resetEditForms();
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        this.extractErrorMessage(err);
        this.resetEditForms();
        this.cdr.detectChanges();
      }
    });
  }

  resetEditForms(): void {
    this.editForm = {
      name: this.user.name || '',
      phone: this.user.phone || '',
      address: this.user.address || '',
      city: this.user.city || '',
      state: this.user.state || '',
      country: this.user.country || '',
      gender: this.user.gender || 'MALE',
      dateOfBirth: this.user.dateOfBirth || ''
    };
    this.passwordForm = { currentPassword: '', newPassword: '' };
  }

  toggleEditSection(section: 'PERSONAL' | 'ADDRESS' | 'PASSWORD'): void {
    this.clearAlerts();
    this.resetEditForms();
    
    if (section === 'PERSONAL') this.isEditingPersonal = !this.isEditingPersonal;
    if (section === 'ADDRESS') this.isEditingAddress = !this.isEditingAddress;
    if (section === 'PASSWORD') this.isChangingPassword = !this.isChangingPassword;
  }

  saveProfileChanges(section: 'PERSONAL' | 'ADDRESS'): void {
    this.clearAlerts();
    this.isLoading = true;

    this.travelService.updateUserProfile(this.user.userId, this.editForm).subscribe({
      next: (updatedUser: UserResponseDTO) => {
        this.user = updatedUser;
        this.successMessage = 'Profile data committed successfully.';
        if (section === 'PERSONAL') this.isEditingPersonal = false;
        if (section === 'ADDRESS') this.isEditingAddress = false;
        this.isLoading = false;
        this.syncProfileFromServer();
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        this.extractErrorMessage(err);
        this.cdr.detectChanges();
      }
    });
  }

  executePasswordRotation(): void {
    this.clearAlerts();
    
    if (!this.passwordForm.currentPassword || this.passwordForm.newPassword.length < 6) {
      this.errorMessage = 'Password verification failed. New password must be at least 6 characters.';
      return;
    }

    this.isLoading = true;
    this.cdr.detectChanges();

    this.travelService.changePassword(this.passwordForm).subscribe({
      next: (response: string) => {
        this.successMessage = response || 'Password altered successfully.';
        this.isChangingPassword = false;
        this.passwordForm = { currentPassword: '', newPassword: '' };
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        this.extractErrorMessage(err);
        this.cdr.detectChanges();
      }
    });
  }

  private extractErrorMessage(err: HttpErrorResponse): void {
    if (err.error) {
      if (typeof err.error === 'string') {
        try {
          const parsed = JSON.parse(err.error);
          this.errorMessage = parsed.message || parsed.error || err.error;
        } catch (e) {
          this.errorMessage = err.error;
        }
      } else {
        this.errorMessage = err.error.message || err.error.error || 'Server processing error occurred.';
      }
    } else {
      this.errorMessage = err.message || 'Operation failed. Please check backend connection.';
    }
  }

  clearAlerts(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  goHome = (): void => {
    this.router.navigate(['/customer/inventories']);
  }
}