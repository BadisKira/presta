import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { StyleClassModule } from 'primeng/styleclass';
import { KeycloakService } from '../../../../services/keycloak/keycloak';

@Component({
  selector: 'topbar-component',
  imports: [RouterModule,StyleClassModule, ButtonModule, RippleModule,CommonModule],
  templateUrl: './topbar.html',
  styleUrl: './topbar.scss'
})
export class TopbarComponent implements OnInit{

  keycloakService = inject(KeycloakService)
  router = inject(Router);
  isLoggedIn = false;
  loading = false;

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