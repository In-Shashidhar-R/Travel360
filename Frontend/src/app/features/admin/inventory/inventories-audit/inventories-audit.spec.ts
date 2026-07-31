import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InventoriesAudit } from './inventories-audit';

describe('InventoriesAudit', () => {
  let component: InventoriesAudit;
  let fixture: ComponentFixture<InventoriesAudit>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InventoriesAudit],
    }).compileComponents();

    fixture = TestBed.createComponent(InventoriesAudit);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
