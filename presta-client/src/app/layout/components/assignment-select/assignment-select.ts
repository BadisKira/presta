import { Component, effect, EventEmitter, inject, input, OnInit, output, signal } from "@angular/core";
import { AssignemntService } from "../../../services/assignment/assignemnt.service";
import { Assignment } from "../../../models/assignment";
import { AutoComplete, AutoCompleteModule, AutoCompleteSelectEvent } from "primeng/autocomplete";


@Component({
  selector: "assignment-select",
  template: `
      <div class="flex flex-col">
          <label for="assignment" class="text-sm font-medium text-gray-700 mb-2">
            {{subtitle()}}
          </label>
          <p-autoComplete formControlName="assignment" 
            [suggestions]="filteredAssignments()"
            (completeMethod)="filterAssignments($event)"
            (onClear)="clearAssignment()"  
              (onSelect)="sendAssignment($event)"  [optionLabel]="'name'" [dataKey]="'id'" 
            placeholder="Sélectionner un service..." [style]="{'width':'100%'}" [inputStyle]="{'width':'100%'}">

            <!-- Rendu des items dans la liste -->
            <ng-template let-a pTemplate="item">
              {{ a?.name }}
            </ng-template>


          </p-autoComplete>
        </div>
    `,
  imports: [AutoComplete, AutoCompleteModule]
})
export class AssignmentSelect implements OnInit {
  private readonly assignmentService = inject(AssignemntService);
  readonly filteredAssignments = signal<Assignment[]>([]);
  assignmentSelected = output<Assignment | undefined>();
  allAssignments = signal<Assignment[]>([]);
  subtitle = input<string>("");


  ngOnInit() {
    this.assignmentService.listAssignements().subscribe(
      (data: Assignment[]) => {
        this.allAssignments.set(data);
        this.filteredAssignments.set(data)
      }
    )

  }


  filterAssignments(event: any): void {
    const query = (event?.query ?? '').toLowerCase();

    console.log(event);

    this.filteredAssignments.set(
      query ? this.allAssignments().filter(a => a.name?.toLowerCase().includes(query))
        : this.allAssignments()
    );
  }

  sendAssignment(event: AutoCompleteSelectEvent) {
    const assignment = event.value as Assignment;
    this.assignmentSelected.emit(assignment);
  }

  clearAssignment() {
    this.assignmentSelected.emit(undefined); 
  }


}