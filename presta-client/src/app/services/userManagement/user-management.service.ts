import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError, from, map, Observable, shareReplay, tap } from 'rxjs';
import { environment } from '../../../environment';
import { AuthUser, UpdateUserDto } from '../../models/user.model';
import { Contractor, ContractorSearchCriteria, UpdateContractorRequest } from '../../models/contractor.model';
import { Page } from '../../models/pagination.model';
import { Client, ClientSearchCriteria } from '../../models/client.model';
import { KeycloakService } from '../keycloak/keycloak';
import { KeycloakProfile } from 'keycloak-js';

// types.ts – adapte selon tes modèles
export type Me =
  | (Contractor & { kind: 'CONTRACTOR' })
  | (Client & { kind: 'CLIENT' })
  | (KeycloakProfile & { kind: 'ADMIN' });


@Injectable({
  providedIn: 'root'
})
export class UserManagementService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api`;
  private readonly keycloakService = inject(KeycloakService);

  private currentUser$?: Observable<Me>;
  private currentUserCache?: Me;

  ;


  /**
   * Synchronise l'utilisateur actuel avec un rôle
   */
  syncUser(role: 'CLIENT' | 'CONTRACTOR'): Observable<string> {
    const params = new HttpParams().set('role', role);
    return this.http.get(`${this.baseUrl}/users/sync`, {
      params,
      responseType: 'text'
    });
  }

  /**
   * Récupère les informations de l'utilisateur connecté
   */

  getCurrentUser(): Observable<Me> {
    if (this.currentUser$) return this.currentUser$; // déjà en cache (Observable chaud)


    if (this.keycloakService.hasRole('CONTRACTOR')) {
      this.currentUser$ = this.http.get<Contractor>(`${this.baseUrl}/contractors/me`).pipe(
        map(u => ({ ...u, kind: 'CONTRACTOR' as const })),
        tap(u => (this.currentUserCache = u)),
        shareReplay(1),
        catchError(err => this.resetOnError(err))
      );
    } else if (this.keycloakService.hasRole('CLIENT')) {
      this.currentUser$ = this.http.get<Client>(`${this.baseUrl}/clients/me`).pipe(
        map(u => ({ ...u, kind: 'CLIENT' as const })),
        tap(u => (this.currentUserCache = u)),
        shareReplay(1),
        catchError(err => this.resetOnError(err))
      );
    } else {
      // ADMIN (ou rôle absent) → Keycloak
      this.currentUser$ = from(this.keycloakService.getUserInfo()).pipe(
        map(p => ({ ...(p as KeycloakProfile), kind: 'ADMIN' as const })),
        tap(u => (this.currentUserCache = u)),
        shareReplay(1),
        catchError(err => this.resetOnError(err))
      );
    }

    return this.currentUser$;
  }

  /** Accès synchrone au cache si déjà chargé (utile pour guards rapides) */
  getCachedUser(): Me | undefined {
    return this.currentUserCache;
  }

  /** Permet d’invalider manuellement le cache (logout, switch de rôle, etc.) */
  invalidateUserCache(): void {
    this.currentUser$ = undefined;
    this.currentUserCache = undefined;
  }

  private resetOnError(err: unknown): Observable<never> {
    this.invalidateUserCache();
    throw err;
  }
  /**
   * Supprime un utilisateur par son username
   */
  deleteUser(username: string): Observable<string> {
    return this.http.delete(`${this.baseUrl}/users/${username}`, {
      responseType: 'text'
    });
  }

  /**
   * Assigne un rôle à un utilisateur
   */
  assignRole(userId: string, role: string): Observable<string> {
    return this.http.post(`${this.baseUrl}/users/${userId}/roles`,
      { role },
      { responseType: 'text' }
    );
  }

  /**
   * Retire un rôle d'un utilisateur
   */
  removeRole(username: string, role: string): Observable<string> {
    return this.http.delete(`${this.baseUrl}/users/${username}/roles/${role}`, {
      responseType: 'text'
    });
  }

  /**
   * Met à jour les informations d'un utilisateur dans Keycloak
   */
  updateUser(userId: string, dto: UpdateUserDto): Observable<string> {
    return this.http.put(`${this.baseUrl}/users/${userId}`, dto, {
      responseType: 'text'
    });
  }

  /**
   * Bannit un utilisateur (désactive son compte)
   */
  banUser(userId: string): Observable<string> {
    return this.http.put(`${this.baseUrl}/users/${userId}/ban`, {}, {
      responseType: 'text'
    });
  }

  /**
   * Débannit un utilisateur (réactive son compte)
   */
  unbanUser(userId: string): Observable<string> {
    return this.http.put(`${this.baseUrl}/users/${userId}/unban`, {}, {
      responseType: 'text'
    });
  }

  // ========== Contractor Controller Methods ==========

  /**
   * Recherche des contractors avec critères
   */
  searchContractors(criteria: ContractorSearchCriteria = {}): Observable<Page<Contractor>> {
    let params = new HttpParams();

    if (criteria.name) params = params.set('name', criteria.name);
    if (criteria.speciality) params = params.set('speciality', criteria.speciality);
    if (criteria.page !== undefined) params = params.set('page', criteria.page.toString());
    if (criteria.size !== undefined) params = params.set('size', criteria.size.toString());
    if (criteria.sortBy) params = params.set('sortBy', criteria.sortBy);
    if (criteria.sortDirection) params = params.set('sortDirection', criteria.sortDirection);

    return this.http.get<Page<Contractor>>(`${this.baseUrl}/contractors`, { params });
  }

  /**
   * Met à jour un contractor
   */
  updateContractor(
    contractorId: string,
    request: UpdateContractorRequest
  ): Observable<{ message: string; contractor: Contractor }> {
    return this.http.patch<{
      message: string; contractor: Contractor


    }>(
      `${this.baseUrl}/contractors/${contractorId}`,
      request
    );
  }

  // ========== Client Controller Methods ==========

  /**
   * Recherche des clients avec critères
   */
  searchClients(criteria: ClientSearchCriteria = {}): Observable<Page<Client>> {
    let params = new HttpParams();

    if (criteria.name) params = params.set('name', criteria.name);
    if (criteria.page !== undefined) params = params.set('page', criteria.page.toString());
    if (criteria.size !== undefined) params = params.set('size', criteria.size.toString());
    if (criteria.sortBy) params = params.set('sortBy', criteria.sortBy);
    if (criteria.sortDirection) params = params.set('sortDirection', criteria.sortDirection);

    return this.http.get<Page<Client>>(`${this.baseUrl}/clients`, { params });
  }


  /**
   * Construit les paramètres de pagination par défaut
   */
  private buildPaginationParams(
    page = 0,
    size = 10,
    sortBy = 'id',
    sortDirection: 'asc' | 'desc' = 'asc'
  ): HttpParams {
    return new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);
  }
}