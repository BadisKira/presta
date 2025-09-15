import { TestBed } from '@angular/core/testing';

import { KeycloakService } from './keycloak';

describe('Keycloak', () => {
  let assignment: KeycloakService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    assignment = TestBed.inject(KeycloakService);
  });

  it('should be created', () => {
    expect(assignment).toBeTruthy();
  });
});
