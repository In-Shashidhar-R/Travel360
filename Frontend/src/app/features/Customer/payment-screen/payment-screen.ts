import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BillingService, InvoiceResponseDTO, PaymentRequestDTO } from '../../../core/services/billing-service';
import { TravelService } from '../../../core/services/travel-service';
import { AuthService } from '../../../core/services/auth.service';

/** Custom Validator: Ensures expiry date is formatted MM/YY and is NOT in the past */
export function futureExpiryValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  const regex = /^(0[1-9]|1[0-2])\/([0-9]{2})$/;
  if (!regex.test(control.value)) {
    return { invalidFormat: true };
  }

  const [monthStr, yearStr] = control.value.split('/');
  const month = parseInt(monthStr, 10);
  const year = parseInt('20' + yearStr, 10);

  const now = new Date();
  const currentMonth = now.getMonth() + 1;
  const currentYear = now.getFullYear();

  if (year < currentYear || (year === currentYear && month < currentMonth)) {
    return { expired: true };
  }
  return null;
}

export type PaymentChannel = 'CREDIT_CARD' | 'DEBIT_CARD' | 'NET_BANKING' | 'UPI' | 'WALLET';

@Component({
  selector: 'app-payment-screen',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule],
  templateUrl: './payment-screen.html',
  styleUrl: './payment-screen.scss'
})
export class PaymentScreen implements OnInit {
  bookingDetails: any | null = null;
  invoice: InvoiceResponseDTO | null = null;
  
  selectedMethod: PaymentChannel = 'CREDIT_CARD'; 
  isLoading = true;
  isProcessingPayment = false;
  errorMessage = '';
  successMessage = '';
  transactionId = '';

  paymentForm!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private billingService: BillingService,
    private travelService: TravelService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    public authService: AuthService
  ) {
    this.initPaymentForm();
  }

  ngOnInit(): void {
    const bookingId = this.route.snapshot.paramMap.get('bookingId');
    if (bookingId) {
      this.loadPaymentContextPipeline(Number(bookingId));
    } else {
      this.errorMessage = 'Missing tracking reference metadata linkage context.';
      this.isLoading = false;
    }
  }

  private initPaymentForm(): void {
    this.paymentForm = this.fb.group({
      // Card Controls
      cardNumber: [''],
      cardHolder: [''],
      expiry: [''],
      cvv: [''],
      
      // NetBanking Controls
      bankName: [''],
      bankAccountNumber: [''],

      // UPI Controls
      upiId: [''],

      // Wallet Controls
      walletProvider: ['PAYTM'],
      walletPhone: ['']
    });

    this.updateFormValidationRules();
  }

  selectPaymentMethod(method: PaymentChannel): void {
    this.selectedMethod = method;
    this.updateFormValidationRules();
    this.cdr.markForCheck();
  }

  private updateFormValidationRules(): void {
    const allControls = ['cardNumber', 'cardHolder', 'expiry', 'cvv', 'bankName', 'bankAccountNumber', 'upiId', 'walletPhone'];

    // Step 1: Clear validators AND force status update on EVERY individual control
    allControls.forEach(cName => {
      const ctrl = this.paymentForm.get(cName);
      if (ctrl) {
        ctrl.clearValidators();
        ctrl.setErrors(null);
        ctrl.updateValueAndValidity({ emitEvent: false });
      }
    });

    // Step 2: Apply specific validators based on active method
    if (this.selectedMethod === 'CREDIT_CARD' || this.selectedMethod === 'DEBIT_CARD') {
      this.paymentForm.get('cardNumber')?.setValidators([Validators.required, Validators.pattern('^[0-9]{12,16}$')]);
      this.paymentForm.get('cardHolder')?.setValidators([Validators.required, Validators.minLength(3)]);
      this.paymentForm.get('expiry')?.setValidators([Validators.required, futureExpiryValidator]);
      this.paymentForm.get('cvv')?.setValidators([Validators.required, Validators.pattern('^[0-9]{3}$')]);
    } else if (this.selectedMethod === 'NET_BANKING') {
      this.paymentForm.get('bankName')?.setValidators([Validators.required]);
      this.paymentForm.get('bankAccountNumber')?.setValidators([Validators.required, Validators.pattern('^[0-9]{9,18}$')]);
    } else if (this.selectedMethod === 'UPI') {
      this.paymentForm.get('upiId')?.setValidators([Validators.required, Validators.pattern('^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$')]);
    } else if (this.selectedMethod === 'WALLET') {
      this.paymentForm.get('walletPhone')?.setValidators([Validators.required, Validators.pattern('^[0-9]{10}$')]);
    }

    // Step 3: Re-evaluate validity across all fields & parent form group
    allControls.forEach(cName => {
      this.paymentForm.get(cName)?.updateValueAndValidity({ emitEvent: false });
    });
    this.paymentForm.updateValueAndValidity({ emitEvent: false });
  }

  isInvalid(controlName: string): boolean {
    const control = this.paymentForm.get(controlName);
    return !!(control && control.touched && control.invalid);
  }

  hasError(controlName: string, errorName: string): boolean {
    const control = this.paymentForm.get(controlName);
    return !!(control && control.touched && control.hasError(errorName));
  }

  get formattedCardNumber(): string {
    const rawValue = this.paymentForm.get('cardNumber')?.value || '';
    const digitsOnly = rawValue.replace(/\D/g, '');
    if (!digitsOnly) return '•••• •••• •••• ••••';
    const groups = digitsOnly.match(/.{1,4}/g);
    return groups ? groups.join(' ') : '•••• •••• •••• ••••';
  }

  get currentUserId(): number {
    return this.authService.currentUser()?.userId || 0;
  }

  get dynamicQrUrl(): string {
    const amount = this.invoice?.amount || 0;
    const ref = this.invoice?.invoiceId || '0000';
    return `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=upi://pay?pa=merchant@bank%26pn=TravelPortal%26am=${amount}%26tr=${ref}`;
  }

  loadPaymentContextPipeline(bookingId: number): void {
    const userId = this.currentUserId;
    if (!userId) {
      this.errorMessage = 'Your session has expired. Please log in again.';
      this.isLoading = false;
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.cdr.detectChanges();

    this.travelService.getBookingById(bookingId).subscribe({
      next: (bookingData: any) => {
        this.bookingDetails = bookingData;

        this.travelService.getCustomerInvoices(userId, 0, 100).subscribe({
          next: (invoicePage: any) => {
            const allInvoices: InvoiceResponseDTO[] = invoicePage?.content || [];

            const targetInvoice = allInvoices.find(inv => 
              inv.bookingId === bookingId && 
              (inv.status === 'UNPAID' || inv.status === 'PENDING')
            );

            if (!targetInvoice) {
              this.errorMessage = `Could not locate an active UNPAID or PENDING invoice record for Booking #${bookingId}.`;
              this.isLoading = false;
              this.cdr.detectChanges();
              return;
            }

            this.invoice = targetInvoice;
            this.isLoading = false;
            this.cdr.detectChanges();
          },
          error: () => {
            this.errorMessage = 'Failed to download customer billing history profiles.';
            this.isLoading = false;
            this.cdr.detectChanges();
          }
        });
      },
      error: () => {
        this.errorMessage = 'Unable to locate parent booking registration profile records.';
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  submitPaymentClearance(): void {
    if (!this.invoice) {
      this.errorMessage = 'Cannot submit payment. Invoice parameters are missing.';
      return;
    }

    if (this.paymentForm.invalid) {
      this.paymentForm.markAllAsTouched();
      return;
    }

    const request: PaymentRequestDTO = {
      invoiceId: this.invoice.invoiceId,
      method: this.selectedMethod
    };

    this.isProcessingPayment = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.cdr.detectChanges();

    this.billingService.executePayment(request).subscribe({
      next: (msg: any) => {
        this.errorMessage = '';
        this.successMessage = 'Payment Cleared Successfully!';
        this.transactionId = 'TXN-' + Math.floor(100000 + Math.random() * 900000);
        
        this.billingService.triggerNotificationRefresh();
        this.cdr.detectChanges(); 
        
        setTimeout(() => {
          this.router.navigate(['/customer/bookings']); 
        }, 2200);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || err.error || 'Transaction declined. Verify account balances.';
        this.isProcessingPayment = false;
        this.cdr.detectChanges();
      }
    });
  }
}