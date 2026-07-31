import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-compliance-dashboard',
  standalone: true,
  imports: [ 
    CommonModule, 
    RouterOutlet, 
    RouterLink, 
    RouterLinkActive
  ], 
  templateUrl: './dashboard.html', 
  styleUrl: './dashboard.scss' 
}) 
export class DashboardC implements OnInit { 
  title = 'compliance-app';
  currentUserName: string = 'Compliance Officer';

  constructor(private router: Router, private authService: AuthService) {}

  ngOnInit(): void {
    const user = this.authService.currentUser();
    
    if (user && user.name) {
      this.currentUserName = user.name;
    } else {
        this.currentUserName = "Compliance Officer"
    }
  }

  logout(): void {
    this.authService.logout();
  }
}