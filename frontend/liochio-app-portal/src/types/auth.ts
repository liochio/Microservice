export type UserRole = 'PARENT' | 'CHILD' | 'ADMIN';

export interface User {
  id: string;
  username: string;
  fullName: string;
  role: UserRole;
  avatarUrl?: string;
  email?: string;
  parentPinRequired?: boolean;
}

export interface AuthSession {
  token: string;
  refreshToken?: string;
  user: User;
  expiresAt: number;
}
