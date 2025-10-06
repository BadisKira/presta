import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, map, of } from 'rxjs';
import { PagedResponse } from '../../models/pagination.model';
import { Contractor, UpdateContractorRequest } from '../../models/contractor.model';
import { environment } from '../../../environment';
import { MessageService } from 'primeng/api';

// Interfaces de pagination
interface PageRequest {
  page: number;
  size: number;
  sortBy: string;
  sortDirection: string;
}


// Interface de filtres de recherche
export interface SearchFilters {
  name?: string;           // Correspond au paramètre 'name' du backend (recherche sur fullName)
  speciality?: string;     // Recherche par spécialité
  assignmentId?: string;   // Recherche par ID ou nom d'assignment
  address?: string;        // Recherche par adresse
}



@Injectable({
  providedIn: 'root'
})
export class ContractorService {

  private _url_base = environment.apiUrl + '/api/contractors';
  private messageService = inject(MessageService);
  constructor(private http: HttpClient) { }

  /**
   * Recherche de contractors avec pagination complète
   */
  searchContractors(
    filters: SearchFilters = {},
    pageRequest: PageRequest = { page: 0, size: 10, sortBy: 'id', sortDirection: 'asc' }
  ): Observable<PagedResponse<Contractor>> {
    let params = new HttpParams();

    // Paramètres de recherche (tous optionnels)
    if (filters.name?.trim()) {
      params = params.set('name', filters.name.trim());
    }
    if (filters.speciality?.trim()) {
      params = params.set('speciality', filters.speciality.trim());
    }
    if (filters.assignmentId?.trim()) {
      params = params.set('assignmentId', filters.assignmentId.trim());
    }
    if (filters.address?.trim()) {
      params = params.set('address', filters.address.trim());
    }

    // Paramètres de pagination (obligatoires)
    params = params.set('page', pageRequest.page.toString());
    params = params.set('size', pageRequest.size.toString());
    params = params.set('sortBy', pageRequest.sortBy);
    params = params.set('sortDirection', pageRequest.sortDirection);

    return this.http.get<PagedResponse<Contractor>>(this._url_base, { params });
  }


  /**
   * Recherche par ID spécifique
   */
  getContractorById(id: string): Observable<Contractor> {
    return this.http.get<Contractor>(`${this._url_base}/${id}`);
  }



  /**
   * Utilitaire pour vérifier si des filtres sont appliqués
   */
  hasActiveFilters(filters: SearchFilters): boolean {
    return !!(
      filters.name?.trim() ||
      filters.speciality?.trim() ||
      filters.assignmentId?.trim() ||
      filters.address?.trim()
    );
  }

  /**
   * Utilitaire pour créer des paramètres de page
   */
  createPageRequest(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'id',
    sortDirection: 'asc' | 'desc' = 'asc'
  ): PageRequest {
    return { page, size, sortBy, sortDirection };
  }


  updateInformation(id: string, request: UpdateContractorRequest) {
    return this.http.patch<Contractor>(`${this._url_base}/${id}`, {
      ...request
    }).pipe(
      catchError(error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur service',
          detail: "Erreur lors de la complétion des informations du contractor "
        });
        return of([]);
      })
    )
  }
}
