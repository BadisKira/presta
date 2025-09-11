import { TestBed } from '@angular/core/testing';

import { Keycloak } from './keycloak';

describe('Keycloak', () => {
  let assignment: Keycloak;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    assignment = TestBed.inject(Keycloak);
  });

  it('should be created', () => {
    expect(assignment).toBeTruthy();
  });
});
