import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { Login } from './login';
import { AuthService } from '../../core/services/auth.service';
import { AuthResponseDTO } from '../../shared/models/auth.model';

describe('Login Component', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    let store: Record<string, string> = {};
    const localStorageMock = {
      getItem: (key: string) => store[key] || null,
      setItem: (key: string, value: string) => { store[key] = value; },
      removeItem: (key: string) => { delete store[key]; },
      clear: () => { store = {}; },
    };

    Object.defineProperty(window, 'localStorage', {
      value: localStorageMock,
      writable: true,
    });

    authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);

    spyOn(router, 'navigate');
    fixture.detectChanges();
  });

  beforeEach(() => {
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  it('should create the login component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with an invalid form', () => {
    expect(component.loginForm.valid).toBeFalse();
  });

  it('should validate form inputs correctly', () => {
    const email = component.loginForm.get('email');
    const password = component.loginForm.get('password');

    email?.setValue('invalid-email');
    expect(email?.hasError('email')).toBeTrue();

    email?.setValue('user@travel360.com');
    password?.setValue('password123');
    expect(component.loginForm.valid).toBeTrue();
  });

  it('should show an error message if submitted with invalid form', () => {
    component.onLogin();
    expect(component.errorMessage()).toBe('Please fill out the required credentials properly.');
    expect(authServiceSpy.login).not.toHaveBeenCalled();
  });

  it('should process login for ADMIN and navigate after timeout', () => {
    const mockResponse: AuthResponseDTO = {
      userId: 1,
      token: 'jwt-token-123',
      email: 'admin@travel360.com',
      name: 'System Admin',
      role: 'ADMIN'
    };

    authServiceSpy.login.and.returnValue(of(mockResponse));

    component.loginForm.setValue({
      email: 'admin@travel360.com',
      password: 'password123'
    });

    component.onLogin();

    expect(component.successMessage()).toContain('Welcome back, System Admin.');

    jasmine.clock().tick(1500);

    expect(router.navigate).toHaveBeenCalledWith(['/admin-dashboard']);
  });

  it('should process login for CUSTOMER and navigate to /cust-dashboard', () => {
    const mockResponse: AuthResponseDTO = {
      userId: 2,
      token: 'jwt-token-456',
      email: 'customer@travel360.com',
      name: 'Traveler',
      role: 'CUSTOMER'
    };

    authServiceSpy.login.and.returnValue(of(mockResponse));

    component.loginForm.setValue({
      email: 'customer@travel360.com',
      password: 'password123'
    });

    component.onLogin();
    jasmine.clock().tick(1500);

    expect(router.navigate).toHaveBeenCalledWith(['/cust-dashboard']);
  });

  it('should handle backend unreachable error (HTTP 0)', () => {
    authServiceSpy.login.and.returnValue(throwError(() => ({ status: 0 })));

    component.loginForm.setValue({
      email: 'user@travel360.com',
      password: 'password123'
    });

    component.onLogin();

    expect(component.errorMessage()).toBe('Failed to fetch: Backend server is unreachable. Please verify your server status.');
  });

  it('should display error message returned from backend', () => {
    authServiceSpy.login.and.returnValue(throwError(() => ({
      status: 401,
      error: { message: 'Invalid email or password.' }
    })));

    component.loginForm.setValue({
      email: 'user@travel360.com',
      password: 'password123'
    });

    component.onLogin();

    expect(component.errorMessage()).toBe('Invalid email or password.');
  });
});