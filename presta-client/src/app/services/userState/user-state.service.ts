import { computed, inject, Injectable, signal } from '@angular/core';
import { UserManagementService } from '../userManagement/user-management.service';
import { AuthUser } from '../../models/user.model';
import { ContractorDto, ContractorSearchCriteria } from '../../models/contractor.model';
import { Page } from '../../models/pagination.model';
import { Client, ClientSearchCriteria } from '../../models/client.model';

@Injectable({
  providedIn: 'root'
})
export class UserStateService {
  private readonly userService = inject(UserManagementService);
  
  // Signals pour l'état
  private readonly _currentUser = signal<AuthUser | null>(null);
  private readonly _contractors = signal<Page<ContractorDto> | null>(null);
  private readonly _clients = signal<Page<Client> | null>(null);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);

  // Computed signals publics
  readonly currentUser = this._currentUser.asReadonly();
  readonly contractors = this._contractors.asReadonly();
  readonly clients = this._clients.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();

  readonly isAuthenticated = computed(() => !!this._currentUser());
  readonly userRoles = computed(() => this._currentUser()?.roles ?? []);
  readonly isAdmin = computed(() => this.userRoles().includes('ADMIN'));
  readonly isContractor = computed(() => this.userRoles().includes('CONTRACTOR'));
  readonly isClient = computed(() => this.userRoles().includes('CLIENT'));

  /**
   * Charge l'utilisateur actuel
   */
  async loadCurrentUser(): Promise<void> {
    this._loading.set(true);
    this._error.set(null);
    
    try {
      const user = await this.userService.getCurrentUser().toPromise();
      this._currentUser.set(user ?? null);
    } catch (error) {
      this._error.set('Erreur lors du chargement de l\'utilisateur');
      console.error('Error loading user:', error);
    } finally {
      this._loading.set(false);
    }
  }

  /**
   * Charge la liste des contractors
   */
  async loadContractors(criteria: ContractorSearchCriteria = {}): Promise<void> {
    this._loading.set(true);
    this._error.set(null);
    
    try {
      const contractors = await this.userService.searchContractors(criteria).toPromise();
      this._contractors.set(contractors ?? null);
    } catch (error) {
      this._error.set('Erreur lors du chargement des contractors');
      console.error('Error loading contractors:', error);
    } finally {
      this._loading.set(false);
    }
  }

  /**
   * Charge la liste des clients
   */
  async loadClients(criteria: ClientSearchCriteria = {}): Promise<void> {
    this._loading.set(true);
    this._error.set(null);
    
    try {
      const clients = await this.userService.searchClients(criteria).toPromise();
      this._clients.set(clients ?? null);
    } catch (error) {
      this._error.set('Erreur lors du chargement des clients');
      console.error('Error loading clients:', error);
    } finally {
      this._loading.set(false);
    }
  }

  /**
   * Réinitialise l'état
   */
  reset(): void {
    this._currentUser.set(null);
    this._contractors.set(null);
    this._clients.set(null);
    this._loading.set(false);
    this._error.set(null);
  }
}

