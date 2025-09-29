import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { KeycloakService } from './services/keycloak/keycloak';
import { ToastModule } from "primeng/toast";
import { ignoreElements } from 'rxjs';
import { UserManagementService } from './services/userManagement/user-management.service';
import { Contractor } from './models/contractor.model';
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
  private userManegementService = inject(UserManagementService);
  private router = inject(Router);



  ngOnInit(): void { 
    if( this.keycloakService.getRoles().includes("CONTRACTOR")){
        this.userManegementService.getCurrentUser().subscribe(
        (user) =>{
            const contractor = user as Contractor;
            console.log("app.ts contractor ==>" , contractor);
            if(contractor.address ==="" || contractor.assignment == null ){
              // redirect vers la page de remplissage d'information 
              // complementaire 
                 this.router.navigate(['/contractor/complete-profile']);
            }
        })
    }  
  }
}
