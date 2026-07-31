import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FinanceInvoices } from './finance-invoices';

describe('FinanceInvoices', () => {
  let component: FinanceInvoices;
  let fixture: ComponentFixture<FinanceInvoices>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FinanceInvoices],
    }).compileComponents();

    fixture = TestBed.createComponent(FinanceInvoices);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
