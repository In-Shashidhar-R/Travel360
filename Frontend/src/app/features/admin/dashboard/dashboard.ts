import { Component, OnInit, ChangeDetectorRef } from '@angular/core'; 
import { CommonModule } from '@angular/common'; 
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AdminService } from '../services/admin.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [ 
    CommonModule, 
    RouterOutlet, 
    RouterLink, 
    RouterLinkActive,
    FormsModule
  ], 
  templateUrl: './dashboard.html', 
  styleUrl: './dashboard.scss' 
}) 
export class Dashboard implements OnInit { 
  alertMessage: string = ''; 
  alertType: 'success' | 'error' | '' = '';
  passwordError: string = '';

  passwordForm = { currentPassword: '', newPassword: '' };

  constructor(
    public authService: AuthService, 
    private adminService: AdminService,
    private cdr: ChangeDetectorRef
  ) {} 

  ngOnInit(): void {} 

  handleAlert(event: string | { type: 'success' | 'error'; message: string }): void { 
    if (typeof event === 'string') {
      this.alertMessage = event;
      this.alertType = 'success';
    } else {
      this.alertMessage = event.message;
      this.alertType = event.type;
    }
    this.cdr.detectChanges();
    
    setTimeout(() => {
      this.alertMessage = ''; 
      this.alertType = '';
      this.cdr.detectChanges();
    }, 5000); 
  } 

  submitPasswordRotation(): void {
    this.passwordError = '';

    if (this.passwordForm.currentPassword === this.passwordForm.newPassword) {
      this.passwordError = 'The new security password cannot be identical to your existing authentication credentials.';
      this.cdr.detectChanges(); 
      return;
    }

    this.adminService.changePassword(this.passwordForm).subscribe({
      next: (msg: string) => {
        this.handleAlert({ type: 'success', message: msg || 'Password Changed Successfully' });
        this.passwordForm = { currentPassword: '', newPassword: '' };
        document.getElementById('closeAdminPasswordBtn')?.click();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.passwordError = this.extractErrorMessage(err);
        this.cdr.detectChanges();
      }
    });
  }

  handleSignOut(): void {
    this.authService.logout();
  }

  private extractErrorMessage(err: any): string {
    if (!err) return 'An unexpected system validation error occurred.';
    
    if (err.error && typeof err.error === 'string') {
      try {
        const parsed = JSON.parse(err.error);
        return parsed.message || parsed.error || err.error;
      } catch (e) {
        return err.error;
      }
    }
    
    if (err.error && typeof err.error === 'object') {
      return err.error.message || err.error.error || JSON.stringify(err.error);
    }
    
    return err.message || 'Server Error Occured';
  }
}