import { TestBed } from '@angular/core/testing';

import { ProductService } from '../product-assignment';

describe('ProductService', () => {
  let assignment: ProductService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    assignment = TestBed.inject(ProductService);
  });

  it('should be created', () => {
    expect(assignment).toBeTruthy();
  });
});
