import { Component, inject, OnInit } from '@angular/core';
import { AssignemntService } from '../../services/assignment/assignemnt.service';

@Component({
  selector: 'app-admin',
  imports: [],
  templateUrl: './admin.html',
  styleUrl: './admin.scss'
})
export class AdminPage implements OnInit {
  
  private assignmentService = inject(AssignemntService);

  ngOnInit(): void {
  // Cette ligne ne fera qu'une seule requête même si appelée plusieurs fois
  this.assignmentService.listAssignements().subscribe({
    next: (data) => console.log('Assignments:', data),
    error: (error) => console.error('Component error:', error)
  });
}
  
}
