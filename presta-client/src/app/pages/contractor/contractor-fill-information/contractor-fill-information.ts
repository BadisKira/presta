import { Component, inject, OnInit, signal } from '@angular/core';
import { UserManagementService } from '../../../services/userManagement/user-management.service';
import { Router, RouterModule } from '@angular/router';
import { Contractor } from '../../../models/contractor.model';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Assignment } from '../../../models/assignment';
import { AssignmentSelect } from '../../../layout/components/assignment-select/assignment-select';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CheckboxModule } from 'primeng/checkbox';
import { RippleModule } from 'primeng/ripple';
import { ContractorService } from '../../../services/contractor/contractor.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-contractor-fill-information',
  imports: [
    ReactiveFormsModule,
    AssignmentSelect,
    ButtonModule, CheckboxModule, InputTextModule, RouterModule, RippleModule
  ],
  templateUrl: './contractor-fill-information.html'
})
export class ContractorFillInformation implements OnInit {
  private userManagment = inject(UserManagementService);
  private router = inject(Router);
  private connectedUser = signal<Contractor | undefined>(undefined);
  private readonly fb = inject(FormBuilder);
  private contractorService = inject(ContractorService);
  private messageService = inject(MessageService);

  // Form
  readonly searchForm = this.fb.group({
    address: ['', Validators.required],
    assignment: [null as Assignment | null, Validators.required],
    speciality: ['']
  });

  ngOnInit(): void {
    this.userManagment.getCurrentUser().subscribe(
      (data) => {
        if (data.kind === "CONTRACTOR") {
          this.connectedUser.set(data as Contractor);
          this.searchForm.patchValue({
            address: data.address ?? '',
            assignment: data.assignment ?? null,
            speciality: data.speciality ?? ''
          });
        } else {
          this.router.navigate(["/"]);
        }
      }
    )
  }


  fillAssignment(event: Assignment | undefined) {
    this.searchForm.patchValue({
      assignment: event ?? null
    });
  }


  onSubmit() {
    if (this.searchForm.valid && this.connectedUser()) {
      const formValue = this.searchForm.value;
      this.contractorService.updateInformation(this.connectedUser()?.id!!, {
        address: formValue.address!,
        assignmentId: formValue.assignment!.id!!,
        speciality: formValue.speciality || undefined
      }).subscribe({
        next: (result) => {
          this.messageService.add({
            severity: 'success',
            summary: 'Reussite',
            detail: "Modification réussi  "
          });
          this.router.navigate(["/"]);
        },
        error: (err) => console.error('Error', err)
      });
    }
  }




}
