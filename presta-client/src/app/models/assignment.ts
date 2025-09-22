export interface Assignment {
  id?: string;
  name?: string;
  description?: string;
}


export interface AssignmentSearchParams {
  searchName?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}



export type CreateAssignmentRequest = {
  name: string,
  description: string
}