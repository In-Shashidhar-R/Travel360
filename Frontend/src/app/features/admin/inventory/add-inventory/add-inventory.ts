import { Component, Output, EventEmitter, ChangeDetectorRef, signal } from '@angular/core'; 
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { FormUtilsService } from '../../services/form.util.service'; 
import { LOCATION_DATA, CountryConfig, StateConfig, DistrictConfig } from '../../../../shared/models/location.model';
import DOMPurify from 'dompurify';

@Component({
  selector: 'app-add-inventory',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './add-inventory.html',
  styleUrl: './add-inventory.scss'
})
export class AddInventory {
  @Output() alertTrigger = new EventEmitter<{ type: 'success' | 'error', message: string }>();

  inventoryForm: FormGroup;
  
  successMessage = signal<string>('');
  errorMessage = signal<string>('');

  busSeatTypes = ['AC_SLEEPER', 'NON_AC_SLEEPER', 'AC_SEATER', 'NON_AC_SEATER'];
  flightSeatTypes = ['ECONOMY', 'PREMIUM_ECONOMY', 'BUSINESS', 'FIRST_CLASS'];
  countries: CountryConfig[] = LOCATION_DATA;
  
  hotelStates: StateConfig[] = [];
  hotelDistricts: DistrictConfig[] = [];
  cabStates: StateConfig[] = [];
  cabDistricts: DistrictConfig[] = [];

  airports = [
    { code: 'MAA', name: 'Chennai International Airport (MAA)' },
    { code: 'BOM', name: 'Chhatrapati Shivaji Maharaj Airport (BOM)' },
    { code: 'DEL', name: 'Indira Gandhi International Airport (DEL)' },
    { code: 'BLR', name: 'Kempegowda International Airport (BLR)' },
    { code: 'HYD', name: 'Rajiv Gandhi International Airport (HYD)' }
  ];

  busTerminals = [
    'Chennai Koyambedu (CMBT)', 'Bangalore Majestic (KSRTC)', 'Mumbai Central Terminal', 'Hyderabad MGBS Hub'
  ];

  constructor(
    private fb: FormBuilder, 
    private adminService: AdminService,
    public cdr: ChangeDetectorRef,        
    public formUtils: FormUtilsService   
  ) {
    this.inventoryForm = this.fb.group({
      inventoryType: ['', Validators.required], 
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

  onHotelLocationCascade(field: 'country' | 'state' | 'district', event: Event): void {
    const res = this.formUtils.cascadeLocations(field, event, this.inventoryForm, this.countries, this.hotelStates, this.hotelDistricts, this.cdr);
    this.hotelStates = res.states;
    this.hotelDistricts = res.districts;

    setTimeout(() => {
      this.cdr.detectChanges();
    });
  }

  addSeatTierControl(): void { 
    const group = this.fb.group({ 
      seatType: ['', Validators.required], 
      priceMultiplier: [1.0, [Validators.required, Validators.min(0.0)]], 
      totalSeatsAllocated: [10, [Validators.required, Validators.min(1)]] 
    });
    this.seatTiers.push(group);
    this.seatTiers.updateValueAndValidity();

    setTimeout(() => {
      this.cdr.detectChanges();
    });
  }
  
  removeSeatTierControl(i: number): void { 
    this.seatTiers.removeAt(i); 
    this.seatTiers.updateValueAndValidity();

    setTimeout(() => {
      this.cdr.detectChanges();
    });
  }
  
  addRouteStopControl(): void { 
    const group = this.fb.group({ 
      stopName: ['', Validators.required], 
      stopType: ['WAYPOINT', Validators.required], 
      scheduledTime: ['', Validators.required] 
    });
    this.routeStops.push(group);
    this.routeStops.updateValueAndValidity();

    setTimeout(() => {
      this.cdr.detectChanges();
    });
  }
  
  removeRouteStopControl(i: number): void { 
    this.routeStops.removeAt(i); 
    this.routeStops.updateValueAndValidity();

    setTimeout(() => {
      this.cdr.detectChanges();
    });
  }

  onInventoryTypeChange(): void { 
    this.seatTiers.clear(); 
    this.routeStops.clear(); 
    this.successMessage.set(''); 
    this.errorMessage.set('');

    const type = this.inventoryForm.get('inventoryType')?.value; 
    const dynamicFields = [
      'basePricePerSeat', 'flightNumber', 'airlineName', 'departureAirport', 'arrivalAirport', 
      'layoverDetails', 'startTime', 'endTime', 'busNumberPlate', 'operatorName', 
      'routeFrom', 'routeTo', 'vehicleRegistrationNumber', 'carModel', 'fuelType', 
      'seaterCount', 'totalRooms', 'basePricePerRoom', 'hotelName', 'roomType', 
      'hotelRating', 'addressLocation', 'district', 'state', 'country', 
      'basePricePerPersonForPackage', 'packageName', 'fullItineraryDetails', 
      'durationDays', 'travelAgentId'
    ];
    
    dynamicFields.forEach(field => {
      const control = this.inventoryForm.get(field);
      if (control) {
        control.clearValidators();
        control.setErrors(null); 
        control.updateValueAndValidity({ emitEvent: false });
      }
    });
    
    if (type === 'HOTEL') {
      this.inventoryForm.get('state')?.disable({ emitEvent: false });
      this.inventoryForm.get('district')?.disable({ emitEvent: false });
    } else {
      this.inventoryForm.get('state')?.enable({ emitEvent: false });
      this.inventoryForm.get('district')?.enable({ emitEvent: false });
    }
    
    if (!type) return;

    const nameRegex = /^[a-zA-Z\s\-'\.]+$/; 
    const alnumNoTagsRegex = /^[^<>?!]*$/; 
    const basicNoHtmlRegex = /^[^<>]*$/; 

    if (type === 'FLIGHT') {
      this.addSeatTierControl(); 
      this.setFieldValidators('basePricePerSeat', [Validators.required, Validators.min(0.0)]);
      this.setFieldValidators('flightNumber', [Validators.required, Validators.pattern(alnumNoTagsRegex)]);
      this.setFieldValidators('airlineName', [Validators.required, Validators.pattern(nameRegex)]);
      this.setFieldValidators('departureAirport', [Validators.required]);
      this.setFieldValidators('arrivalAirport', [Validators.required]);
      this.setFieldValidators('startTime', [Validators.required]);
      this.setFieldValidators('endTime', [Validators.required]);
      this.setFieldValidators('layoverDetails', [Validators.pattern(basicNoHtmlRegex)]);
    } else if (type === 'BUS') {
      this.addSeatTierControl(); 
      this.addRouteStopControl(); 
      this.setFieldValidators('basePricePerSeat', [Validators.required, Validators.min(0.0)]);
      this.setFieldValidators('busNumberPlate', [Validators.required, Validators.pattern(alnumNoTagsRegex)]);
      this.setFieldValidators('operatorName', [Validators.required, Validators.pattern(nameRegex)]);
      this.setFieldValidators('routeFrom', [Validators.required]);
      this.setFieldValidators('routeTo', [Validators.required]);
      this.setFieldValidators('startTime', [Validators.required]);
      this.setFieldValidators('endTime', [Validators.required]);
    } else if (type === 'HOTEL') {
      this.setFieldValidators('hotelName', [Validators.required, Validators.pattern(nameRegex)]);
      this.setFieldValidators('roomType', [Validators.required, Validators.pattern(alnumNoTagsRegex)]);
      this.setFieldValidators('totalRooms', [Validators.required, Validators.min(1)]);
      this.setFieldValidators('basePricePerRoom', [Validators.required, Validators.min(0.0)]);
      this.setFieldValidators('hotelRating', [Validators.required, Validators.min(1), Validators.max(5)]);
      this.setFieldValidators('addressLocation', [Validators.required, Validators.pattern(basicNoHtmlRegex)]);
      this.setFieldValidators('district', [Validators.required]);
      this.setFieldValidators('state', [Validators.required]);
      this.setFieldValidators('country', [Validators.required]);
    } else if (type === 'TOUR_PACKAGE') {
      this.setFieldValidators('packageName', [Validators.required, Validators.pattern(nameRegex)]);
      this.setFieldValidators('basePricePerPersonForPackage', [Validators.required, Validators.min(0.0)]);
      this.setFieldValidators('durationDays', [Validators.required, Validators.min(1)]);
      this.setFieldValidators('travelAgentId', [Validators.required, Validators.min(1)]);
      this.setFieldValidators('fullItineraryDetails', [Validators.required, Validators.pattern(basicNoHtmlRegex)]);
    }

    this.inventoryForm.updateValueAndValidity();

    setTimeout(() => {
      this.cdr.detectChanges();
    });
  }

  private setFieldValidators(fieldName: string, validators: any[]): void {
    const control = this.inventoryForm.get(fieldName);
    if (control) {
      control.setValidators(validators);
      control.updateValueAndValidity({ emitEvent: false });
    }
  }

  submitInventoryForm(): void {
    this.successMessage.set('');
    this.errorMessage.set('');

    if (this.inventoryForm.invalid) {
      this.inventoryForm.markAllAsTouched();
      const brokenControls: string[] = [];
      const fields = this.inventoryForm.controls;
      for (const name in fields) {
        if (fields[name].invalid) brokenControls.push(name);
      }
      this.errorMessage.set(`Submission Blocked. Invalid Parameters: [${brokenControls.join(', ')}]`);
      return;
    }

    const type = this.inventoryForm.get('inventoryType')?.value;
    const formRaw = this.inventoryForm.value;

    switch (type) {
      case 'FLIGHT':
        const flightDTO = {
          partnerId: Number(formRaw.partnerId),
          basePricePerSeat: Number(formRaw.basePricePerSeat),
          flightNumber: DOMPurify.sanitize(formRaw.flightNumber?.trim() || ''),
          airlineName: DOMPurify.sanitize(formRaw.airlineName?.trim() || ''),
          departureAirport: formRaw.departureAirport,
          arrivalAirport: formRaw.arrivalAirport,
          layoverDetails: formRaw.isConnecting ? DOMPurify.sanitize(formRaw.layoverDetails?.trim() || '') : '',
          startTime: formRaw.startTime,
          endTime: formRaw.endTime,
          seatTiers: formRaw.seatTiers.map((tier: any) => ({
            seatType: tier.seatType,
            priceMultiplier: Number(tier.priceMultiplier),
            totalSeatsAllocated: Number(tier.totalSeatsAllocated)
          })),
          connecting: !!formRaw.isConnecting
        };
        this.adminService.provisionFlight(flightDTO).subscribe({
          next: () => this.handleSuccess(),
          error: (err) => this.handleError(err)
        });
        break;

      case 'HOTEL':
        const hotelDTO = {
          partnerId: Number(formRaw.partnerId),
          totalRooms: Number(formRaw.totalRooms),
          basePricePerRoom: Number(formRaw.basePricePerRoom),
          hotelName: DOMPurify.sanitize(formRaw.hotelName?.trim() || ''),
          roomType: DOMPurify.sanitize(formRaw.roomType?.trim() || ''),
          hotelRating: Number(formRaw.hotelRating),
          addressLocation: DOMPurify.sanitize(formRaw.addressLocation?.trim() || ''),
          district: formRaw.district,
          state: formRaw.state,
          country: formRaw.country
        };
        this.adminService.provisionHotel(hotelDTO).subscribe({
          next: () => this.handleSuccess(),
          error: (err) => this.handleError(err)
        });
        break;

      case 'BUS':
        const busDTO = {
          partnerId: Number(formRaw.partnerId),
          basePricePerSeat: Number(formRaw.basePricePerSeat),
          busNumberPlate: DOMPurify.sanitize(formRaw.busNumberPlate?.trim() || ''),
          operatorName: DOMPurify.sanitize(formRaw.operatorName?.trim() || ''),
          routeFrom: formRaw.routeFrom,
          routeTo: formRaw.routeTo,
          startTime: formRaw.startTime,
          endTime: formRaw.endTime,
          routeStops: formRaw.routeStops,
          seatTiers: formRaw.seatTiers.map((tier: any) => ({
            seatType: tier.seatType,
            priceMultiplier: Number(tier.priceMultiplier),
            totalSeatsAllocated: Number(tier.totalSeatsAllocated)
          }))
        };
        this.adminService.provisionBus(busDTO).subscribe({
          next: () => this.handleSuccess(),
          error: (err) => this.handleError(err)
        });
        break;

      case 'TOUR_PACKAGE':
        const tourDTO = {
          partnerId: Number(formRaw.partnerId),
          basePricePerPersonForPackage: Number(formRaw.basePricePerPersonForPackage),
          packageName: DOMPurify.sanitize(formRaw.packageName?.trim() || ''),
          fullItineraryDetails: DOMPurify.sanitize(formRaw.fullItineraryDetails?.trim() || ''),
          durationDays: Number(formRaw.durationDays),
          travelAgentId: Number(formRaw.travelAgentId)
        };
        this.adminService.provisionTour(tourDTO).subscribe({
          next: () => this.handleSuccess(),
          error: (err) => this.handleError(err)
        });
        break;
    }
  }

  private handleSuccess() {
    this.successMessage.set('Inventory Added Successfully');
    this.errorMessage.set('');
    this.inventoryForm.reset({ inventoryType: '', basePricePerSeat: 0, seaterCount: 4, totalRooms: 10, hotelRating: 1, durationDays: 1 });
    this.seatTiers.clear();
    this.routeStops.clear();
    this.cdr.detectChanges();
  }

  private handleError(err: any) {
    this.successMessage.set('');
    const msg = err.error?.message || err.message || 'Resource Allocation Error occurred.';
    this.errorMessage.set(`Server Execution Exception: ${msg}`);
    this.cdr.detectChanges();
  }
}