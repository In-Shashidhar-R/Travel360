import { Component, ChangeDetectorRef, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { FormUtilsService } from '../../features/admin/services/form.util.service';
import { LOCATION_DATA, CountryConfig, StateConfig, DistrictConfig } from '../../shared/models/location.model';
import DOMPurify from 'dompurify';

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './sign-up.html',
  styleUrl: './sign-up.scss'
})
export class SignUp {
  signUpForm: FormGroup;

  successMessage = signal<string>('');
  errorMessage = signal<string>('');

  countries: CountryConfig[] = LOCATION_DATA;
  states: StateConfig[] = [];
  districts: DistrictConfig[] = [];
  cities: string[] = [];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    public cdr: ChangeDetectorRef,
    public formUtils: FormUtilsService
  ) {
    this.signUpForm = this.fb.group({
      name: ['', [
        Validators.required, Validators.pattern(/^[a-zA-Z\s\-'\.]+$/)
      ]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(4)]],
      phone: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      address: ['', [
        Validators.required, Validators.pattern(/^[^<>]*$/)
      ]],
      country: ['', Validators.required],
      state: [{ value: '', disabled: true }, Validators.required],
      district: [{ value: '', disabled: true }, Validators.required],
      city: [{ value: '', disabled: true }, Validators.required],
      dateOfBirth: ['', [Validators.required, this.ageValidator]],
      gender: ['', Validators.required]
    });
  }

  maxDobDate: string = (() => {
    const d = new Date();
    d.setFullYear(d.getFullYear() - 18);
    return d.toISOString().split('T')[0];
  })();

  onSelectChange(field: 'country' | 'state' | 'district', event: Event): void {
    const res = this.formUtils.cascadeLocations(field, event, this.signUpForm, this.countries, this.states, this.districts, this.cdr);
    this.states = res.states;
    this.districts = res.districts;
    this.cities = res.cities;
  }

  ageValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    const dob = new Date(control.value);
    const today = new Date();
    let age = today.getFullYear() - dob.getFullYear();
    const m = today.getMonth() - dob.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < dob.getDate())) {
      age--;
    }
    return age >= 18 ? null : { underAge: true };
  }

  onSubmit() {
    this.successMessage.set('');
    this.errorMessage.set('');

    if (this.signUpForm.valid) {
      const formRawValues = this.signUpForm.value;

      const sanitizedPayload = {
        name: DOMPurify.sanitize(formRawValues.name?.trim() || ''),
        email: formRawValues.email?.trim() || '',
        password: formRawValues.password, 
        phone: formRawValues.phone?.trim() || '',
        address: DOMPurify.sanitize(formRawValues.address?.trim() || ''),
        country: formRawValues.country,
        state: formRawValues.state,
        city: formRawValues.city,
        dateOfBirth: formRawValues.dateOfBirth,
        gender: formRawValues.gender
      };

      this.authService.signUp(sanitizedPayload).subscribe({
        next: (response) => {
          this.successMessage.set('Registration successful! Redirecting to login...');
          setTimeout(() => this.router.navigate(['/login']), 2000);
        },
        error: (err) => {
          this.errorMessage.set(err.error?.message || 'Registration failed. Please check your inputs.');
        }
      });
    } else {
      this.signUpForm.markAllAsTouched();
    }
  }
}