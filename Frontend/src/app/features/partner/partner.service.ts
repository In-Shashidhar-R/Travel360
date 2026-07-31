import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PartnerService {

  private api = 'http://localhost:9095/api/v1';

  constructor(
    private http: HttpClient
  ) {}

  getMyInventories(): Observable<any> {
    return this.http.get(
      `${this.api}/inventories/mine?page=0&size=50&direction=DESC`
    );
  }

  activateInventory(id:number): Observable<any> {
    return this.http.put(
      `${this.api}/inventories/${id}/activate`,
      {}
    );
  }

  deactivateInventory(id:number): Observable<any> {
    return this.http.put(
      `${this.api}/inventories/${id}/deactivate`,
      {}
    );
  }
}