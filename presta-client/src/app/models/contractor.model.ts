import { Assignment } from "./assignment";
import { User } from "./user.model";

export interface Contractor {
  id: string;
  fullName: string;
  address: string;
  assignment: Assignment;
  speciality: string;
  user: User;
}

export interface UpdateContractorRequest {
  address: string;
  assignmentId: string;
  speciality?: string;
}

export interface ContractorSearchCriteria {
  name?: string;
  speciality?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}
