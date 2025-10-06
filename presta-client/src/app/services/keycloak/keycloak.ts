import { inject, Injectable } from '@angular/core';
import Keycloak, { KeycloakProfile } from 'keycloak-js';
import { UserProfile } from '../../models/user.model';
import { HttpClient } from '@angular/common/http';
import { MessageService } from 'primeng/api';
import { Router } from '@angular/router';
import { catchError, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class KeycloakService {

  private _keycloak: Keycloak | undefined;
  private http = inject(HttpClient);
  private _urlSyncUser = "http://localhost:8080/api/users/sync";
  private messageService = inject(MessageService);
  private router = inject(Router);


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
    this.keycloak.onAuthSuccess = async () => {

      console.log("Suis je ici ? ");
      console.log(this.getRoles());
      if (this.getRoles().includes('ADMIN')) {
        //localStorage.setItem('role', 'ADMIN');
        //localStorage.setItem('userSynced', 'true');
        return;
      }
      if (!localStorage.getItem('userSynced')
        && !this.getRoles().includes('CONTRACTOR')
        && !this.getRoles().includes('CLIENT')) {
        this.syncUserWithBackend();
        //localStorage.setItem('userSynced', 'true');
        return;
      }
    };

    this.keycloak.onAuthError = (err) => console.error('Auth error', err);

    this.keycloak.onAuthLogout = () => {
      localStorage.removeItem('userSynced');
    };

    const authenticated = await this.keycloak.init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
      checkLoginIframe: false,
    });

    if (authenticated) {
      this._profile = (await this.keycloak.loadUserProfile()) as UserProfile;
      this._profile.token = this.keycloak.token || '';
    }
  }


  register() {
    return this.keycloak.register({
      redirectUri: window.location.origin,
      scope: 'openid profile email'
    });
  }

  login() {
    return this.keycloak.login({
      redirectUri: window.location.origin,
      scope: 'openid profile email'
    });
  }

  logout() {
    localStorage.removeItem('userSynced');
    return this.keycloak.logout({ redirectUri: window.location.origin });
  }

  isLoggedIn(): boolean {
    return !!this.keycloak.authenticated && !this.keycloak.isTokenExpired();
  }


  hasRole(role: string): boolean {
    const roles = this.keycloak.tokenParsed?.realm_access?.roles || [];
    return roles.includes(role);
  }

  getRoles(): string[] {
    return this.keycloak.tokenParsed?.realm_access?.roles || [];
  }



  async syncUserWithBackend(): Promise<boolean> {
    console.log(this.keycloak.token)
    if (!this.keycloak.token || localStorage.getItem("userSynced")) {
      return false;
    }

    let tentative = 0;
    const maxTentatives = 3;

    while (tentative < maxTentatives) {
      tentative++;

      try {
        await this.http.get(this._urlSyncUser + `?role=${localStorage.getItem("role")}`, {
          responseType: 'text'
        }).toPromise();

        localStorage.removeItem('role');
        return true;

      } catch (error) {
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

  getUserInfo(): Promise<KeycloakProfile> {
    return this.keycloak.loadUserProfile()
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
    if (error.status < 400) {
      return;
    }
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
