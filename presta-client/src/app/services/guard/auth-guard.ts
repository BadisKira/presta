import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { KeycloakService } from '../keycloak/keycloak';

export const authGuard: CanActivateFn = async (route, state) => {
  const keycloakService = inject(KeycloakService);
  const router = inject(Router);

  // Vérifier si connecté
  if (!keycloakService.isLoggedIn()) {
    await keycloakService.login();
    return false;
  }

  // Vérifier les rôles si nécessaire
  const requiredRoles = route.data?.['roles'];
  if (requiredRoles) {
    for (const role of requiredRoles) {
      if (!keycloakService.hasRole(role)) {
        router.navigate(['/unauthorized']);
        return false;
      }
    }
  }

  return true;
};