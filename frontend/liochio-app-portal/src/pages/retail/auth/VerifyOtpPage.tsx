import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { Button } from '../../../components/common/Button';
import { useToast } from '../../../components/common/Toast';
import { ShieldCheck, ArrowLeft, CheckCircle2 } from 'lucide-react';
import { authApi } from '../../../api/authApi';

export const VerifyOtpPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const initialUsername = searchParams.get('username') || '';
  const initialOtp = searchParams.get('otp') || searchParams.get('token') || '';
  
  const [username, setUsername] = useState<string>(initialUsername);
  const [otpCode, setOtpCode] = useState<string>(initialOtp);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const { success, error } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    if (initialUsername) setUsername(initialUsername);
    if (initialOtp) setOtpCode(initialOtp);
  }, [initialUsername, initialOtp]);

  const handleVerify = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!username || !otpCode) {
      error('Loi', 'Vui long nhap day du Ten dang nhap va Ma OTP 6 so!');
      return;
    }

    setIsLoading(true);
    try {
      await authApi.verifyOtp(username, otpCode);
      success('Kich Hoat Thanh Cong!', 'Tai khoan cua ban da duoc kich hoat. Vui long dang nhap.');
      navigate('/login');
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || 'Ma OTP khong chinh xac hoac da het han.';
      error('Xac Thuc That Bai', errorMsg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="glass-panel p-6 sm:p-8 rounded-3xl border border-slate-800 shadow-2xl space-y-6 text-center">
      <div className="flex justify-center mb-4">
        <div className="w-16 h-16 rounded-full bg-emerald-500/20 flex items-center justify-center">
          <ShieldCheck className="w-8 h-8 text-emerald-400" />
        </div>
      </div>
      
      <div className="space-y-2">
        <h2 className="text-xl font-bold text-white">Xác Thực & Kích Hoạt Tài Khoản</h2>
        <p className="text-xs text-slate-400 px-4">
          Vui lòng nhập mã OTP gồm 6 chữ số được gửi về email của bạn để kích hoạt tài khoản.
        </p>
      </div>

      <form onSubmit={handleVerify} className="space-y-4 text-left">
        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1.5">Tên Đăng Nhập:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-sm text-slate-200 focus:outline-none focus:border-emerald-500 font-mono"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1.5">Mã OTP (6 Số):</label>
          <input
            type="text"
            maxLength={6}
            value={otpCode}
            onChange={(e) => setOtpCode(e.target.value.replace(/\D/g, ''))}
            placeholder="Nhập 6 số OTP (VD: 886699)"
            required
            className="w-full px-4 py-3 rounded-xl bg-slate-900 border border-slate-700 text-xl font-mono font-bold tracking-widest text-center text-emerald-400 focus:outline-none focus:border-emerald-500"
          />
        </div>

        <Button
          type="submit"
          variant="primary"
          size="lg"
          className="w-full mt-2"
          isLoading={isLoading}
          rightIcon={<CheckCircle2 className="w-4 h-4" />}
        >
          Xác Thực & Kích Hoạt Tài Khoản
        </Button>
      </form>

      <div className="pt-2 text-center text-xs text-slate-400">
        <Link to="/login" className="inline-flex items-center gap-1 text-slate-400 hover:text-white transition-colors">
          <ArrowLeft className="w-3.5 h-3.5" /> Quay lại Đăng Nhập
        </Link>
      </div>
    </div>
  );
};
