import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InventoryEditModal } from './inventory-edit-modal';

describe('InventoryEditModal', () => {
  let component: InventoryEditModal;
  let fixture: ComponentFixture<InventoryEditModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InventoryEditModal],
    }).compileComponents();

    fixture = TestBed.createComponent(InventoryEditModal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
