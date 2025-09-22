export interface UserProfile {
  firstName: string;
  lastName: string;
}

export interface UserProfile {
  username?: string;
  email?: string;
  token?: string;
}


export interface ContactInfo {
  email: string;
  phone?: string;
}

export interface UserDto {
  id: string;
  keycloakId: string;
  firstName: string;
  lastName: string;
  email: string;
}

export interface AuthUser {
  keycloakId: string;
  profile: UserProfile;
  contact: ContactInfo;
  roles: string[];
}

export interface UpdateUserDto {
  firstName?: string;
  lastName?: string;
  enabled?: boolean;
}

export interface UpdatePasswordDto {
  newPassword: string;
}


export type UserRoles = "ADMIN" | "CLIENT" | "CONTRACTOR" ;