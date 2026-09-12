import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

interface SuperAdminUser {
  id: string;
  username: string;
  fullName: string;
  role: string;
}

interface AuthContextType {
  user: SuperAdminUser | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (username: string, password?: string) => Promise<boolean>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  token: null,
  isAuthenticated: false,
  login: async () => false,
  logout: () => {},
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('superadmin_token'));
  const [user, setUser] = useState<SuperAdminUser | null>(() => {
    const raw = localStorage.getItem('superadmin_user');
    if (raw) {
      try {
        return JSON.parse(raw);
      } catch {
        return null;
      }
    }
    return null;
  });

  const login = async (username: string, password?: string): Promise<boolean> => {
    if (username.trim().length > 0) {
      try {
        const payload = {
          username: username.trim(),
          password: password || 'Password123!',
          portalType: 'SUPERADMIN',
        };
        let res;
        try {
          res = await axios.post('http://localhost:8081/api/v1/auth/login', payload, {
            headers: { 'Content-Type': 'application/json', 'X-Portal-Type': 'SUPERADMIN' },
            timeout: 5000,
          });
        } catch (err8081: any) {
          if (err8081?.response?.data) {
            throw err8081;
          }
          res = await axios.post('http://localhost:8080/api/v1/auth/login', payload, {
            headers: { 'Content-Type': 'application/json', 'X-Portal-Type': 'SUPERADMIN' },
            timeout: 5000,
          });
        }
        if (res && res.data && (res.data.success || res.data.data)) {
          const d = res.data.data || res.data;
          const newToken = d.accessToken || d.access_token || ('sa_jwt_' + Date.now());
          const adminUser: SuperAdminUser = {
            id: String(d.userId || '1'),
            username: d.username || username,
            fullName: d.fullName || 'Tổng Chỉ Huy SuperAdmin',
            role: 'SUPER_ADMIN',
          };
          localStorage.setItem('superadmin_token', newToken);
          localStorage.setItem('superadmin_user', JSON.stringify(adminUser));
          setToken(newToken);
          setUser(adminUser);
          return true;
        }
      } catch (err: any) {
        console.error('SuperAdmin login error:', err?.response?.data || err.message);
        const serverMsg = err?.response?.data?.message;
        if (serverMsg) {
          throw new Error(serverMsg);
        }
        if (err?.message && (err.message.includes('Network Error') || err.message.includes('ECONNREFUSED'))) {
          throw new Error('Không thể kết nối đến máy chủ Auth (Port 8081). Vui lòng đảm bảo backend đang chạy!');
        }
        throw new Error(err?.message || 'Đăng nhập thất bại: Tài khoản không có quyền truy cập SuperAdmin');
      }
    }
    return false;
  };

  const logout = () => {
    localStorage.removeItem('superadmin_token');
    localStorage.removeItem('superadmin_user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated: !!token, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
