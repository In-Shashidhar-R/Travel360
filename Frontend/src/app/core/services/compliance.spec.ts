import { TestBed } from '@angular/core/testing';

import { ComplianceService } from './compliance';

describe('Compliance', () => {
  let service: ComplianceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ComplianceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
