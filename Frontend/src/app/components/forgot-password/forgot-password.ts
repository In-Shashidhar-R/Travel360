import { Component, signal } from '@angular/core'; // 👈 Import signal here
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.scss'
})
export class ForgotPassword {
  forgotForm: FormGroup;

  infoMessage = signal<string>('');
  errorMessage = signal<string>('');

  constructor(private fb: FormBuilder, private router: Router, private authService: AuthService) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      newPassword: ['', [
        Validators.required,
        Validators.minLength(6),
        Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{6,}$/)
      ]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
    const password = group.get('newPassword')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return password === confirm ? null : { mismatch: true };
  }

  onResetSubmit() {
    this.infoMessage.set('');
    this.errorMessage.set('');

    if (this.forgotForm.valid) {
      const payload = {
        email: this.forgotForm.value.email,
        newPassword: this.forgotForm.value.newPassword
      };

      this.authService.resetPassword(payload).subscribe({
        next: (response) => {
          this.infoMessage.set('Password updated successfully! Redirecting to login view...');
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2500);
        },
        error: (err) => {
          console.error('Password reset failed:', err);

          if (err.status === 0) {
            this.errorMessage.set('Failed to fetch: Backend server is unreachable. Please check your connection.');
          } else if (err.error && typeof err.error === 'object') {
            this.errorMessage.set(err.error.message || err.error.error || 'Server error occurred.');
          } else {
            this.errorMessage.set(err.error || 'An error occurred while resetting your password.');
          }
        }
      });
    } else {
      this.errorMessage.set('Please fix the validation errors in the form before submitting.');
      this.forgotForm.markAllAsTouched();
    }
  }
}