import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Button } from '../../../components/common/Button';
import { useToast } from '../../../components/common/Toast';
import { ArrowRight, ArrowLeft } from 'lucide-react';
import { authApi } from '../../../api/authApi';

export const RegisterPage: React.FC = () => {
  const [fullName, setFullName] = useState<string>('');
  const [username, setUsername] = useState<string>('');
  const [email, setEmail] = useState<string>('');
  const [phone, setPhone] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [confirmPassword, setConfirmPassword] = useState<string>('');
  const [dateOfBirth, setDateOfBirth] = useState<string>('');
  const [gender, setGender] = useState<string>('MALE');
  const [role, setRole] = useState<'PARENT' | 'CHILD'>('PARENT');
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const { success, error } = useToast();
  const navigate = useNavigate();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      error('Lỗi nhập liệu', 'Mật khẩu xác nhận không khớp!');
      return;
    }

    setIsLoading(true);
    try {
      await authApi.register({
        full_name: fullName,
        fullName: fullName,
        username,
        email,
        phone: phone,
        phone_number: phone,
        password,
        confirm_password: confirmPassword,
        date_of_birth: dateOfBirth || '2000-01-01',
        gender: gender || 'MALE',
        role,
        roles: [role]
      });
      success('Đăng Ký Thành Công!', 'Vui lòng nhập mã OTP gửi về Email để kích hoạt tài khoản của bạn.');
      navigate(`/verify-otp?username=${encodeURIComponent(username)}`);
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || 'Lỗi hệ thống khi đăng ký.';
      error('Đăng Ký Thất Bại', errorMsg);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="glass-panel p-6 sm:p-8 rounded-3xl border border-slate-800 shadow-2xl space-y-6">
      <div className="space-y-1 text-center">
        <h2 className="text-xl font-bold text-white">Đăng Ký Mở Tài Khoản</h2>
        <p className="text-xs text-slate-400">Vui lòng điền đầy đủ thông tin bảo mật</p>
      </div>

      <form onSubmit={handleRegister} className="space-y-4">
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5">Họ và Tên:</label>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              required
              className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5">Ngày Sinh:</label>
            <input
              type="date"
              value={dateOfBirth}
              onChange={(e) => setDateOfBirth(e.target.value)}
              required
              className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5">Tên Đăng Nhập:</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5">Giới Tính:</label>
            <select
              value={gender}
              onChange={(e) => setGender(e.target.value)}
              className="w-full px-3 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
            >
              <option value="MALE">Nam</option>
              <option value="FEMALE">Nữ</option>
              <option value="OTHER">Khác</option>
            </select>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5">Email:</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5">Số Điện Thoại:</label>
            <input
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              required
              placeholder="VD: 0912345678"
              className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5">Mật Khẩu:</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="Tối thiểu 8 ký tự"
              className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5">Xác Nhận MK:</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
            />
          </div>
        </div>

        <div className="mt-4">
          <label className="block text-xs font-semibold text-slate-400 mb-1.5">Vai Trò Đăng Ký:</label>
          <select
            value={role}
            onChange={(e) => setRole(e.target.value as any)}
            className="w-full px-3 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-sm font-bold text-emerald-400 focus:outline-none focus:border-emerald-500"
          >
            <option value="PARENT">👨‍👩‍👧 Tài Khoản Phụ Huynh</option>
            <option value="CHILD">🧒 Tài Khoản Trẻ Em</option>
          </select>
        </div>

        <Button
          type="submit"
          variant="primary"
          size="lg"
          className="w-full mt-4"
          isLoading={isLoading}
          rightIcon={<ArrowRight className="w-4 h-4" />}
        >
          Hoàn Tất Mở Tài Khoản
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
