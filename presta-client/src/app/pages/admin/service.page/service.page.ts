import { Component, OnInit, signal, ViewChild } from '@angular/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Table, TableModule } from 'primeng/table';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { ToastModule } from 'primeng/toast';
import { ToolbarModule } from 'primeng/toolbar';
import { RatingModule } from 'primeng/rating';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { SelectModule } from 'primeng/select';
import { RadioButtonModule } from 'primeng/radiobutton';
import { InputNumberModule } from 'primeng/inputnumber';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { InputIconModule } from 'primeng/inputicon';
import { IconFieldModule } from 'primeng/iconfield';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { Assignment, AssignmentSearchParams } from '../../../models/assignment';
import { AssignemntService } from '../../../services/assignment/assignemnt.service';
import { PagedResponse } from '../../../models/pagination.model';

interface Column {
    field: string;
    header: string;
}

interface ExportColumn {
    title: string;
    dataKey: string;
}

@Component({
    selector: 'app-service.page',
    imports: [
        CommonModule,
        TableModule,
        FormsModule,
        ButtonModule,
        RippleModule,
        ToastModule,
        ToolbarModule,
        RatingModule,
        InputTextModule,
        TextareaModule,
        SelectModule,
        RadioButtonModule,
        InputNumberModule,
        DialogModule,
        TagModule,
        InputIconModule,
        IconFieldModule,
        ConfirmDialogModule
    ],
    templateUrl: './service.page.html',
    styleUrl: './service.page.scss',
    providers: [ConfirmationService],
})
export class ServicePage implements OnInit {
    assignments = signal<Assignment[]>([]);
    totalRecords = signal<number>(0);
    loading = signal<boolean>(false);
    // Variables d'état
    assignmentDialog: boolean = false;
    assignment: Assignment = {} as Assignment;
    selectedAssignments: Assignment[] = [];
    submitted: boolean = false;
    // Configuration du tableau
    cols: Column[] = [];
    exportColumns: ExportColumn[] = [];
    @ViewChild('dt') dt!: Table;

    // Paramètres de pagination et recherche
    currentPage: number = 0;
    pageSize: number = 10;
    searchTerm: string = '';
    sortField: string = 'name';
    sortOrder: 'asc' | 'desc' = 'asc';

    constructor(
        private assignmentService: AssignemntService,
        private messageService: MessageService,
        private confirmationService: ConfirmationService
    ) { }

    ngOnInit() {
        this.initializeColumns();
        this.loadAssignments();
    }

    initializeColumns() {
        this.cols = [
            { field: 'name', header: 'Nom' },
            { field: 'description', header: 'Description' }
        ];

        this.exportColumns = this.cols.map((col) => ({
            title: col.header,
            dataKey: col.field
        }));
    }


    loadAssignments(params?: Partial<AssignmentSearchParams>) {
        this.loading.set(true);

        const searchParams: AssignmentSearchParams = {
            page: params?.page ?? this.currentPage,
            size: params?.size ?? this.pageSize,
            searchName: params?.searchName ?? (this.searchTerm || undefined),
            sortBy: params?.sortBy ?? this.sortField,
            sortDirection: params?.sortDirection ?? this.sortOrder
        };


        this.currentPage = searchParams.page!;
        this.pageSize = searchParams.size!;
        if (searchParams.searchName) {
            this.searchTerm = searchParams.searchName;
        }

        this.assignmentService.getAssignments(searchParams).subscribe({
            next: (response: PagedResponse<Assignment>) => {
                this.assignments.set(response.content);
                this.totalRecords.set(response.totalElements);
                this.loading.set(false);
                console.log('Assignments chargés:', response);
            },
            error: (error) => {
                this.loading.set(false);
                console.error('Erreur lors du chargement:', error);
            }
        });
    }


    onPageChange(event: any) {
        this.loadAssignments({
            page: event.page,
            size: event.rows
        });
    }

    onSort(event: any) {
        this.sortField = event.field;
        this.sortOrder = event.order === 1 ? 'asc' : 'desc';

        this.loadAssignments({
            page: 0,
            sortBy: this.sortField,
            sortDirection: this.sortOrder
        });
    }


    onGlobalFilter(table: Table, event: Event) {
        const searchValue = (event.target as HTMLInputElement).value;
        this.searchTerm = searchValue;

        this.loadAssignments({
            page: 0,
            searchName: searchValue || undefined
        });

        table.filterGlobal(searchValue, 'contains');
    }

    exportCSV() {
        this.dt.exportCSV();
    }

    openNew() {
        this.assignment = {} as Assignment;
        this.submitted = false;
        this.assignmentDialog = true;
    }

    openEditDialog(assignment: Assignment) {
        this.assignment = { ...assignment };
        this.submitted = false;
        this.assignmentDialog = true;
    }

    hideDialog() {
        this.assignmentDialog = false;
        this.submitted = false;
        this.assignment = {} as Assignment;
    }

    dialogConfirmation(assignment: Assignment) {
        this.submitted = true;

        if (!assignment.name?.trim()) {
            return;
        }

        if (assignment.id) {
            // Update existing assignment
            this.assignmentService.updateAssignment(assignment).subscribe({
                next: () => {
                    this.messageService.add({
                        severity: 'success',
                        summary: 'Succès',
                        detail: 'Assignment mis à jour avec succès'
                    });
                    this.hideDialog();
                    this.loadAssignments();
                },
                error: (error) => {
                    console.error('Erreur lors de la mise à jour:', error);
                }
            });
        } else {
            // Create new assignment
            this.assignmentService.saveAssignment(assignment).subscribe({
                next: () => {
                    this.messageService.add({
                        severity: 'success',
                        summary: 'Succès',
                        detail: 'Assignment créé avec succès'
                    });
                    this.hideDialog();
                    this.loadAssignments();
                },
                error: (error) => {
                    console.error('Erreur lors de la création:', error);
                }
            });
        }
    }

    openDeleteDialogConfirmation(assignment: Assignment) {
        this.confirmationService.confirm({
            message: 'Êtes-vous sûr de vouloir supprimer le service ' + assignment.name + '?',
            header: 'Confirmer la suppression',
            icon: 'pi pi-exclamation-triangle',
            accept: () => {
                this.deleteAssignment(assignment);
            }
        });
    }

    deleteAssignment(assignment: Assignment) {
        this.assignmentService.deleteAssignment(assignment.id!).subscribe({
            next: () => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Succès',
                    detail: 'Service supprimé avec succès'
                });
                // Recharger la page actuelle
                this.loadAssignments();
            },
            error: (error) => {
                console.error('Erreur lors de la suppression:', error);
            }
        });
    }


    // Méthodes utilitaires pour la recherche
    searchByName(searchTerm: string) {
        this.loadAssignments({
            page: 0,
            searchName: searchTerm || undefined
        });
    }

    clearSearch() {
        this.searchTerm = '';
        this.loadAssignments({
            page: 0,
            searchName: undefined
        });
    }

    // Refresh data
    refreshData() {
        this.loadAssignments();
    }
}