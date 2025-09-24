import { Component, OnInit, ViewChild, signal } from '@angular/core';
import { Table, TableModule } from 'primeng/table';
import { MessageService, ConfirmationService } from 'primeng/api';
import { Client, ClientSearchParams } from '../../../models/client.model';
import { ClientService } from '../../../services/client/client.service';
import { PagedResponse } from '../../../models/pagination.model';
import { Toolbar } from "primeng/toolbar";
import { Button } from "primeng/button";
import { IconField } from "primeng/iconfield";
import { InputIcon } from "primeng/inputicon";
import { Dialog } from "primeng/dialog";
import { CommonModule } from '@angular/common';
import { ConfirmDialog } from "primeng/confirmdialog";
import { InputTextModule } from 'primeng/inputtext';

interface Column {
  field: string;
  header: string;
}

interface ExportColumn {
  title: string;
  dataKey: string;
}

@Component({
  selector: 'app-admin-client.page',
  imports: [
    CommonModule,
    TableModule,
    InputTextModule,
    Toolbar,
    Button,
    TableModule,
    IconField,
    InputIcon,
    ConfirmDialog
  ],
  templateUrl: './client.page.html',
  providers: [ConfirmationService]
})
export class ClientPage implements OnInit {
  // Signals pour la gestion des données
  clients = signal<Client[]>([]);
  totalRecords = signal<number>(0);
  loading = signal<boolean>(false);

  // Variables d'état
  clientDialog: boolean = false;
  client: Partial<Client> = {};
  submitted: boolean = false;

  // Configuration du tableau
  cols: Column[] = [];
  exportColumns: ExportColumn[] = [];
  @ViewChild('dt') dt!: Table;

  // Paramètres de pagination et recherche
  currentPage: number = 0;
  pageSize: number = 10;
  searchTerm: string = '';
  sortField: string = 'user.lastName';
  sortOrder: 'asc' | 'desc' = 'asc';

  constructor(
    private clientService: ClientService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) { }

  ngOnInit() {
    this.initializeColumns();
    this.loadClients();
  }

  initializeColumns() {
    this.cols = [
      { field: 'user.profile.fullName', header: 'Nom complet' },
      { field: 'user.profile.firstName', header: 'Prénom' },
      { field: 'user.profile.lastName', header: 'Nom' },
      { field: 'user.contactInfo.email', header: 'Email' }
    ];

    this.exportColumns = this.cols.map((col) => ({
      title: col.header,
      dataKey: col.field
    }));
  }

  // Méthode principale pour charger les clients avec pagination
  loadClients(params?: Partial<ClientSearchParams>) {
    this.loading.set(true);

    const searchParams: ClientSearchParams = {
      page: params?.page ?? this.currentPage,
      size: params?.size ?? this.pageSize,
      name: params?.name ?? (this.searchTerm || undefined),
      sortBy: params?.sortBy
        ? `user.${params.sortBy.split(".").pop()}`
        : this.sortField,
      sortDirection: params?.sortDirection ?? this.sortOrder
    };

    // Mettre à jour les variables locales
    this.currentPage = searchParams.page!;
    this.pageSize = searchParams.size!;
    if (searchParams.name) {
      this.searchTerm = searchParams.name;
    }

    this.clientService.getClients(searchParams).subscribe({
      next: (response: PagedResponse<Client>) => {
        this.clients.set(response.content);
        this.totalRecords.set(response.totalElements);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);
        console.error('Erreur lors du chargement:', error);
      }
    });
  }

  // Gestion de la pagination côté serveur
  onPageChange(event: any) {
    this.loadClients({
      page: event.page,
      size: event.rows
    });
  }

  // Gestion du tri côté serveur
  onSort(event: any) {
    this.sortField = event.field;
    this.sortOrder = event.order === 1 ? 'asc' : 'desc';

    this.loadClients({
      page: 0, // Reset à la première page lors du tri
      sortBy: this.sortField,
      sortDirection: this.sortOrder
    });
  }

  // Gestion de la recherche globale
  onGlobalFilter(table: Table, event: Event) {
    const searchValue = (event.target as HTMLInputElement).value;
    this.searchTerm = searchValue;

    // Utiliser la recherche côté serveur
    this.loadClients({
      page: 0, // Reset à la première page lors de la recherche
      name: searchValue || undefined
    });

    // Optionnel: garder aussi le filtrage côté client pour une expérience plus fluide
    table.filterGlobal(searchValue, 'contains');
  }

  // Export CSV
  exportCSV() {
    this.dt.exportCSV();
  }

  hideDialog() {
    this.clientDialog = false;
    this.submitted = false;
    this.client = {};
  }


  openBanDialogConfirmation(client: Client) {
    this.confirmationService.confirm({
      message: 'Êtes-vous sûr de vouloir Bannir le client ' + client.user.profile.fullName + '?',
      header: 'Confirmer la suppression',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.banClient(client);
      }
    });
  }

  banClient(client: Client) {
    this.clientService.banUser(client.id!).subscribe({
      next: () => {
       this.messageService.add({
          severity: 'success',
          summary: 'Utilisateur banni',
          detail: `${client.user.profile?.fullName || 'Client'} a été banni avec succès.`
        });
        // Recharger la page actuelle
        this.loadClients();
      },
      error: (error) => {
        console.error('Erreur lors de la suppression:', error);
      }
    });
  }


  openUnBanDialogConfirmation(client: Client) {
    this.confirmationService.confirm({
      message: 'Êtes-vous sûr de vouloir de réactiver le client ' + client.user.profile.fullName + '?',
      header: 'Confirmer la suppression',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.unbanClient(client);
      }
    });
  }

  unbanClient(client: Client) {
    this.clientService.unbanUser(client.id!).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Utilisateur débanni',
          detail: `${client.user.profile?.fullName || 'Utilisateur'} a été débanni avec succès.`
        });
        // Recharger la page actuelle
        this.loadClients();
      },
      error: (error) => {
        console.error('Erreur lors de la suppression:', error);
      }
    });
  }



  // Méthodes utilitaires pour la recherche
  searchByName(searchTerm: string) {
    this.loadClients({
      page: 0,
      name: searchTerm || undefined
    });
  }

  clearSearch() {
    this.searchTerm = '';
    this.loadClients({
      page: 0,
      name: undefined
    });
  }

  // Refresh data
  refreshData() {
    this.loadClients();
  }

  // Méthodes utilitaires pour accéder aux propriétés imbriquées
  getClientFullName(client: Client): string {
    return client.user.profile.fullName || `${client.user.profile.firstName} ${client.user.profile.lastName}`;
  }

  getClientEmail(client: Client): string {
    return client.user.contactInfo.email;
  }

  getClientFirstName(client: Client): string {
    return client.user.profile.firstName;
  }

  getClientLastName(client: Client): string {
    return client.user.profile.lastName;
  }
}