import { Component, OnInit, ChangeDetectorRef, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { forkJoin } from 'rxjs';
import { UserDirectoryDTO } from '../../../../shared/models/user.model';
import DOMPurify from 'dompurify';

@Component({
  selector: 'app-user-audit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-audit.html',
  styleUrls: ['./user-audit.scss'],
  encapsulation: ViewEncapsulation.None
})
export class UserAudit implements OnInit {
  mockUsers: UserDirectoryDTO[] = [];
  
  activeEditTarget: any = null;
  editForm: any = {};
  
  directoryError: string = '';

  userSearchId: string = '';  
  userSearchRole: string = '';

  currentPage: number = 1;
  pageSize: number = 6; 

  maxDobDate: string = (() => {
    const d = new Date();
    d.setFullYear(d.getFullYear() - 18);
    return d.toISOString().split('T')[0];
  })();

  constructor(private adminService: AdminService, public cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadSystemUsers();
  }

  private extractContentArray(response: any): any[] {
    if (!response) return [];
    if (Array.isArray(response)) return response;
    if (response.content && Array.isArray(response.content)) return response.content;
    if (response.data) {
      if (Array.isArray(response.data)) return response.data;
      if (response.data.content && Array.isArray(response.data.content)) return response.data.content;
    }
    return [];
  }

  computeAge(dobString: string | undefined | null): number | null {
    if (!dobString) return null;
    const birthDate = new Date(dobString);
    const today = new Date();
    
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDifference = today.getMonth() - birthDate.getMonth();
    
    if (monthDifference < 0 || (monthDifference === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    return age;
  }

  loadSystemUsers(): void {
    forkJoin({
      users: this.adminService.getAllUsers(),
      partners: this.adminService.getAllPartners()
    }).subscribe({
      next: ({ users, partners }) => {
        const userArray = this.extractContentArray(users);
        const partnerArray = this.extractContentArray(partners);

        const userData = userArray
          .filter((u: any) => u.role !== 'PARTNER') 
          .map((u: any) => ({
            userId: u.userId, name: u.name, email: u.email, phone: u.phone, role: u.role,
            gender: u.gender, address: u.address, city: u.city, state: u.state, country: u.country,
            dateOfBirth: u.dateOfBirth,
            age: this.computeAge(u.dateOfBirth),
            agentBio: u.agentBio, agentExperienceYears: u.agentExperienceYears
          }));

        const partnerData = partnerArray.map((p: any) => ({
          userId: p.partnerId, name: p.name, email: p.email, phone: p.contactNumber, role: 'PARTNER',
          gender: p.gender, address: p.address, city: p.city, state: p.state, country: p.country,
          dateOfBirth: p.dateOfBirth,
          age: this.computeAge(p.dateOfBirth),
          partnerType: p.type, gstNumber: p.gstNumber, commissionRate: p.commissionRate
        }));

        this.mockUsers = [...userData, ...partnerData];
        this.resetPagination();
      },
      error: (err) => console.error('💥 User Directory ForkJoin Exception:', err)
    });
  }

  initializeEditWorkflow(user: any): void {
    this.directoryError = ''; 
    this.activeEditTarget = user;
    this.editForm = { ...user };
    
    this.cdr.detectChanges(); 
  }

  // Restricts phone input to digits only
  onPhoneInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    input.value = input.value.replace(/[^0-9]/g, '');
    this.editForm.phone = input.value;
  }

  processDirectoryModification(): void {
    if (!this.activeEditTarget) return;
    this.directoryError = '';

    // 1. Name Validation
    const nameRegex = /^[a-zA-Z\s\-'\.,&]+$/;
    if (!this.editForm.name || !nameRegex.test(this.editForm.name.trim())) {
      this.directoryError = 'Validation Failure: Legal name is required and cannot contain numbers or special characters (<, >, ?, !).';
      this.cdr.detectChanges();
      return;
    }

    // 2. Phone Validation (Exactly 10 digits)
    const phoneRegex = /^[0-9]{10}$/;
    if (!this.editForm.phone || !phoneRegex.test(this.editForm.phone.trim())) {
      this.directoryError = 'Validation Failure: Contact Phone must be exactly 10 digits.';
      this.cdr.detectChanges();
      return;
    }

    // 3. Gender Validation
    if (!this.editForm.gender) {
      this.directoryError = 'Validation Failure: Gender selection is required.';
      this.cdr.detectChanges();
      return;
    }

    // 4. Date of Birth & Age Gate (18+)
    if (!this.editForm.dateOfBirth) {
      this.directoryError = 'Validation Failure: A valid Date of Birth is required.';
      this.cdr.detectChanges();
      return;
    }

    const targetAge = this.computeAge(this.editForm.dateOfBirth);
    if (targetAge === null || targetAge < 18) {
      this.directoryError = 'Validation Failure: Modified profile holder must evaluate to at least 18 years old.';
      this.cdr.detectChanges();
      return;
    }

    // 5. HTML Injection Prevention on Location & Bio Strings
    const noHtmlRegex = /^[^<>]*$/;
    const stringFields = ['address', 'city', 'state', 'country', 'agentBio'];
    for (const f of stringFields) {
      if (this.editForm[f] && !noHtmlRegex.test(this.editForm[f])) {
        this.directoryError = `Validation Failure: HTML brackets (<, >) are explicitly blocked in field (${f}).`;
        this.cdr.detectChanges();
        return;
      }
    }

    const targetId = this.activeEditTarget.userId;

    // Role Specific Payload Dispatch
    if (this.activeEditTarget.role === 'TRAVEL_AGENT') {
      if (this.editForm.agentExperienceYears != null && this.editForm.agentExperienceYears < 0) {
        this.directoryError = 'Validation Failure: Experience years cannot be negative.';
        this.cdr.detectChanges();
        return;
      }

      const agentPayload = {
        name: DOMPurify.sanitize(this.editForm.name.trim()),
        phone: this.editForm.phone.trim(),
        address: DOMPurify.sanitize(this.editForm.address?.trim() || ''),
        city: DOMPurify.sanitize(this.editForm.city?.trim() || ''),
        state: DOMPurify.sanitize(this.editForm.state?.trim() || ''),
        country: DOMPurify.sanitize(this.editForm.country?.trim() || ''),
        gender: this.editForm.gender,
        dateOfBirth: this.editForm.dateOfBirth,
        agentBio: DOMPurify.sanitize(this.editForm.agentBio?.trim() || ''),
        agentExperienceYears: Number(this.editForm.agentExperienceYears || 0)
      };

      this.adminService.updateUserProfile(targetId, agentPayload).subscribe({
        next: () => this.handleWorkflowSuccess(),
        error: (err) => {
          this.directoryError = err.error?.message || 'Failed to update Travel Agent profile.';
          this.cdr.detectChanges();
        }
      });
    } else if (this.activeEditTarget.role === 'PARTNER') {
      if (!this.editForm.partnerType) {
        this.directoryError = 'Validation Failure: Merchant category type selection is mandatory.';
        this.cdr.detectChanges();
        return;
      }

      const gstRegex = /^[0-9A-Z]{10,20}$/;
      if (!this.editForm.gstNumber || !gstRegex.test(this.editForm.gstNumber.trim())) {
        this.directoryError = 'Validation Failure: GSTIN must be 10-20 uppercase alphanumeric characters.';
        this.cdr.detectChanges();
        return;
      }

      if (this.editForm.commissionRate == null || this.editForm.commissionRate < 0 || this.editForm.commissionRate > 100) {
        this.directoryError = 'Validation Failure: Commission rate must be between 0% and 100%.';
        this.cdr.detectChanges();
        return;
      }

      const partnerPayload = {
        name: DOMPurify.sanitize(this.editForm.name.trim()),
        type: this.editForm.partnerType,
        contactNumber: this.editForm.phone.trim(),
        address: DOMPurify.sanitize(this.editForm.address?.trim() || ''),
        city: DOMPurify.sanitize(this.editForm.city?.trim() || ''),
        state: DOMPurify.sanitize(this.editForm.state?.trim() || ''),
        country: DOMPurify.sanitize(this.editForm.country?.trim() || ''),
        gender: this.editForm.gender,
        dateOfBirth: this.editForm.dateOfBirth,
        gstNumber: this.editForm.gstNumber.trim(),
        commissionRate: Number(this.editForm.commissionRate || 0)
      };

      this.adminService.updatePartnerProfile(targetId, partnerPayload).subscribe({
        next: () => this.handleWorkflowSuccess(),
        error: (err) => {
          this.directoryError = err.error?.message || 'Failed to update Business Partner profile.';
          this.cdr.detectChanges();
        }
      });
    }
  }

  private handleWorkflowSuccess(): void {
    document.getElementById('closeDirectoryEditBtn')?.click();
    this.activeEditTarget = null;
    this.loadSystemUsers();
    this.cdr.detectChanges();
  }

  onFilterChange(): void {
    this.resetPagination();
  }

  resetPagination(): void {
    this.currentPage = 1;
    this.cdr.detectChanges();
  }

  get filteredUsers(): UserDirectoryDTO[] {
    if (!Array.isArray(this.mockUsers)) return [];
    return this.mockUsers.filter(u => {
      const matchId = this.userSearchId ? u.userId.toString() === this.userSearchId : true;
      const matchRole = this.userSearchRole ? u.role === this.userSearchRole : true;
      return matchId && matchRole;
    });
  }

  get paginatedUsers(): UserDirectoryDTO[] {
    const sortedFiltered = this.filteredUsers;
    const startIndex = (this.currentPage - 1) * this.pageSize;
    return sortedFiltered.slice(startIndex, startIndex + this.pageSize);
  }

  getStartIndex(): number {
    if (this.filteredUsers.length === 0) return 0;
    return (this.currentPage - 1) * this.pageSize + 1;
  }

  getEndIndex(totalLength: number): number {
    const end = this.currentPage * this.pageSize;
    return end > totalLength ? totalLength : end;
  }

  getTotalPages(totalLength: number): number {
    return Math.ceil(totalLength / this.pageSize) || 1;
  }

  getPagesArray(totalLength: number): number[] {
    const pages = this.getTotalPages(totalLength);
    return Array(pages).fill(0);
  }

  goToPage(page: number): void {
    const maxPage = this.getTotalPages(this.filteredUsers.length);
    if (page >= 1 && page <= maxPage) {
      this.currentPage = page;
      this.cdr.detectChanges();
    }
  }
}