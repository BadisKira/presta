import { Component, inject, signal, computed, effect, DestroyRef } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { toSignal, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, startWith, catchError, of } from 'rxjs';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { Paginator, PaginatorModule } from 'primeng/paginator';
import { CardModule } from 'primeng/card';
import { AvatarModule } from 'primeng/avatar';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { RouterLink } from '@angular/router';

// Models & services
import { Contractor } from '../../../models/contractor.model';
import { Assignment } from '../../../models/assignment';
import { ContractorService, SearchFilters } from '../../../services/contractor/contractor.service';
import { AssignemntService } from '../../../services/assignment/assignemnt.service';
import { AssignmentSelect } from '../assignment-select/assignment-select';

@Component({
  selector: 'app-contractor-search-component',
  imports: [
    CommonModule, ReactiveFormsModule,
    InputTextModule, AutoCompleteModule,
    ButtonModule, ProgressSpinnerModule, AssignmentSelect,
    Paginator, PaginatorModule, TagModule, RouterLink, CardModule, AvatarModule
  ],
  templateUrl: './contractors-search.component.html',
})
export class ContractorsSearchComponent {
  private readonly fb = inject(FormBuilder);
  private readonly contractorService = inject(ContractorService);
  private readonly assignmentService = inject(AssignemntService);
  private readonly destroyRef = inject(DestroyRef);


  // Form
  readonly searchForm = this.fb.group({
    fullName: [''],
    address: [''],
    assignment: [null as Assignment | null],
    speciality: ['']
  });

  // State signals
  readonly loading = signal(false);
  readonly contractors = signal<Contractor[]>([]);
  readonly totalElements = signal(0);
  readonly pageSize = signal(10);
  readonly currentPage = signal(0);

  // Assignments
  readonly assignments = toSignal(
    this.assignmentService.listAssignements().pipe(
      catchError(() => of([]))
    ),
    { initialValue: [] as Assignment[] }
  );

  readonly filteredAssignments = signal<Assignment[]>([]);

  constructor() {
    // Sync assignments
    effect(() => {
      this.filteredAssignments.set(this.assignments());
    }, { allowSignalWrites: true });

    // Search stream
    const searchTrigger$ = toSignal(
      this.searchForm.valueChanges.pipe(
        startWith(this.searchForm.getRawValue()),
        debounceTime(400)
      ),
      { initialValue: this.searchForm.getRawValue() }
    );

    // Auto search on form/pagination changes
    effect(() => {
      const formValue = searchTrigger$();
      const assignmentId = formValue.assignment?.id;

      const filters: SearchFilters = {
        name: formValue.fullName || undefined,
        address: formValue.address || undefined,
        speciality: formValue.speciality || undefined,
        assignmentId: assignmentId ? String(assignmentId) : undefined
      };

      const pageRequest = this.contractorService.createPageRequest(
        this.currentPage(), this.pageSize(), 'fullName', 'asc'
      );

      this.search(filters, pageRequest);
    }, { allowSignalWrites: true });
  }

  private search(filters: SearchFilters, pageRequest: any): void {
    this.loading.set(true);

    this.contractorService.searchContractors(filters, pageRequest)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.contractors.set(res?.content ?? []);
          this.totalElements.set(res?.totalElements ?? 0);
          this.loading.set(false);
        },
        error: () => {
          this.contractors.set([]);
          this.totalElements.set(0);
          this.loading.set(false);
        }
      });
  }

  filterAssignments(event: any): void {
    const query = (event?.query ?? '').toLowerCase();
    const allAssignments = this.assignments();

    this.filteredAssignments.set(
      query ? allAssignments.filter(a => a.name?.toLowerCase().includes(query))
        : allAssignments
    );
  }

  onPageChange(event: any): void {
    this.currentPage.set(event.page ?? 0);
    this.pageSize.set(event.rows ?? 10);
  }

  clearFilters(): void {
    this.searchForm.reset({
      fullName: '',
      address: '',
      assignment: null,
      speciality: ''
    });
    this.currentPage.set(0);
  }

  trackById = (_: number, item: { id: string }): string => item.id;

  initials(name?: string): string {
    if (!name?.trim()) return '?';
    const parts = name.trim().split(/\s+/);
    return parts.length === 1
      ? parts[0][0].toUpperCase()
      : (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }

  // Computed states
  readonly hasResults = computed(() => this.contractors().length > 0);
  readonly statusMessage = computed(() => {
    if (this.loading()) return 'Recherche en cours...';
    if (!this.hasResults() && !this.loading()) return 'Aucun résultat trouvé';
    return null;
  });


  fillAssignment(event: Assignment | undefined) {
    this.searchForm.patchValue({
      assignment: event ?? null
    });
  }


}