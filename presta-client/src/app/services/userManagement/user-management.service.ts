import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environment';
import { AuthUser, UpdateUserDto } from '../../models/user.model';
import { ContractorDto, ContractorSearchCriteria, UpdateContractorRequest } from '../../models/contractor.model';
import { Page } from '../../models/pagination.model';
import { Client, ClientSearchCriteria } from '../../models/client.model';

@Injectable({
  providedIn: 'root'
})
export class UserManagementService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api`;

  
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
  getCurrentUser(): Observable<AuthUser> {
    return this.http.get<AuthUser>(`${this.baseUrl}/users/me`);
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
  searchContractors(criteria: ContractorSearchCriteria = {}): Observable<Page<ContractorDto>> {
    let params = new HttpParams();
    
    if (criteria.name) params = params.set('name', criteria.name);
    if (criteria.speciality) params = params.set('speciality', criteria.speciality);
    if (criteria.page !== undefined) params = params.set('page', criteria.page.toString());
    if (criteria.size !== undefined) params = params.set('size', criteria.size.toString());
    if (criteria.sortBy) params = params.set('sortBy', criteria.sortBy);
    if (criteria.sortDirection) params = params.set('sortDirection', criteria.sortDirection);

    return this.http.get<Page<ContractorDto>>(`${this.baseUrl}/contractors`, { params });
  }

  /**
   * Met à jour un contractor
   */
  updateContractor(
    contractorId: string, 
    request: UpdateContractorRequest
  ): Observable<{ message: string; contractor: ContractorDto }> {
    return this.http.patch<{ message: string; contractor: ContractorDto }>(
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