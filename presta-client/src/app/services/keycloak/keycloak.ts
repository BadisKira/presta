import { inject, Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { UserProfile } from '../../models/user-profile';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class KeycloakService {

  private _keycloak: Keycloak | undefined;
  private http = inject(HttpClient);
  private _urlSyncUser = "http://localhost:8080/api/users/sync";

  get keycloak() {
    if (!this._keycloak) {
      this._keycloak = new Keycloak({
        url: 'http://localhost:9090',
        realm: "presta-realm",
        clientId: "presta-client"
      },
      );
    }
    return this._keycloak;
  }

  private _profile: UserProfile | undefined;

  get profile(): UserProfile | undefined {
    return this._profile;
  }

  async init() {
    const authenticated = await this.keycloak.init({
      onLoad: 'check-sso',
      //silentCheckSsoRedirectUri:window.location.origin + '/assets/silent-check-sso.html',
      checkLoginIframe: false,
      // Stockage du token en localeStorage je sais pas si c'est une bonne idée 
      token: localStorage.getItem("kc_token") || undefined,
      refreshToken: localStorage.getItem("kc_refreshToken") || undefined,
    });

    if (authenticated) {
      this._profile = (await this.keycloak.loadUserProfile()) as UserProfile;
      this._profile.token = this.keycloak.token || '';
    }
  }

  async login() {
    await this.keycloak.login();
  }

  logout() {
    // this.keycloak.accountManagement();
    return this.keycloak.logout({ redirectUri: 'http://localhost:4200' });
  }

  isLoggedIn(): boolean {
    return !!this.keycloak.authenticated && !this.keycloak.isTokenExpired();
  }


  hasRole(role: string): boolean {
    const roles = this.keycloak.tokenParsed?.realm_access?.roles || [];
    return roles.includes(role);
  }



  async syncUserWithBackend(): Promise<boolean> {
    if (!this.keycloak.token) {
      console.log("Sync with backend but no token ")
      return false;
    }
    

    if (localStorage.getItem("userSynced")) {
      return false;
    }

    let tentative = 0;
    const maxTentatives = 3;
    
    while (tentative < maxTentatives) {
      tentative++;
      
      try {
        const response = await this.http.get(this._urlSyncUser).toPromise();
        localStorage.setItem("userSynced", "true");
        return true;
        
      } catch (error: any) {
        if (tentative === maxTentatives) {
          this.handleSyncError(error);
          return false;
        }
        const delai = tentative * 1000;
        await this.wait(delai);
      }
    }
    
    return false;
  }

  /**
   * Fonction helper pour attendre
   */
  private wait(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  /**
   * Gestion des erreurs de sync
   */
  private handleSyncError(error: any): void {
    if (error.status === 401) {
      console.error('Token invalide ou expiré');
    } else if (error.status === 500) {
      console.error('Erreur serveur');
    } else if (error.status === 0) {
      console.error('Pas de connexion au serveur');
    } else {
      console.error('Erreur inconnue:', error.message);
    }
  }

}
