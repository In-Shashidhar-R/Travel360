import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InventoryBrowser } from './inventory-browser';

describe('InventoryBrowser', () => {
  let component: InventoryBrowser;
  let fixture: ComponentFixture<InventoryBrowser>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InventoryBrowser],
    }).compileComponents();

    fixture = TestBed.createComponent(InventoryBrowser);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
