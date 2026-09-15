import React, { createContext, useContext, useState } from 'react';
import axios from 'axios';

export interface CorpUser {
  id: string;
  tenantId: string;
  tenantCode: string;
  username: string;
  fullName: string;
  role: 'CORP_ADMIN' | 'MAKER' | 'CHECKER';
  avatarUrl?: string;
  branch?: string;
  coreAccountRef?: string;
}

interface AuthContextType {
  user: CorpUser | null;
  role?: any;
  switchRole: (newRole: any) => void;
  token: string | null;
  isAuthenticated: boolean;
  login: (username: string, role?: any, tenantCode?: any, password?: any) => Promise<boolean>;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType>({
  user: null,
  token: null,
  isAuthenticated: false,
  role: "CORP_ADMIN",
  switchRole: () => {},
  login: async () => false,
  logout: () => {},
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('corp_token'));
  const [user, setUser] = useState<CorpUser | null>(() => {
    const raw = localStorage.getItem('corp_user');
    if (raw) {
      try {
        return JSON.parse(raw);
      } catch {
        return null;
      }
    }
    return null;
  });

  const login = async (
    username: string,
    role: any = 'CORP_ADMIN',
    tenantCode: any = 'VPB-FINTECH',
    password?: any
  ): Promise<boolean> => {
    if (username.trim().length > 0) {
      try {
        const payload = {
          username: username.trim(),
          password: password || 'Password123!',
          portalType: 'CORP',
        };
        let res;
        try {
          res = await axios.post('http://localhost:8080/api/v1/auth/login', payload, {
            headers: { 'Content-Type': 'application/json', 'X-Portal-Type': 'CORP' },
            timeout: 5000,
          });
        } catch {
          res = await axios.post('http://localhost:8081/api/v1/auth/login', payload, {
            headers: { 'Content-Type': 'application/json', 'X-Portal-Type': 'CORP' },
            timeout: 5000,
          });
        }
        if (res && res.data && (res.data.success || res.data.data)) {
          const d = res.data.data || res.data;
          const newToken = d.accessToken || d.access_token || ('corp_jwt_' + Date.now());
          
          let detectedRole: 'CORP_ADMIN' | 'MAKER' | 'CHECKER' = role;
          const roles = (d.roles || []).map((r: string) => r.toUpperCase());
          if (roles.some((r: string) => r.includes('MAKER'))) detectedRole = 'MAKER';
          else if (roles.some((r: string) => r.includes('CHECKER'))) detectedRole = 'CHECKER';
          else if (roles.some((r: string) => r.includes('CORP_ADMIN') || r.includes('ADMIN'))) detectedRole = 'CORP_ADMIN';

          const corpUser: CorpUser = {
            id: String(d.userId || 'corp-1'),
            tenantId: d.tenantId || 'TENANT_001',
            tenantCode: tenantCode || 'VPB-FINTECH',
            username: d.username || username,
            fullName: d.fullName || (detectedRole === 'CORP_ADMIN' ? 'Trần Doanh Nghiệp (Corp Admin)' : detectedRole === 'MAKER' ? 'Lê Khởi Tạo (Maker)' : 'Phạm Kiểm Soát (Checker)'),
            role: detectedRole,
            branch: 'Hội sở chính VPBank Digital',
            coreAccountRef: d.coreAccountRef || 'ACC_CORP_001',
          };
          localStorage.setItem('corp_token', newToken);
          localStorage.setItem('corp_user', JSON.stringify(corpUser));
          localStorage.setItem('tenant_id', corpUser.tenantId);
          setToken(newToken);
          setUser(corpUser);
          return true;
        }
      } catch (err: any) {
        console.error('Corp login error:', err?.response?.data || err.message);
        throw new Error(err?.response?.data?.message || 'Đăng nhập thất bại: Tài khoản không có quyền truy cập Cổng Doanh Nghiệp');
      }
    }
    return false;
  };

  const logout = () => {
    localStorage.removeItem('corp_token');
    localStorage.removeItem('corp_user');
    localStorage.removeItem('tenant_id');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, role: user?.role || 'CORP_ADMIN', switchRole: () => {}, token, isAuthenticated: !!token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
