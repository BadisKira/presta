import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ProductService } from './services/product/product-service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('presta-client');
  private productService = inject(ProductService);

   
  ngOnInit(): void {
      console.log("Bonjour le constructor de APP")
      this.productService.getProducts();  }
}
