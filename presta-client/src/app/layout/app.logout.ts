import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { StyleClassModule } from 'primeng/styleclass';
import { KeycloakService } from '../services/keycloak/keycloak';
import { Button } from "primeng/button";

@Component({
    selector: 'app-logout',
    imports: [RouterModule, CommonModule, StyleClassModule, Button],
    template: `<p-button (click)="logout()">logout you shiti shit </p-button>`
})
export class AppLogout {
    keycloakService = inject(KeycloakService);



    async logout(){
        if(this.keycloakService.isLoggedIn()){
            await this.keycloakService.logout();
        }
    }
}