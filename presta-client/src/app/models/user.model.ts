export interface UserProfile {
  firstName: string;
  lastName: string;
}

export interface UserProfile {
  username?: string;
  email?: string;
  token?: string;
}




// export interface UserDto {
//   id: string;
//   keycloakId: string;
//   firstName: string;
//   lastName: string;
//   email: string;
// }

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

interface KeycloakId {
  value: string;
}

interface Profile {
  firstName: string;
  lastName: string;
  fullName: string;
}

interface ContactInfo {
  email: string;
  value: string;
}

export interface User {
  id: string;
  keycloakId: KeycloakId;
  profile: Profile;
  contactInfo: ContactInfo;
  isActive:boolean
}