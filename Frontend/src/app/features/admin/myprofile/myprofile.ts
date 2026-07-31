import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { AdminService } from '../services/admin.service';

@Component({
  selector: 'app-my-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './myprofile.html',
  styleUrl: './myprofile.scss'
})
export class MyProfile implements OnInit {
  isEditMode: boolean = false;
  
  alertMessage: string = '';
  alertType: 'success' | 'error' | '' = '';
  profileError: string = '';

  adminForm: any = { 
    id: null, name: '', phone: '', city: '', country: '', 
    address: '', state: '', gender: '', dateOfBirth: '' 
  };

  maxDobDate: string = (() => {
    const d = new Date();
    d.setFullYear(d.getFullYear() - 18);
    return d.toISOString().split('T')[0];
  })();

  constructor(
    public authService: AuthService,
    private adminService: AdminService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUser();
    if (user) {
      this.loadUserProfile(user);
    }
  }

  toggleEdit(mode: boolean): void {
    this.isEditMode = mode;
    if (!mode) {
      const user = this.authService.currentUser();
      if (user) this.loadUserProfile(user);
    }
    this.cdr.detectChanges();
  }

  loadUserProfile(user: any): void {
    this.profileError = '';
    this.adminService.getUserById(user.userId).subscribe({
      next: (fullProfile) => {
        this.adminForm = {
          id: fullProfile.userId,
          name: fullProfile.name,
          phone: fullProfile.phone || '',
          city: fullProfile.city || '',
          country: fullProfile.country || '',
          address: fullProfile.address || '',
          state: fullProfile.state || '',
          gender: fullProfile.gender || '',
          dateOfBirth: fullProfile.dateOfBirth || ''
        };
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.profileError = this.extractErrorMessage(err);
        this.cdr.detectChanges();
      }
    });
  }

  onPhoneInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.replace(/[^0-9]/g, '');
    this.adminForm.phone = input.value;
  }

  submitAdminProfileUpdate(): void {
    this.profileError = '';

    const nameRegex = /^[a-zA-Z\s\-'\.]+$/;
    if (!this.adminForm.name || !this.adminForm.name.trim()) {
      this.profileError = 'Validation Failure: Full identity name is mandatory.';
      return;
    }
    if (!nameRegex.test(this.adminForm.name.trim())) {
      this.profileError = 'Validation Failure: Name cannot contain numbers or special characters (<, >, ?, !).';
      return;
    }

    const phoneRegex = /^[0-9]{10}$/;
    if (!this.adminForm.phone || !phoneRegex.test(this.adminForm.phone.trim())) {
      this.profileError = 'Validation Failure: Phone number must be exactly 10 digits (numbers only).';
      return;
    }

    if (!this.adminForm.gender) {
      this.profileError = 'Validation Failure: Please select a designated gender parameter.';
      return;
    }

    if (!this.adminForm.dateOfBirth) {
      this.profileError = 'Validation Failure: Verified Date of Birth is required.';
      return;
    }

    const birthDate = new Date(this.adminForm.dateOfBirth);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    if (age < 18) {
      this.profileError = 'Validation Failure: Account holder must be at least 18 years old.';
      return;
    }

    const noHtmlRegex = /^[^<>]*$/;
    const locFields = ['address', 'city', 'state', 'country'];
    for (const field of locFields) {
      if (this.adminForm[field] && !noHtmlRegex.test(this.adminForm[field])) {
        this.profileError = `Validation Failure: HTML tags (<, >) are explicitly blocked in location fields (${field}).`;
        return;
      }
    }

    this.adminService.updateUserProfile(this.adminForm.id, this.adminForm).subscribe({
      next: (res) => {
        this.triggerAlert('success', 'Your administrative profile has been safely updated.');
        this.authService.updateSession({ name: res.name });
        this.isEditMode = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.profileError = this.extractErrorMessage(err);
        this.cdr.detectChanges();
      }
    });
  }

  private triggerAlert(type: 'success' | 'error', message: string): void {
    this.alertMessage = message;
    this.alertType = type;
    this.cdr.detectChanges();
    setTimeout(() => {
      this.alertMessage = '';
      this.alertType = '';
      this.cdr.detectChanges();
    }, 5000);
  }

  private extractErrorMessage(err: any): string {
    if (!err) return 'An unexpected system validation error occurred.';
    if (err.error && typeof err.error === 'string') {
      try {
        const parsed = JSON.parse(err.error);
        return parsed.message || parsed.error || err.error;
      } catch (e) { return err.error; }
    }
    if (err.error && typeof err.error === 'object') {
      return err.error.message || err.error.error || JSON.stringify(err.error);
    }
    return err.message || 'An upstream communication connection exception occurred.';
  }
}