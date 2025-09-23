import { TestBed } from '@angular/core/testing';

import { ClientService } from './client.service';

describe('ClientService', () => {
  let assignment: ClientService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    assignment = TestBed.inject(ClientService);
  });

  it('should be created', () => {
    expect(assignment).toBeTruthy();
  });
});
