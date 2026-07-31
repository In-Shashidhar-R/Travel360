import { Component, Output, EventEmitter, ChangeDetectorRef, Signal, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../../../core/services/auth.service'; 
import { FormUtilsService } from '../../services/form.util.service'; 
import { LOCATION_DATA, CountryConfig, StateConfig, DistrictConfig } from '../../../../shared/models/location.model';
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

export interface AgentRequestDTO {
  name: string;
  email: string;
  password?: string;
  phone: string;
  address?: string;
  country: string;
  state: string;
  city: string;
  agentBio?: string;
  gender?: string;
  dateOfBirth?: string; 
  agentExperienceYears: number;
}

@Component({
  selector: 'app-agent-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './agent-register.html',
  styleUrl: '../../dashboard/dashboard.scss'
})
export class AgentRegister {
  successMessage = signal<string>('');
  @Output() alertTrigger = new EventEmitter<string>();

  agentForm: FormGroup;
  countries: CountryConfig[] = LOCATION_DATA;
  agentStates: StateConfig[] = [];
  agentDistricts: DistrictConfig[] = [];
  agentCities: string[] = [];
  submissionError: string | null = null; 

  constructor(
    private fb: FormBuilder, 
    private adminService: AdminService,
    private authService: AuthService,
    public cdr: ChangeDetectorRef, 
    public formUtils: FormUtilsService 
  ) {
    this.agentForm = this.fb.group({
      name: ['', [
        Validators.required, 
        Validators.pattern(/^[a-zA-Z\s\-'\.]+$/)
      ]], 
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required], 
      phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      address: ['', [Validators.pattern(/^[^<>]*$/)]], 
      country: ['', Validators.required], 
      state: [{ value: '', disabled: true }, Validators.required],
      district: [{ value: '', disabled: true }], 
      city: [{ value: '', disabled: true }, Validators.required],
      gender: ['', Validators.required], 
      dateOfBirth: ['', [Validators.required, adultAgeValidator(18)]],
      agentBio: ['', [Validators.pattern(/^[^<>]*$/)]], 
      agentExperienceYears: [0, [Validators.min(0)]]
    });
  }

  maxDobDate: string = (() => {
  const d = new Date();
  d.setFullYear(d.getFullYear() - 18);
  return d.toISOString().split('T')[0];
  })();

  onSelectChange(field: 'country' | 'state' | 'district', event: Event): void {
    const res = this.formUtils.cascadeLocations(field, event, this.agentForm, this.countries, this.agentStates, this.agentDistricts, this.cdr);
    this.agentStates = res.states;
    this.agentDistricts = res.districts;
    this.agentCities = res.cities;

    setTimeout(() => {
      this.cdr.detectChanges();
    });
  }

  submitAgentForm(): void {
    if (this.agentForm.valid) {
      this.submissionError = null;
      const rawFormValues = this.agentForm.value;
      const currentAdminId = this.authService.getCurrentUserId() || 1; 
      
      const sanitizedPayload: AgentRequestDTO = {
        name: DOMPurify.sanitize(rawFormValues.name?.trim() || ''),
        email: rawFormValues.email?.trim() || '',
        password: rawFormValues.password,
        phone: rawFormValues.phone?.trim() || '',
        address: DOMPurify.sanitize(rawFormValues.address?.trim() || ''),
        country: rawFormValues.country,
        state: rawFormValues.state,
        city: rawFormValues.city,
        gender: rawFormValues.gender,
        dateOfBirth: rawFormValues.dateFormBirth || rawFormValues.dateOfBirth,
        agentBio: DOMPurify.sanitize(rawFormValues.agentBio?.trim() || ''),
        agentExperienceYears: Number(rawFormValues.agentExperienceYears || 0)
      };
      
      this.adminService.registerAgent(sanitizedPayload, currentAdminId).subscribe({
        next: () => {
          this.successMessage.set('Travel Agent Register Successfully');
          this.agentForm.reset({ agentExperienceYears: 0, gender: '' });
          ['state', 'district', 'city'].forEach(f => this.agentForm.get(f)?.disable());
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.submissionError = err.message || 'Server Exception';
          this.alertTrigger.emit(`Agent Deployment Exception: ${this.submissionError}`);
        }
      });
    } else {
      this.agentForm.markAllAsTouched();
    }
  }
}