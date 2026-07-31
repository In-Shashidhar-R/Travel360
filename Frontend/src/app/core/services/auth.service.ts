import { Injectable, signal, computed, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthRequestDTO, AuthResponseDTO } from '../../shared/models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:9095/api/v1/users';
  
  private authState = signal<AuthResponseDTO | null>(null);

  currentUser = computed(() => this.authState());
  isAuthenticated = computed(() => !!this.authState()?.token);
  currentUserRole = computed(() => this.authState()?.role || null);

  constructor(
    private http: HttpClient, 
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object 
  ) {
    this.authState.set(this.loadStoredSession());
  }

  login(credentials: AuthRequestDTO): Observable<AuthResponseDTO> {
    return this.http.post<AuthResponseDTO>(`${this.API_URL}/login`, credentials).pipe(
      tap(response => this.establishSession(response))
    );
  }

  signUp(registrationPayload: any): Observable<any> {
    return this.http.post(`${this.API_URL}/register`, registrationPayload);
  }

  resetPassword(resetPayload: { email: string; newPassword: string }): Observable<string> {
    return this.http.post(`${this.API_URL}/reset-password`, resetPayload, { responseType: 'text' });
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('travel360_session');
    }
    this.authState.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.authState()?.token || null;
  }

  getCurrentUserId(): number | null {
    return this.authState()?.userId || null; 
  }

  updateSession(updatedData: Partial<AuthResponseDTO>): void {
    const currentSession = this.authState();
    if (currentSession) {
      const mergedSession = { ...currentSession, ...updatedData };
      this.authState.set(mergedSession);
      if (isPlatformBrowser(this.platformId)) {
        localStorage.setItem('travel360_session', JSON.stringify(mergedSession));
      }
    }
  }

  private establishSession(session: AuthResponseDTO): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('travel360_session', JSON.stringify(session));
    }
    this.authState.set(session);
  }

  private loadStoredSession(): AuthResponseDTO | null {
    if (isPlatformBrowser(this.platformId)) {
      const data = localStorage.getItem('travel360_session');
      return data ? JSON.parse(data) : null;
    }
    return null;
  }
}