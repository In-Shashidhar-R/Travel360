import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class BookingService {
  private readonly baseUrl: string = 'http://localhost:9095/api/v1/booking-requests';
  private readonly userUrl: string = 'http://localhost:9095/api/v1/users'; 
  private readonly passengerUrl: string = 'http://localhost:9095/api/v1/passengers';

  constructor(private http: HttpClient) {}


  getAssignedRequests(page: number = 0, size: number = 10): Observable<any> {
    const queryParams = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('direction', 'DESC');

    return this.http.get(`${this.baseUrl}/assigned`, { params: queryParams });
  }

  acceptRequest(requestId: number, notes: string): Observable<any> {
    let decisionData = { agentNotes: notes };
    return this.http.put(`${this.baseUrl}/${requestId}/accept`, decisionData);
  }

  
  rejectRequest(requestId: number, notes: string): Observable<any> {
    let decisionData = { agentNotes: notes };
    return this.http.put(`${this.baseUrl}/${requestId}/reject`, decisionData);
  }

  bookTourPackage(requestId: number, tourBookingModel: any): Observable<any> {
    let bookingData = {
      customerId: tourBookingModel.customerId,
      inventoryId: tourBookingModel.inventoryId,
      targetTravelDate: tourBookingModel.targetTravelDate,
      numberOfPersons: tourBookingModel.numberOfPersons,
      passengerProfileIds: tourBookingModel.passengerProfileIds
    };

    return this.http.post(`${this.baseUrl}/${requestId}/book`, bookingData);
  }

  updateProfile(userId: number, profileData: any): Observable<any> {
    return this.http.put(`${this.userUrl}/${userId}`, profileData);
  }

 changePassword(passwordPayload: any): Observable<any> {
  
  return this.http.put(`${this.userUrl}/change-password`, passwordPayload, { responseType: 'text' });
}


 getProfileDetails(userId: number): Observable<any> {
  return this.http.get(`${this.userUrl}/${userId}`);
  }

  getPassengersByCustomerId(customerId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.passengerUrl}/customer/${customerId}`);
  }


}