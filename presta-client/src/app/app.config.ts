import { APP_INITIALIZER, ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter, withEnabledBlockingInitialNavigation, withInMemoryScrolling } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { KeycloakService } from './services/keycloak/keycloak';
import { httpTokenInterceptor } from './services/interceptor/http-token-interceptor';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { errorInterceptor } from './services/interceptor/error-interceptor';
import { provideAnimations } from '@angular/platform-browser/animations';
import { MessageService } from 'primeng/api';

export function kcFactory(keycloakService: KeycloakService) {
  return () => keycloakService.init();
}
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    //provideRouter(routes),
    provideRouter(routes, withInMemoryScrolling({ anchorScrolling: 'enabled', scrollPositionRestoration: 'enabled' }), withEnabledBlockingInitialNavigation()),
    provideHttpClient(withInterceptors([
      httpTokenInterceptor,errorInterceptor
    ])),
     {
      provide: APP_INITIALIZER,
      useFactory: kcFactory,
      deps: [KeycloakService],
      multi: true
    },
    provideAnimationsAsync(),
    providePrimeNG({
      theme:{
        preset: Aura,
        options: { darkModeSelector: '.app-dark' } 
      }
    }),
    provideAnimations(),
    MessageService
  ]
};
