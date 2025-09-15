import { HttpHeaders, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { KeycloakService } from '../keycloak/keycloak';

export const httpTokenInterceptor: HttpInterceptorFn = (req, next) => {


  const tokenService = inject(KeycloakService);
  const token = tokenService.keycloak.token;
  if (token) {
    const authReq = req.clone({
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`
      })
    });

    return next(authReq);
  }
  return next(req);

};
