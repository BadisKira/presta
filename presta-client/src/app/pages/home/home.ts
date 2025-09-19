import { Component, inject, OnInit, signal } from '@angular/core';
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
  role = signal<'CLIENT' | 'CONTRACTOR'>("CLIENT");

  syncUser() {
    console.log(this.keycloakService.keycloak.token);
    this.keycloakService.syncUserWithBackend();
  }

  changeRole(newRole:"CLIENT"| "CONTRACTOR"){
    this.role.set(newRole);
    console.log(this.role());
    localStorage.setItem("role",this.role());
  }
}
