import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext } from '../../../context/AuthContext';
import { UserRole } from '../../../types/auth';
import { Button } from '../../../components/common/Button';
import { useToast } from '../../../components/common/Toast';
import {
  Lock,
  Mail,
  Eye,
  EyeOff,
  ArrowRight,
  ShieldCheck,
  QrCode,
  Sparkles,
  Smartphone,
  CheckCircle2,
  Database,
} from 'lucide-react';

export const LoginPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'PASSWORD' | 'QR'>('PASSWORD');
  const [emailOrUsername, setEmailOrUsername] = useState<string>('retail_user');
  const [password, setPassword] = useState<string>('Password123!');
  const [showPassword, setShowPassword] = useState<boolean>(false);
  const [role, setRole] = useState<UserRole>('PARENT');
  const [rememberMe, setRememberMe] = useState<boolean>(true);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [qrScanned, setQrScanned] = useState<boolean>(false);

  const { login } = useContext(AuthContext);
  const { success, error } = useToast();
  const navigate = useNavigate();

  // 2 Real Database Accounts in liochio_retail_db
  const realDatabaseAccounts = [
    {
      label: '👨‍💼 Phụ Huynh (Retail Parent)',
      email: 'retail_user',
      password: 'Password123!',
      role: 'PARENT' as UserRole,
      desc: 'Ví chính gia đình (25,000,000đ) & Quản lý Heo Đất',
    },
    {
      label: '🧒 Bé Bảo Nam (Child IoT)',
      email: 'be_nam',
      password: 'Password123!',
      role: 'CHILD' as UserRole,
      desc: 'Heo đất thông minh Smart Piggy & Mục tiêu tích lũy',
    },
  ];

  const handleSelectAccount = (acc: (typeof realDatabaseAccounts)[0]) => {
    setEmailOrUsername(acc.email);
    setPassword(acc.password);
    setRole(acc.role);
    success('Đã Chọn Tài Khoản Database', `${acc.label}: ${acc.email}`);
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!emailOrUsername.trim() || !password.trim()) {
      error('Thiếu Thông Tin', 'Vui lòng nhập tài khoản và mật khẩu!');
      return;
    }

    setIsLoading(true);
    try {
      await login(emailOrUsername, password, role);
      success('Đăng Nhập Thành Công!', `Xác thực JWT từ Database Retail FinTech thành công.`);
      navigate('/dashboard');
    } catch (err: any) {
      error('Đăng Nhập Thất Bại', err?.response?.data?.message || err.message || 'Mật khẩu hoặc tài khoản không chính xác.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSimulateQrConfirm = async () => {
    setQrScanned(true);
    setIsLoading(true);
    try {
      await login('retail_user', 'Password123!', 'PARENT');
      success('Xác Thực QR Thành Công!', 'Đã đăng nhập qua Database Retail FinTech.');
      navigate('/dashboard');
    } catch {
      error('Lỗi QR', 'Không thể xác thực QR.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="glass-panel p-6 sm:p-8 rounded-3xl border border-slate-800 shadow-2xl space-y-6">
      {/* Database connection badge */}
      <div className="flex items-center justify-between px-3 py-1.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-xs text-emerald-400">
        <span className="flex items-center gap-1.5 font-semibold">
          <Database className="w-4 h-4" /> Kết nối MySQL Database: liochio_retail_db
        </span>
        <span className="font-mono text-[10px]">Phân Hệ Khách Hàng Cá Nhân</span>
      </div>

      {/* Tab Switcher: Password vs QR Login */}
      <div className="grid grid-cols-2 p-1 bg-slate-900/90 rounded-2xl border border-slate-800 text-xs font-bold">
        <button
          onClick={() => setActiveTab('PASSWORD')}
          className={`py-2.5 rounded-xl flex items-center justify-center gap-2 transition-all ${
            activeTab === 'PASSWORD'
              ? 'bg-emerald-600 text-white shadow-md shadow-emerald-950/40'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Lock className="w-3.5 h-3.5" />
          Tài Khoản MySQL
        </button>

        <button
          onClick={() => setActiveTab('QR')}
          className={`py-2.5 rounded-xl flex items-center justify-center gap-2 transition-all ${
            activeTab === 'QR'
              ? 'bg-cyan-600 text-white shadow-md shadow-cyan-950/40'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <QrCode className="w-3.5 h-3.5" />
          Quét QR Code
        </button>
      </div>

      {activeTab === 'PASSWORD' ? (
        <>
          {/* Quick Select Real Database Users */}
          <div className="space-y-2">
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block">
              Chọn Nhanh Tài Khoản Thực Tế Trong MySQL:
            </span>
            <div className="grid grid-cols-3 gap-2">
              {realDatabaseAccounts.map((acc) => (
                <button
                  key={acc.role}
                  type="button"
                  onClick={() => handleSelectAccount(acc)}
                  className={`p-2 rounded-xl border text-left transition-all ${
                    role === acc.role && emailOrUsername === acc.email
                      ? 'bg-emerald-500/20 border-emerald-500 text-emerald-300 shadow-sm'
                      : 'bg-slate-900/60 border-slate-800 text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
                  }`}
                >
                  <p className="text-xs font-bold leading-tight truncate">{acc.label}</p>
                  <p className="text-[10px] text-slate-500 font-mono mt-0.5 truncate">{acc.email}</p>
                </button>
              ))}
            </div>
          </div>

          <form onSubmit={handleLogin} className="space-y-4 pt-1">
            {/* Email / Username Input */}
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5">
                Email / Tên Đăng Nhập trong Database:
              </label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
                <input
                  type="text"
                  value={emailOrUsername}
                  onChange={(e) => setEmailOrUsername(e.target.value)}
                  placeholder="superadmin@liochio.vn"
                  required
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-900/90 border border-slate-700 text-sm font-medium text-slate-200 focus:outline-none focus:border-emerald-500 transition-colors font-mono"
                />
              </div>
            </div>

            {/* Password Input with Toggle */}
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5">
                Mật Khẩu (Bcrypt):
              </label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Nhập mật khẩu (VD: Password123@)"
                  required
                  className="w-full pl-10 pr-10 py-2.5 rounded-xl bg-slate-900/90 border border-slate-700 text-sm font-medium text-slate-200 focus:outline-none focus:border-emerald-500 transition-colors font-mono"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-3 text-slate-500 hover:text-slate-300"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {/* Role Select */}
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5">
                Phân Quyền Vai Trò (RBAC Mode):
              </label>
              <select
                value={role}
                onChange={(e) => setRole(e.target.value as UserRole)}
                className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-sm font-bold text-emerald-400 focus:outline-none focus:border-emerald-500 cursor-pointer"
              >
                <option value="ADMIN">🛡️ Quản Trị Viên (ADMIN - Toàn quyền kiểm toán & Sổ kép)</option>
                <option value="PARENT">👨‍👩‍👧 Phụ Huynh (PARENT - Quản trị ví & Thưởng nạp +50%)</option>
                <option value="CHILD">🧒 Trẻ Em (CHILD - Gamification nuôi heo & Tích lũy XP)</option>
              </select>
            </div>

            {/* Remember Me */}
            <div className="flex items-center justify-between text-xs text-slate-400 pt-1">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="rounded bg-slate-900 border-slate-700 text-emerald-500 focus:ring-0"
                />
                <span>Ghi nhớ phiên đăng nhập</span>
              </label>
              <span className="text-emerald-400 font-mono text-[11px]">BCRYPT_VERIFIED</span>
            </div>

            {/* Submit Button */}
            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full mt-2"
              isLoading={isLoading}
              rightIcon={<ArrowRight className="w-4 h-4" />}
            >
              Xác Thực & Đăng Nhập Database
            </Button>
          </form>
        </>
      ) : (
        /* QR Code Login Tab */
        <div className="space-y-5 text-center py-2">
          <div className="w-48 h-48 mx-auto p-3 rounded-2xl bg-white flex items-center justify-center shadow-2xl relative">
            <div className="w-full h-full border-4 border-slate-900 rounded-xl flex flex-col items-center justify-center p-2 text-slate-900">
              <QrCode className="w-24 h-24" />
              <span className="text-[9px] font-mono font-bold mt-1">LIOCHIO-AUTH-MYSQL</span>
            </div>
            {qrScanned && (
              <div className="absolute inset-0 bg-emerald-900/90 rounded-2xl flex flex-col items-center justify-center text-white backdrop-blur-sm animate-fade-in">
                <CheckCircle2 className="w-12 h-12 text-emerald-400 mb-1" />
                <span className="text-xs font-bold">Đã Xác Thực!</span>
                <span className="text-[10px] text-slate-300">Đang chuyển trang...</span>
              </div>
            )}
          </div>

          <div className="space-y-1">
            <p className="text-xs font-bold text-slate-200">Quét QR Đăng Nhập An Toàn</p>
            <p className="text-[11px] text-slate-400">
              Tự động cấp phát JWT JTI token và lưu trữ phiên vào bảng user_sessions.
            </p>
          </div>

          <Button
            type="button"
            variant="outline"
            size="md"
            className="w-full"
            isLoading={isLoading}
            onClick={handleSimulateQrConfirm}
            leftIcon={<Smartphone className="w-4 h-4 text-cyan-400" />}
          >
            Giả Lập Quét & Xác Nhận FaceID Từ Mobile
          </Button>
        </div>
      )}

      {/* Footer link */}
      <div className="pt-3 border-t border-slate-800 flex items-center justify-between text-xs text-slate-400">
        <div>
          Chưa có tài khoản?{' '}
          <Link to="/register" className="font-bold text-emerald-400 hover:underline">
            Đăng ký
          </Link>
        </div>
        <div>
          <Link to="/verify-otp" className="font-bold text-cyan-400 hover:underline">
            Kích hoạt OTP
          </Link>
        </div>
      </div>
    </div>
  );
};
