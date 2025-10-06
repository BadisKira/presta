import { Component, OnInit, ViewChild, signal } from '@angular/core';
import { Table, TableModule } from 'primeng/table';
import { MessageService, ConfirmationService } from 'primeng/api';
import { Contractor } from '../../../models/contractor.model';
import { ContractorService, SearchFilters } from '../../../services/contractor/contractor.service';
import { PagedResponse } from '../../../models/pagination.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Toolbar } from "primeng/toolbar";
import { Button } from "primeng/button";
import { Tag } from 'primeng/tag';
import { Tooltip } from "primeng/tooltip"
import { ConfirmDialog, ConfirmDialogModule } from "primeng/confirmdialog"
import { InputTextModule } from 'primeng/inputtext';
import { IconField } from "primeng/iconfield";
import { InputIcon } from "primeng/inputicon";


@Component({
  selector: 'app-admin-contractor-page',
  standalone: true,
  imports: [
    FormsModule,
    Tag,
    Tooltip,
    ConfirmDialogModule,
    CommonModule,
    InputTextModule,
    Toolbar,
    Button,
    TableModule,
    IconField,
    InputIcon,
    ConfirmDialog
  ],
  templateUrl: './contractor.page.html',
  providers: [MessageService, ConfirmationService]
})
export class ContractorPage implements OnInit {
  contractors = signal<Contractor[]>([]);
  totalRecords = signal(0);
  loading = signal(false);

  @ViewChild('dt') dt!: Table;

  filters: SearchFilters = {};
  page = 0;
  size = 10;
  sortField = 'fullName';
  sortOrder: 'asc' | 'desc' = 'asc';

  constructor(
    private service: ContractorService,
    private msg: MessageService,
    private confirm: ConfirmationService
  ) { }

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    const pageReq = this.service.createPageRequest(this.page, this.size, this.sortField, this.sortOrder);

    this.service.searchContractors(this.filters, pageReq).subscribe({
      next: (res: PagedResponse<Contractor>) => {
        this.contractors.set(res.content);
        this.totalRecords.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onLazyLoad(e: any) {
    this.page = e.first / e.rows;
    this.size = e.rows;
    if (e.sortField) {
      this.sortField = e.sortField;
      this.sortOrder = e.sortOrder === 1 ? 'asc' : 'desc';
    }
    this.load();
  }

  search(value: string) {
    this.filters.name = value || undefined;
    this.page = 0;
    this.load();
  }

  toggleStatus(c: Contractor) {
    const action = c.user.isActive ? 'bannir' : 'réactiver';
    this.confirm.confirm({
      message: `Voulez-vous ${action} ${c.fullName}?`,
      accept: () => {
        this.msg.add({
          severity: 'success',
          summary: 'Succès',
          detail: `${c.fullName} ${c.user.isActive ? 'banni' : 'réactivé'}`
        });
        this.load();
      }
    });
  }

  exportCSV() {
    this.dt.exportCSV();
  }
}