import { Assignment } from "./assignment";
import { UserDto } from "./user.model";

export interface ContractorDto {
  id: string;
  fullName: string;
  address: string;
  assignment: Assignment;
  speciality: string;
  user: UserDto;
}

export interface UpdateContractorRequest {
  address?: string;
  assignmentId?: string;
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
