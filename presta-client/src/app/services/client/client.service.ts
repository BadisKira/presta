import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../../environment';
import { MessageService } from 'primeng/api';
import { catchError, Observable, of, tap, throwError } from 'rxjs';
import { Client, ClientSearchParams } from '../../models/client.model';
import { PagedResponse } from '../../models/pagination.model';

@Injectable({
  providedIn: 'root'
})
@Injectable({
  providedIn: 'root'
})
export class ClientService {
  private http = inject(HttpClient);
  private _url_base = environment.apiUrl + '/api/clients';
  private messageService = inject(MessageService);

  // Méthode simple pour récupérer tous les clients (sans pagination)
  listClients(): Observable<Client[]> {
    return this.http.get<Client[]>(this._url_base).pipe(
      tap(data => data),
      catchError(error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur service',
          detail: "Erreur lors de la récupération des clients"
        });
        return of([]);
      })
    );
  }

  // Nouvelle méthode avec pagination et recherche
  getClients(params?: ClientSearchParams): Observable<PagedResponse<Client>> {
    let httpParams = new HttpParams();

    if (params) {
      if (params.name) {
        httpParams = httpParams.set('name', params.name);
      }
      if (params.page !== undefined) {
        httpParams = httpParams.set('page', params.page.toString());
      }
      if (params.size !== undefined) {
        httpParams = httpParams.set('size', params.size.toString());
      }
      if (params.sortBy) {
        httpParams = httpParams.set('sortBy', params.sortBy);
      }
      if (params.sortDirection) {
        httpParams = httpParams.set('sortDirection', params.sortDirection);
      }
    }

    return this.http.get<PagedResponse<Client>>(this._url_base, { params: httpParams }).pipe(
      catchError(error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur service',
          detail: "Erreur lors de la récupération des clients"
        });
        // Retourner une page vide en cas d'erreur
        return of({
          content: [],
          totalElements: 0,
          totalPages: 0,
          size: params?.size || 10,
          number: params?.page || 0,
          first: true,
          last: true,
          empty: true,
          numberOfElements: 0,
          pageable: {
            pageNumber: params?.page || 0,
            pageSize: params?.size || 10,
            sort: { sorted: false, empty: true, unsorted: true },
            offset: 0,
            paged: true,
            unpaged: false
          },
          sort: { sorted: false, empty: true, unsorted: true }
        } as PagedResponse<Client>);
      })
    );
  }


  deleteClient(clientId: string): Observable<void> {
    return this.http.delete<void>(this._url_base + `/${clientId}`).pipe(
      catchError(error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur service',
          detail: "Erreur lors de la suppression du client"
        });
        return throwError(() => error);
      })
    );
  }

  banUser(userId: string): Observable<Client> {
    return this.http.put<Client>(`${environment.apiUrl}/api/users/${userId}/ban`, {}).pipe(
      // tap(client => {
      //   this.messageService.add({
      //     severity: 'success',
      //     summary: 'Client banni',
      //     detail: `${client.user.profile?.fullName || 'Client'} a été banni avec succès.`
      //   });
      // }),
      catchError(error => {
        console.error(error);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur service',
          detail: "Impossible de bannir l'utilisateur"
        });
        return throwError(() => error);
      })
    );
  }

  unbanUser(userId: string): Observable<Client> {
    return this.http.put<Client>(`${environment.apiUrl}/api/users/${userId}/unban`, {}).pipe(
      catchError(error => {
        console.error(error);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur service',
          detail: "Impossible de débannir l'utilisateur"
        });
        return throwError(() => error);
      })
    );
  }





  // Méthode utilitaire pour chercher des clients par nom
  searchClients(name: string, page: number = 0, size: number = 10): Observable<PagedResponse<Client>> {
    return this.getClients({
      name,
      page,
      size,
      sortBy: 'user.lastName',
      sortDirection: 'asc'
    });
  }

}