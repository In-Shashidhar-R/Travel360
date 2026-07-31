import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustRequests } from './cust-requests';

describe('CustRequests', () => {
  let component: CustRequests;
  let fixture: ComponentFixture<CustRequests>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustRequests],
    }).compileComponents();

    fixture = TestBed.createComponent(CustRequests);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
