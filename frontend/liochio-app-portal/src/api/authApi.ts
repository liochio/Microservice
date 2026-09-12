import { apiClient } from './client';
import { User, AuthSession, UserRole } from '../types/auth';

export const authApi = {
  login: async (emailOrUsername: string, password?: string, targetRole: UserRole = 'PARENT'): Promise<AuthSession> => {
    const rawInput = emailOrUsername.trim();
    const response = await apiClient.post('/auth/login', {
      email: rawInput,
      username: rawInput,
      password: password || 'Password123!',
      portalType: 'CONSUMER',
    });

    if (response.data && response.data.success && response.data.data) {
      const data = response.data.data;
      const token = data.access_token || data.accessToken;
      
      // Extract role dynamically from backend response
      let detectedRole: UserRole = targetRole;
      const rolesList = data.roles || data.user_roles || [];
      const roleCodes = rolesList.map((r: any) => (r.code || r.name || '').toUpperCase());
      
      if (roleCodes.some((c: string) => c.includes('SUPER_ADMIN') || c.includes('ADMIN'))) {
        detectedRole = 'ADMIN';
      } else if (roleCodes.some((c: string) => c.includes('CHILD'))) {
        detectedRole = 'CHILD';
      } else if (roleCodes.some((c: string) => c.includes('PARENT'))) {
        detectedRole = 'PARENT';
      }

      const realUser: User = {
        id: String(data.user_id || data.userId || data.user?.user_id || '1'),
        username: data.username || data.user?.username || emailOrUsername,
        fullName: data.full_name || data.fullName || data.user?.full_name || (detectedRole === 'ADMIN' ? 'Quản Trị Viên Liochio' : detectedRole === 'CHILD' ? 'Bé Bảo Nam' : 'Trần Văn Phụ Huynh'),
        role: detectedRole,
        avatarUrl: detectedRole === 'ADMIN' ? '🛡️' : detectedRole === 'CHILD' ? '🧒' : '👨‍💼',
        email: data.email || data.user?.email || emailOrUsername,
      };

      return {
        token,
        refreshToken: data.refresh_token || data.refreshToken,
        user: realUser,
        expiresAt: Date.now() + 86400000,
      };
    }

    throw new Error(response.data?.message || 'Đăng nhập không thành công');
  },

  register: async (payload: any): Promise<{ success: boolean; message: string }> => {
    const res = await apiClient.post('/auth/register', payload);
    return res.data;
  },

  verifyOtp: async (username: string, otpCode: string): Promise<{ success: boolean; message: string }> => {
    const res = await apiClient.post('/auth/verify-otp', {
      username: username.trim(),
      otpCode: otpCode.trim(),
      otp_code: otpCode.trim(),
      otp: otpCode.trim(),
    });
    return res.data;
  },

  verifyDeviceOtp: async (username: string, deviceId: string, otpCode: string): Promise<AuthSession> => {
    const res = await apiClient.post('/auth/verify-device-otp', { username, deviceId, otpCode, rememberDevice: true });
    
    if (res.data && res.data.success && res.data.data) {
      const data = res.data.data;
      const token = data.access_token || data.accessToken;
      const realUser: User = {
        id: String(data.user_id || data.userId || data.user?.user_id || '1'),
        username: data.username || data.user?.username || username,
        fullName: data.full_name || data.fullName || data.user?.full_name || 'Người dùng',
        role: 'PARENT',
        avatarUrl: '👨‍💼',
        email: data.email || data.user?.email || username,
      };

      return {
        token,
        refreshToken: data.refresh_token || data.refreshToken,
        user: realUser,
        expiresAt: Date.now() + 86400000,
      };
    }
    throw new Error(res.data?.message || 'Xác thực OTP thất bại');
  },

  getCurrentUser: async (): Promise<User | null> => {
    try {
      const res = await apiClient.get('/auth/me');
      return res.data;
    } catch {
      return null;
    }
  },
};
