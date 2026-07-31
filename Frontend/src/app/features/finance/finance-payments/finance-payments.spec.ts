import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FinancePayments } from './finance-payments';

describe('FinancePayments', () => {
  let component: FinancePayments;
  let fixture: ComponentFixture<FinancePayments>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FinancePayments],
    }).compileComponents();

    fixture = TestBed.createComponent(FinancePayments);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
