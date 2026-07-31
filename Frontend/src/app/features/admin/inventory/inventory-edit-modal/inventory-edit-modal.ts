import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { AdminService } from '../../services/admin.service';

@Component({
  selector: 'app-inventory-edit-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './inventory-edit-modal.html',
  styleUrl: './inventory-edit-modal.scss'
})
export class InventoryEditModal implements OnChanges {
  @Input() item!: any;
  @Output() close = new EventEmitter<void>();
  @Output() saved = new EventEmitter<void>();

  inventoryForm: FormGroup;
  busSeatTypes = ['AC_SLEEPER', 'NON_AC_SLEEPER', 'AC_SEATER', 'NON_AC_SEATER'];
  flightSeatTypes = ['ECONOMY', 'PREMIUM_ECONOMY', 'BUSINESS', 'FIRST_CLASS'];

  constructor(
    private fb: FormBuilder, 
    private adminService: AdminService,
    private cdr: ChangeDetectorRef
  ) {
    this.inventoryForm = this.fb.group({
      inventoryId: [''],
      itemType: [''],
      partnerId: ['', [Validators.required, Validators.min(1)]],
      basePricePerSeat: [0],
      
      flightNumber: [''], airlineName: [''], departureAirport: [''], arrivalAirport: [''], isConnecting: [false], layoverDetails: [''], startTime: [''], endTime: [''],
      
      busNumberPlate: [''], operatorName: [''], routeFrom: [''], routeTo: [''],
      
      vehicleRegistrationNumber: [''], carModel: [''], fuelType: [''], seaterCount: [4],
      
      totalRooms: [10], basePricePerRoom: [0], hotelName: [''], roomType: [''], hotelRating: [1], addressLocation: [''], district: [''], state: [''], country: [''],
      
      basePricePerPersonForPackage: [0], packageName: [''], fullItineraryDetails: [''], durationDays: [1], travelAgentId: [''],
      
      seatTiers: this.fb.array([]),
      routeStops: this.fb.array([])
    });
  }

  get seatTiers(): FormArray { return this.inventoryForm.get('seatTiers') as FormArray; }
  get routeStops(): FormArray { return this.inventoryForm.get('routeStops') as FormArray; }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['item'] && changes['item'].currentValue) {
      this.hydrateFormWorkspace(changes['item'].currentValue);
    }
  }

  private hydrateFormWorkspace(rawItem: any): void {
    this.seatTiers.clear();
    this.routeStops.clear();
    
    this.inventoryForm.patchValue({ itemType: rawItem.itemType });
    this.cdr.detectChanges();

    const patchPayload: Record<string, any> = {
      inventoryId: rawItem.inventoryId,
      itemType: rawItem.itemType,
      partnerId: rawItem.partnerId,
      basePricePerSeat: rawItem.basePricePerSeat || 0
    };

    if (rawItem.itemType === 'FLIGHT') {
      patchPayload['flightNumber'] = rawItem.flightNumber;
      patchPayload['airlineName'] = rawItem.airlineName;
      patchPayload['departureAirport'] = rawItem.departureAirport;
      patchPayload['arrivalAirport'] = rawItem.arrivalAirport;
      patchPayload['startTime'] = rawItem.startTime;
      patchPayload['endTime'] = rawItem.endTime;
      patchPayload['isConnecting'] = !!rawItem.isConnecting;
      patchPayload['layoverDetails'] = rawItem.layoverDetails || '';
      
      if (Array.isArray(rawItem.seatTiers)) {
        rawItem.seatTiers.forEach((tier: any) => this.addSeatTierControl(tier));
      }
    } 
    else if (rawItem.itemType === 'BUS') {
      patchPayload['busNumberPlate'] = rawItem.busNumberPlate;
      patchPayload['operatorName'] = rawItem.operatorName;
      patchPayload['routeFrom'] = rawItem.routeFrom;
      patchPayload['routeTo'] = rawItem.routeTo;
      patchPayload['startTime'] = rawItem.startTime;
      patchPayload['endTime'] = rawItem.endTime;

      if (Array.isArray(rawItem.seatTiers)) {
        rawItem.seatTiers.forEach((tier: any) => this.addSeatTierControl(tier));
      }
      if (Array.isArray(rawItem.routeStops)) {
        rawItem.routeStops.forEach((stop: any) => this.addRouteStopControl(stop));
      }
    } 
    else if (rawItem.itemType === 'CAB') {
      patchPayload['vehicleRegistrationNumber'] = rawItem.vehicleRegistrationNumber;
      patchPayload['carModel'] = rawItem.carModel;
      patchPayload['fuelType'] = rawItem.fuelType;
      patchPayload['seaterCount'] = rawItem.seaterCount;
      patchPayload['district'] = rawItem.district;
      patchPayload['state'] = rawItem.state;
    } 
    else if (rawItem.itemType === 'HOTEL') {
      patchPayload['hotelName'] = rawItem.hotelName;
      patchPayload['roomType'] = rawItem.roomType;
      patchPayload['totalRooms'] = rawItem.totalRooms;
      patchPayload['basePricePerRoom'] = rawItem.basePricePerSeat;
      patchPayload['hotelRating'] = rawItem.hotelRating;
      patchPayload['addressLocation'] = rawItem.addressLocation;
      patchPayload['district'] = rawItem.district;
      patchPayload['state'] = rawItem.state;
      patchPayload['country'] = rawItem.country;
    } 
    else if (rawItem.itemType === 'TOUR_PACKAGE') {
      patchPayload['packageName'] = rawItem.packageName;
      patchPayload['basePricePerPersonForPackage'] = rawItem.basePricePerSeat;
      patchPayload['durationDays'] = rawItem.durationDays;
      patchPayload['travelAgentId'] = rawItem.travelAgentId;
      patchPayload['fullItineraryDetails'] = rawItem.fullItineraryDetails;
    }

    this.inventoryForm.patchValue(patchPayload);
    this.inventoryForm.updateValueAndValidity();

    setTimeout(() => {
      this.cdr.detectChanges();
    });
  }

  addSeatTierControl(data?: any): void {
    this.seatTiers.push(this.fb.group({
      seatType: [data?.seatType || '', Validators.required],
      priceMultiplier: [data?.priceMultiplier ?? 1.0, [Validators.required, Validators.min(0.0)]],
      totalSeatsAllocated: [data?.totalSeatsAllocated ?? 10, [Validators.required, Validators.min(1)]]
    }));

    setTimeout(() => {
      this.cdr.detectChanges();
    });
  }

  removeSeatTierControl(i: number): void { 
    this.seatTiers.removeAt(i); 
    setTimeout(() => {
      this.cdr.detectChanges();
    });
  }

  addRouteStopControl(data?: any): void {
    this.routeStops.push(this.fb.group({
      stopName: [data?.stopName || '', Validators.required],
      stopType: [data?.stopType || 'WAYPOINT', Validators.required],
      scheduledTime: [data?.scheduledTime || '', Validators.required]
    }));

    setTimeout(() => {
      this.cdr.detectChanges();
    });
  }

  removeRouteStopControl(i: number): void { 
    this.routeStops.removeAt(i); 
    setTimeout(() => {
      this.cdr.detectChanges();
    });
  }

  onSaveEdit(): void {
    if (this.inventoryForm.invalid) {
      this.inventoryForm.markAllAsTouched();
      alert('Form validation failed. Review all nested sub-tier elements.');
      return;
    }

    const formRaw = this.inventoryForm.value;
    const itemId = formRaw.inventoryId;
    let updateObservable: Observable<any> | null = null;

    switch (formRaw.itemType) {
      case 'FLIGHT':
        updateObservable = this.adminService.updateFlight(itemId, {
          partnerId: Number(formRaw.partnerId),
          basePricePerSeat: Number(formRaw.basePricePerSeat),
          flightNumber: formRaw.flightNumber,
          airlineName: formRaw.airlineName,
          departureAirport: formRaw.departureAirport,
          arrivalAirport: formRaw.arrivalAirport,
          layoverDetails: formRaw.isConnecting ? formRaw.layoverDetails : '',
          startTime: formRaw.startTime,
          endTime: formRaw.endTime,
          seatTiers: formRaw.seatTiers,
          connecting: !!formRaw.isConnecting
        });
        break;

      case 'BUS':
        updateObservable = this.adminService.updateBus(itemId, {
          partnerId: Number(formRaw.partnerId),
          basePricePerSeat: Number(formRaw.basePricePerSeat),
          busNumberPlate: formRaw.busNumberPlate,
          operatorName: formRaw.operatorName,
          routeFrom: formRaw.routeFrom,
          routeTo: formRaw.routeTo,
          startTime: formRaw.startTime,
          endTime: formRaw.endTime,
          routeStops: formRaw.routeStops,
          seatTiers: formRaw.seatTiers
        });
        break;

      case 'CAB':
        updateObservable = this.adminService.updateCab(itemId, {
          partnerId: Number(formRaw.partnerId),
          basePricePerSeat: Number(formRaw.basePricePerSeat),
          vehicleRegistrationNumber: formRaw.vehicleRegistrationNumber,
          carModel: formRaw.carModel,
          fuelType: formRaw.fuelType,
          seaterCount: Number(formRaw.seaterCount),
          district: formRaw.district,
          state: formRaw.state
        });
        break;

      case 'HOTEL':
        updateObservable = this.adminService.updateHotel(itemId, {
          partnerId: Number(formRaw.partnerId),
          totalRooms: Number(formRaw.totalRooms),
          basePricePerRoom: Number(formRaw.basePricePerRoom),
          hotelName: formRaw.hotelName,
          roomType: formRaw.roomType,
          hotelRating: Number(formRaw.hotelRating),
          addressLocation: formRaw.addressLocation,
          district: formRaw.district,
          state: formRaw.state,
          country: formRaw.country
        });
        break;

      case 'TOUR_PACKAGE':
        updateObservable = this.adminService.updateTour(itemId, {
          partnerId: Number(formRaw.partnerId),
          basePricePerPersonForPackage: Number(formRaw.basePricePerPersonForPackage),
          packageName: formRaw.packageName,
          fullItineraryDetails: formRaw.fullItineraryDetails,
          durationDays: Number(formRaw.durationDays),
          travelAgentId: Number(formRaw.travelAgentId)
        });
        break;
    }

    if (!updateObservable) return;

    updateObservable.subscribe({
      next: () => {
        alert('Specifications modified successfully across system registry endpoints.');
        this.saved.emit();
      },
      error: (err: any) => {
        console.error('💥 Upstream Registry Mutation Failure Context:', err);
        alert('Could not update specifications. Confirm payload parameters align with upstream validation rules.');
      }
    });
  }
}