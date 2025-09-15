import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { KeycloakService } from './services/keycloak/keycloak';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('presta-client');
  private keycloakService = inject(KeycloakService);
  
  ngOnInit(): void {
    if(this.keycloakService.isLoggedIn()){
      this.keycloakService.syncUserWithBackend();
    }
  }
}
