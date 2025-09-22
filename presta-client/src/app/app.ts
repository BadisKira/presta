import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { KeycloakService } from './services/keycloak/keycloak';
import { ToastModule } from "primeng/toast";
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastModule],
  template: `
    <p-toast position="top-right"></p-toast>
    <router-outlet></router-outlet>
    `,
  styleUrl:'./assets/styles.scss'
})
export class App implements OnInit {
  protected readonly title = signal('presta-client');
  private keycloakService = inject(KeycloakService);

  ngOnInit(): void {
    localStorage.setItem("role","ADMIN")
    if (this.keycloakService.isLoggedIn() && localStorage.getItem("role") != "ADMIN") {
      this.keycloakService.syncUserWithBackend();
    }
  }
}
