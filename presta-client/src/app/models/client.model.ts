import { UserDto } from "./user.model";

export interface Client {
  id: string;
  fullName: string;
  address?: string;
  user: UserDto;
}

export interface ClientSearchCriteria {
  name?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}