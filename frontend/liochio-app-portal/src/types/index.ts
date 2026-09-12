export type UserRole = 'CORP_ADMIN' | 'CORP_STAFF' | 'BRANCH_MANAGER';

export interface User {
  id: string;
  tenantId: string;
  username: string;
  fullName: string;
  email: string;
  role: UserRole;
  avatarUrl?: string;
}

export interface Branch {
  id: string;
  tenantId: string;
  provinceId: string;
  provinceName?: string;
  code: string;
  name: string;
  address: string;
  phone: string;
  managerName?: string;
  status: 'ACTIVE' | 'MAINTENANCE' | 'CLOSED';
  createdAt: string;
}

export interface CorpStaff {
  id: string;
  tenantId: string;
  branchId?: string;
  branchName?: string;
  username: string;
  fullName: string;
  email: string;
  role: UserRole;
  phone: string;
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED';
  createdAt: string;
}

export interface CorpBrandingConfig {
  tenantId: string;
  companyName: string;
  brandTitle: string;
  logoUrl: string;
  primaryColor: string;
  accentColor: string;
  customDomain?: string;
}
