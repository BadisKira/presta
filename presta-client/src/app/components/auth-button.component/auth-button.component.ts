import { Component, input, OnInit } from '@angular/core';
import {ButtonModule} from "primeng/button";
import { KeycloakService } from '../../services/keycloak/keycloak';
import { CommonModule } from '@angular/common';
@Component({
  selector: 'app-auth-button',
  imports: [ButtonModule,CommonModule],
  templateUrl: './auth-button.component.html',
  styleUrl: './auth-button.component.scss'
})
export class AuthButtonComponent implements OnInit {
  isLoggedIn = false;
  loading = false;

  role = input.required<'CLIENT' | 'CONTRACTOR'>();

  constructor(private keycloakService: KeycloakService) {}

  ngOnInit() {
    this.checkAuthStatus();
  }

  checkAuthStatus() {
    this.isLoggedIn = this.keycloakService.isLoggedIn();
  }

  async login() {
    this.loading = true;
    try {
      await this.keycloakService.login();
    } catch (error) {
      console.error('Erreur login:', error);
      this.loading = false;
    }
  }

  async logout() {
    this.loading = true;
    try {
      await this.keycloakService.logout();
    } catch (error) {
      console.error('Erreur logout:', error);
      this.loading = false;
    }
  }
}
