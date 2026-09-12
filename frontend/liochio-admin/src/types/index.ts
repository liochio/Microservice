export type UserRole = 'SUPER_ADMIN' | 'CORP_ADMIN' | 'CORP_STAFF' | 'BRANCH_MANAGER' | 'PARENT' | 'CHILD' | 'USER';

export interface User {
  id: string;
  tenantId?: string;
  username: string;
  fullName: string;
  email: string;
  role: UserRole;
  avatarUrl?: string;
}

export type TenantType = 'ENTERPRISE' | 'FINTECH_PARTNER' | 'BANK' | 'SME' | 'INDIVIDUAL';
export type TenantStatus = 'ACTIVE' | 'SUSPENDED' | 'EXPIRED' | 'PENDING_APPROVAL';

export interface Tenant {
  id: string;
  code: string;
  name: string;
  type: TenantType;
  status: TenantStatus;
  domain?: string;
  maxUsersQuota: number;
  maxDevicesQuota: number;
  currentUsersCount: number;
  currentDevicesCount: number;
  contactEmail: string;
  contactPhone: string;
  contractExpiresAt: string;
  createdAt: string;
  features: string[];
}

export interface MailGatewayConfig {
  smtpHost: string;
  smtpPort: number;
  smtpUser: string;
  smtpPassword?: string;
  senderEmail: string;
  senderName: string;
  useTls: boolean;
}

export interface Province {
  id: string;
  code: string;
  name: string;
  region: 'NORTH' | 'CENTRAL' | 'SOUTH';
  totalBranchesCount: number;
  isActive: boolean;
}

export interface MenuNode {
  id: string;
  title: string;
  path: string;
  icon: string;
  rolesAllowed: UserRole[];
  children?: MenuNode[];
}
