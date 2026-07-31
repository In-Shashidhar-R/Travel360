import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { UserDirectoryDTO, UserResponseDTO } from '../../../shared/models/user.model';
import { TravelService } from '../../../core/services/travel-service';
import { PassengerDirectory } from '../passenger-directory/passenger-directory';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, PassengerDirectory],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {
  user: UserDirectoryDTO = {
    userId: 0,
    name: '',
    email: '',
    role: '',
    phone: ''
  };

  totalBookings = 0;
  confirmedBookings = 0;
  cancelledBookings = 0;
  passengerCount = 0;
  
  allBookings: any[] = []; 

  constructor(
    private travelService: TravelService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    setTimeout(() => {
      this.initializeUserSession();
    }, 0);
  }

  private initializeUserSession(): void {
    const sessionData = localStorage.getItem('travel360_session');
    if (sessionData) {
      try {
        const parsedUser = JSON.parse(sessionData);
        if (parsedUser && parsedUser.userId) {
          this.travelService.getUserById(parsedUser.userId).subscribe({
            next: (fullProfile: UserResponseDTO) => {
              this.user = fullProfile;
              this.loadDashboardMetrics();
              this.cdr.detectChanges();
            },
            error: () => {
              this.user = parsedUser as UserResponseDTO;
              this.loadDashboardMetrics();
              this.cdr.detectChanges();
            }
          });
          return;
        }
      } catch (e) {
        console.error('Failed to parse user session parameters.', e);
      }
    }
    alert('User context invalid or session expired. Returning to credential login.');
    this.router.navigate(['/login']);
  }

  loadDashboardMetrics(): void {
    if (!this.user.userId) return;
    
    this.travelService.getCustomerBookingsPage(this.user.userId, 0, 50).subscribe({
      next: (pageResponse: any) => {
        const bookings = pageResponse?.content || pageResponse || [];
        
        this.allBookings = bookings;
        this.totalBookings = pageResponse?.totalElements || bookings.length;
        this.confirmedBookings = bookings.filter((b: any) => b.status === 'CONFIRMED' || b.status === 'PAID').length;
        this.cancelledBookings = bookings.filter((b: any) => b.status === 'CANCELLED').length;
        
        this.cdr.detectChanges(); 
      },
      error: (err) => {
        console.error('Failed to resolve dynamic customer booking ledgers.', err);
        this.allBookings = [];
        this.totalBookings = 0;
        this.confirmedBookings = 0;
        this.cancelledBookings = 0;
        this.cdr.detectChanges();
      }
    });

    this.travelService.getPassengerDirectory().subscribe({
      next: (data) => {
        this.passengerCount = data ? data.length : 0;
        this.cdr.detectChanges();
      },
      error: () => {
        this.passengerCount = 0;
        this.cdr.detectChanges();
      }
    });
  }
}   