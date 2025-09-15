import { Component, inject, OnInit } from '@angular/core';
import { AuthButtonComponent } from '../../components/auth-button.component/auth-button.component';
import { KeycloakService } from '../../services/keycloak/keycloak';

@Component({
  selector: 'app-home',
  imports: [AuthButtonComponent],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class HomePage {
  private keycloakService = inject(KeycloakService)
  syncUser() {
    console.log(this.keycloakService.keycloak.token);
    this.keycloakService.syncUserWithBackend();
  }

}
