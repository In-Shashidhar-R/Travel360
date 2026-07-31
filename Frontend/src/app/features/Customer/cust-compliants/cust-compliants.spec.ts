import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustCompliants } from './cust-compliants';

describe('CustCompliants', () => {
  let component: CustCompliants;
  let fixture: ComponentFixture<CustCompliants>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustCompliants],
    }).compileComponents();

    fixture = TestBed.createComponent(CustCompliants);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
