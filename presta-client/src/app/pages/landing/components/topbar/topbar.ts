import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { StyleClassModule } from 'primeng/styleclass';
import { KeycloakService } from '../../../../services/keycloak/keycloak';
import { UserManagementService } from '../../../../services/userManagement/user-management.service';
import { Client } from '../../../../models/client.model';
import { Contractor } from '../../../../models/contractor.model';
import { KeycloakProfile } from 'keycloak-js';
import { ProfileDropdownComponent } from "../../../../layout/components/profile-dropdown/profile-dropdown";
import { DarkmodeButtonComponent } from "../../../../layout/components/darkmode-button.component/darkmode-button.component";

@Component({
  selector: 'topbar-component',
  imports: [RouterModule, StyleClassModule, ButtonModule, 
    RippleModule, CommonModule, ProfileDropdownComponent,
    DarkmodeButtonComponent],
  templateUrl: './topbar.html',
  styleUrl: './topbar.scss'
})
export class TopbarComponent implements OnInit {

  keycloakService = inject(KeycloakService)
  userManagementService = inject(UserManagementService);
  router = inject(Router);
  isLoggedIn = false;
  loading = false;

  _currentUser: Client | Contractor | KeycloakProfile | null = null;

  ngOnInit() {
    this.checkAuthStatus();
    if(this.isLoggedIn && localStorage.getItem("userSynced") === "true"){ 
      this.userManagementService.getCurrentUser().subscribe(user => {
        this._currentUser = user;
        //this.redirectByRole();
      });
    }
  }

  checkAuthStatus() {
    this.isLoggedIn = this.keycloakService.isLoggedIn();
  }




  redirectByRole(): void {
  const role = localStorage.getItem('role');

  switch (role) {
    case 'CONTRACTOR':
      this.router.navigate(['/contractor']);
      break;
    case 'CLIENT':
      this.router.navigate(['/client']);
      break;
    default:
      this.router.navigate(['/landing']);
      break;
  }
}



  async register(role: string = "CLIENT") {
    this.loading = true;
    localStorage.setItem("role", role);
    try {
      await this.keycloakService.register();
    } catch (error) {
      console.error('Erreur register:', error);
      this.loading = false;
    }
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