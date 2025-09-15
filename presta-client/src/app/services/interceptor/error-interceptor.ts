import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { Router } from "@angular/router";
import { catchError, throwError } from "rxjs";
import { ExceptionDTO } from "../../models/exception.dto";
import { MessageService } from 'primeng/api';


export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const messageService = inject(MessageService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {

      if (error.error?.code && error.error?.message) {
        const exception = error.error as ExceptionDTO;
        
        messageService.add({
          severity: 'error',
          summary: exception.status,
          detail: exception.message,
          life: 5000
        });

      } else {
        messageService.add({
          severity: 'error',
          summary: `Erreur ${error.status || 'réseau'}`,
          detail: getGenericMessage(error.status),
          life: 5000
        });
      }

      return throwError(() => error);
    })
  );
};

function getGenericMessage(status: number): string {
  const messages: Record<number, string> = {
    0: 'Serveur injoignable',
    400: 'Requête invalide',
    401: 'Non authentifié',
    403: 'Accès refusé',
    404: 'Ressource introuvable',
    500: 'Erreur serveur',
    503: 'Service indisponible'
  };
  return messages[status] || 'Une erreur est survenue';
}