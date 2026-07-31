import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserAudit } from './user-audit';

describe('UserAudit', () => {
  let component: UserAudit;
  let fixture: ComponentFixture<UserAudit>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserAudit],
    }).compileComponents();

    fixture = TestBed.createComponent(UserAudit);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
