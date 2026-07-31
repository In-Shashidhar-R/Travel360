import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BookingService } from '../../services/booking-service';

@Component({
  selector: 'app-agent-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './agent-profile.component.html',
  styleUrls: []
})
export class AgentProfileComponent implements OnInit {
 
  agentDetails: any = {
    name: "",
    email: "",
    phone: "",
    address: "",
    city: "",
    state: "",
    country: "",
    agentBio: "",
    agentExperienceYears: 0
  };

  isEditMode: boolean = false;
  isPasswordPanelOpen: boolean = false;
  isSaving: boolean = false;

  passwordModel = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  currentUserId: number | null = null;
  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    private router: Router,
    private bookingService: BookingService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
   
    const savedResult = localStorage.getItem("travel360_session") || sessionStorage.getItem("travel360_session");
    
    if (savedResult) {
      try {
        const sessionData = JSON.parse(savedResult);
        this.currentUserId = sessionData.userId || null;
        
        if (this.currentUserId) {
         
          this.fetchCompleteProfileFromServer(this.currentUserId);
        } else {
          this.errorMessage = "Could not resolve active session ID parameters.";
        }
      } catch (error) {
        console.error("Failed to parse initial session wrapper strings:", error);
      }
    }
  }

  fetchCompleteProfileFromServer(userId: number): void {
    this.bookingService.getProfileDetails(userId).subscribe({
      next: (dbUser: any) => {
        console.log('Successfully fetched complete live profile payload:', dbUser);
        
       
        this.agentDetails = {
          name: dbUser.name || '',
          email: dbUser.email || '',
          phone: dbUser.phone || dbUser.phoneNumber || '',
          address: dbUser.address || '',
          city: dbUser.city || '',
          state: dbUser.state || '',
          country: dbUser.country || '',
          agentBio: dbUser.agentBio || dbUser.bio || '',
          agentExperienceYears: dbUser.agentExperienceYears !== undefined ? dbUser.agentExperienceYears : 0
        };
        
        this.cdr.detectChanges(); 
      },
      error: (err: any) => {
        console.error('Failed to auto-fetch agent profile entries:', err);
        this.errorMessage = 'Could not load complete profile records from backend database.';
        this.cdr.detectChanges();
      }
    });
  }

  handleProfileButtonClick(): void {
    if (!this.isEditMode) {
      this.isEditMode = true;
      this.clearMessages();
      this.cdr.detectChanges();
    } else {
      this.executeProfileUpdate();
    }
  }

  private executeProfileUpdate(): void {
    if (!this.currentUserId) {
      this.errorMessage = 'Action aborted: Cannot resolve active User Session ID.';
      return;
    }
    
    this.isSaving = true;
    this.clearMessages();

    const updatePayload = {
      name: this.agentDetails.name,
      phone: this.agentDetails.phone,
      address: this.agentDetails.address,
      city: this.agentDetails.city,
      state: this.agentDetails.state,
      country: this.agentDetails.country,
      agentBio: this.agentDetails.agentBio,
      agentExperienceYears: Number(this.agentDetails.agentExperienceYears)
    };

    this.bookingService.updateProfile(this.currentUserId, updatePayload).subscribe({
      next: (response: any) => {
        this.isEditMode = false;
        this.isSaving = false;
        this.successMessage = 'Profile information successfully saved.';
        
        
        this.fetchCompleteProfileFromServer(this.currentUserId!); 
      },
      error: (err: any) => {
        console.error('Profile update failed:', err);
        this.isSaving = false;
        this.errorMessage = err.error?.message || 'Failed to update profile records.';
        this.cdr.detectChanges();
      }
    });
  }

  onUpdatePasswordSubmit(): void {
  this.clearMessages();
  if (this.passwordModel.newPassword !== this.passwordModel.confirmPassword) {
    this.errorMessage = "New Password and Confirm Password fields do not match.";
    return;
  }

  if (this.passwordModel.currentPassword === this.passwordModel.newPassword) {
    this.errorMessage = "Your new password cannot be the same as your current password.";
    return;
  }

  const payload = {
    currentPassword: this.passwordModel.currentPassword,
    newPassword: this.passwordModel.newPassword
  };

  this.bookingService.changePassword(payload).subscribe({
    next: () => {
      this.successMessage = 'Password updated successfully.';
      alert('Password changed. For account safety, please sign back in.');
      this.logout();
    },
    error: (err: any) => {
      console.error('Password rotation failed:', err);
      this.errorMessage = err.error?.message || 'Failed to rewrite password security variables.';
      this.cdr.detectChanges();
    }
  });
}


  togglePasswordPanel(): void {
    this.isPasswordPanelOpen = !this.isPasswordPanelOpen;
    this.passwordModel = { currentPassword: '', newPassword: '', confirmPassword: '' };
    this.clearMessages();
    this.cdr.detectChanges();
  }

  clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  logout(): void {
    localStorage.clear();
    sessionStorage.clear();
    this.router.navigate(['/login']);
  }
}