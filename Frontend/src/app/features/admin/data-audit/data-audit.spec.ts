import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataAudit } from './data-audit';

describe('DataAudit', () => {
  let component: DataAudit;
  let fixture: ComponentFixture<DataAudit>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DataAudit],
    }).compileComponents();

    fixture = TestBed.createComponent(DataAudit);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
