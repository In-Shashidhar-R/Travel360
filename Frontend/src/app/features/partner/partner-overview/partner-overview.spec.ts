import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PartnerOverview } from './partner-overview';

describe('PartnerOverview', () => {
  let component: PartnerOverview;
  let fixture: ComponentFixture<PartnerOverview>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PartnerOverview],
    }).compileComponents();

    fixture = TestBed.createComponent(PartnerOverview);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
