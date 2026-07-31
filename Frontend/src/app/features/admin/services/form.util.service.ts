import { Injectable, ChangeDetectorRef } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { CountryConfig, StateConfig, DistrictConfig } from '../../../shared/models/location.model';

@Injectable({
  providedIn: 'root'
})
export class FormUtilsService {

  cascadeLocations(
    field: 'country' | 'state' | 'district',
    event: Event,
    form: FormGroup,
    countries: CountryConfig[],
    currentStates: StateConfig[],
    currentDistricts: DistrictConfig[],
    cdr: ChangeDetectorRef
  ) {
    const value = (event.target as HTMLSelectElement).value;
    form.get(field)?.setValue(value);

    let states = [...currentStates];
    let districts = [...currentDistricts];
    let cities: string[] = [];

    if (field === 'country') {
      const match = countries.find(c => c.name === value);
      states = match && match.states ? match.states : [];
      districts = [];
      cities = [];
      form.patchValue({ state: '', district: '', city: '' });
      
      this.toggle(form, 'state', states.length > 0);
      this.toggle(form, 'district', false);
      this.toggle(form, 'city', false);
    } 
    else if (field === 'state') {
      const match = currentStates.find(s => s.name === value);
      districts = match && match.districts ? match.districts : [];
      cities = [];
      form.patchValue({ district: '', city: '' });
      
      this.toggle(form, 'district', districts.length > 0);
      this.toggle(form, 'city', false);
    } 
    else if (field === 'district') {
      const match = currentDistricts.find(d => d.name === value);
      cities = match && match.cities ? match.cities : [];
      form.patchValue({ city: '' });
      
      this.toggle(form, 'city', cities.length > 0);
    }
    cdr.detectChanges();

    return { states, districts, cities };
  }

  syncDropdown(field: string, event: Event, form: FormGroup, cdr: ChangeDetectorRef): void {
    const value = (event.target as HTMLSelectElement).value;
    form.get(field)?.setValue(value);
    cdr.detectChanges();
  }

  private toggle(form: FormGroup, controlName: string, enable: boolean): void {
    const ctrl = form.get(controlName);
    if (enable) {
      ctrl?.enable();
    } else {
      ctrl?.disable();
      ctrl?.setValue('');
    }
  }
}