import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router'; 
import { catchError, throwError } from 'rxjs'; 

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router); 
  const token = authService.getToken();

  const isAuthRoute = req.url.includes('/login') ||
                      req.url.includes('/register') ||
                      req.url.includes('/reset-password') ||
                      req.url.includes('/api/auth/');
                      
  if (token && !isAuthRoute) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authService.logout(); 
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};