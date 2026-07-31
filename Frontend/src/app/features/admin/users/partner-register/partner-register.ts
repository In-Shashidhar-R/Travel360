import { Component, Output, EventEmitter, ChangeDetectorRef, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { FormUtilsService } from '../../services/form.util.service'; 
import { LOCATION_DATA, CountryConfig, StateConfig, DistrictConfig } from '../../../../shared/models/location.model';
import { PartnerRequestDTO } from '../../../../shared/models/partner.model'; 
import DOMPurify from 'dompurify';

export function adultAgeValidator(minAge: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    
    const dob = new Date(control.value);
    const today = new Date();
    
    let age = today.getFullYear() - dob.getFullYear();
    const monthDiff = today.getMonth() - dob.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < dob.getDate())) {
      age--;
    }
    
    return age >= minAge ? null : { underaged: true };
  };
}

@Component({
  selector: 'app-partner-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './partner-register.html',
  styleUrl: '../../dashboard/dashboard.scss'
})
export class PartnerRegister {
  successMessage = signal<string>('');
  @Output() alertTrigger = new EventEmitter<string>();
  
  partnerForm: FormGroup;
  countries: CountryConfig[] = LOCATION_DATA;
  partnerStates: StateConfig[] = [];
  partnerDistricts: DistrictConfig[] = [];
  partnerCities: string[] = [];
  todayDate: string = new Date().toISOString().split('T')[0];

  constructor(
    private fb: FormBuilder, 
    private adminService: AdminService, 
    public cdr: ChangeDetectorRef,
    public formUtils: FormUtilsService
  ) {
    this.partnerForm = this.fb.group({
      name: ['', [
        Validators.required, 
        Validators.pattern(/^[a-zA-Z\s\-'\.,&]+$/)
      ]],
      type: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      contactNumber:['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      password: ['', [Validators.required, Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{5,}$/)]],
      address: ['', [
        Validators.required, 
        Validators.pattern(/^[^<>]*$/)
      ]], 
      country: ['', Validators.required],
      state: [{ value: '', disabled: true }, Validators.required],
      district: [{ value: '', disabled: true }, Validators.required], 
      city: [{ value: '', disabled: true }, Validators.required],
      gender: ['', Validators.required], 
      dateOfBirth: ['', [Validators.required, adultAgeValidator(18)]],
      gstNumber: ['', [Validators.required, Validators.pattern(/^[0-9A-Z]{10,20}$/)]], 
      commissionRate: [0.0, [Validators.required, Validators.min(0), Validators.max(100)]]
    });
  }

  maxDobDate: string = (() => {
  const d = new Date();
  d.setFullYear(d.getFullYear() - 18);
  return d.toISOString().split('T')[0];
  })();

  onSelectChange(field: 'country' | 'state' | 'district', event: Event): void {
    const res = this.formUtils.cascadeLocations(field, event, this.partnerForm, this.countries, this.partnerStates, this.partnerDistricts, this.cdr);
    this.partnerStates = res.states;
    this.partnerDistricts = res.districts;
    this.partnerCities = res.cities;

    setTimeout(() => {
      this.cdr.detectChanges();
    });
  }

  submitPartnerForm(): void {
    if (this.partnerForm.valid) {
      const { district, ...formValue } = this.partnerForm.getRawValue(); 
      
      const sanitizedPayload: PartnerRequestDTO = {
        ...formValue,
        name: DOMPurify.sanitize(formValue.name?.trim() || ''),
        email: formValue.email?.trim() || '',
        contactNumber: formValue.contactNumber?.trim() || '',
        address: DOMPurify.sanitize(formValue.address?.trim() || '')
      } as PartnerRequestDTO;
      
      this.adminService.registerPartner(sanitizedPayload).subscribe({
        next: () => {
          this.successMessage.set('Partner registered successfully');
          this.partnerForm.reset({ commissionRate: 0.0 });
          ['state', 'district', 'city'].forEach(f => this.partnerForm.get(f)?.disable());
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.alertTrigger.emit(`Registration Failed: ${err.error?.message || err.message}`);
        }
      });
    } else {
      this.partnerForm.markAllAsTouched();
    }
  }
}