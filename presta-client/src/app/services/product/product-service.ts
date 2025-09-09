import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/products';

  getProducts() {
    this.http.get(this.apiUrl).subscribe({
      next: (data) => console.log('Produits:', data),
      error: (err) => console.error('Erreur:', err)
    });
  }
}
