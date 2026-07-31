import { Component, afterNextRender, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})

export class Login {
  loginForm: FormGroup;
  errorMessage = signal<string>('');
  successMessage = signal<string>('');

  constructor(private fb: FormBuilder, private router: Router, private authService: AuthService) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    afterNextRender(() => {
      localStorage.clear();
    });
  }

  onLogin(): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value).subscribe({
        next: (response) => {
          this.successMessage.set(`Verification Confirmed! Welcome back, ${response.name || 'Explorer'}.`);

          setTimeout(() => {
            const targetRoute =
              response.role === 'ADMIN'
                ? '/admin-dashboard'
                : response.role === 'TRAVEL_AGENT'
                  ? '/agent-dashboard'
                  : response.role === 'COMPLIANCE_OFFICER'
                    ? '/compliance/audit-logs'
                    : response.role === 'FINANCE_OFFICER'
                      ? '/finance-dashboard'
                      : response.role === 'PARTNER'
                        ? '/partner-dashboard'
                        : response.role === 'CUSTOMER'
                          ? '/cust-dashboard'
                          : '/';

            this.router.navigate([targetRoute]);
          }, 1500);
        },
        error: (err) => {
          console.error('Login request failed:', err);
          if (err.status === 0) {
            this.errorMessage.set('Failed to fetch: Backend server is unreachable. Please verify your server status.');
          } else if (err.error && typeof err.error === 'object') {
            this.errorMessage.set(err.error.message || err.error.error || 'Access Denied.');
          } else {
            this.errorMessage.set(err.error || 'Access Denied. Invalid bad credentials parameters.');
          }
        }
      });
    } else {
      this.errorMessage.set('Please fill out the required credentials properly.');
      this.loginForm.markAllAsTouched();
    }
  }
}