import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Assignment, AssignmentSearchParams } from '../../models/assignment';
import { environment } from '../../../environment';
import { MessageService } from 'primeng/api';
import { catchError, Observable, of, tap } from 'rxjs';
import { PagedResponse } from '../../models/pagination.model';






@Injectable({
  providedIn: 'root'
})
export class AssignemntService {
  private http = inject(HttpClient);
  private _url_base = environment.apiUrl + '/api/assignments';
  private messageService = inject(MessageService)

  listAssignements(): Observable<Assignment[]> {
    return this.http.get<Assignment[]>(this._url_base).pipe(
      tap(data => data),
      catchError(error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur service',
          detail: "Erreur lors de la récupération des services"
        });
        return of([]);
      })
    );
  }

  saveAssignment(assignemnt:Assignment) {
    return this.http.post<Assignment>(this._url_base,{
      ...assignemnt
    }).pipe(
       catchError(error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur service',
          detail: "Erreur lors de la création du nouveau service "
        });
        return of([]);
      })
    )
  }



  updateAssignment(assignemnt:Assignment) {
    return this.http.put<Assignment>(this._url_base,{
      ...assignemnt
    }).pipe(
       catchError(error => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur service',
          detail: "Erreur lors de la modification du service "
        });
        return of([]);
      })
    )
  }

  
  deleteAssignment(assignmentId: string) {
    return this.http.delete<Assignment>(this._url_base + `/${assignmentId}`).pipe(
        catchError(error => {
            this.messageService.add({
                severity: 'error',
                summary: 'Erreur service',
                detail: "Erreur lors de la suppression du service"
            });
            return of(); 
        })
    );
}


getAssignments(params?: AssignmentSearchParams): Observable<PagedResponse<Assignment>> {
    let httpParams = new HttpParams();

    if (params) {
      if (params.searchName) {
        httpParams = httpParams.set('searchName', params.searchName);
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

    return this.http.get<PagedResponse<Assignment>>(this._url_base, { params: httpParams }).pipe(
      catchError(error => {
        console.log(error);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur service',
          detail: "Erreur lors de la récupération des services"
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
        } as PagedResponse<Assignment>);
      })
    );
  }
}
