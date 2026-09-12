import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Building2, Lock, User, AlertCircle, ArrowRight, Zap, ShieldCheck, UserCheck, CheckSquare } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [tenantCode, setTenantCode] = useState('VPB-FINTECH');
  const [username, setUsername] = useState('corp_admin');
  const [password, setPassword] = useState('Password123!');
  const [role, setRole] = useState<'CORP_ADMIN' | 'MAKER' | 'CHECKER'>('CORP_ADMIN');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const ok = await login(username, role, tenantCode, password);
      if (ok) {
        navigate('/', { replace: true });
      } else {
        setError('Tài khoản hoặc thông tin Tenant không hợp lệ!');
      }
    } catch (err: any) {
      setError(err?.message || 'Lỗi kết nối máy chủ xác thực Doanh Nghiệp!');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = async (quickRole: 'CORP_ADMIN' | 'MAKER' | 'CHECKER', quickUser: string) => {
    setLoading(true);
    setError('');
    try {
      await login(quickUser, quickRole, 'VPB-FINTECH', 'Password123!');
      navigate('/', { replace: true });
    } catch (err: any) {
      setError(err?.message || 'Lỗi đăng nhập Doanh Nghiệp');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4 relative overflow-hidden text-slate-100 font-sans">
      {/* Background glow */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-[400px] h-[400px] bg-blue-600/10 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-md bg-slate-900/95 border border-slate-800 rounded-3xl p-8 shadow-2xl relative z-10 backdrop-blur-xl">
        {/* Header Badge */}
        <div className="text-center mb-6">
          <div className="w-14 h-14 mx-auto rounded-2xl bg-gradient-to-tr from-cyan-500 to-blue-600 flex items-center justify-center shadow-xl shadow-cyan-500/20 mb-3 border border-white/10">
            <Building2 className="w-7 h-7 text-white" />
          </div>
          <h1 className="text-xl font-black text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-400 tracking-tight">
            LIOCHIO CORPORATE SAAS
          </h1>
          <p className="text-xs text-slate-400 mt-1 font-mono">
            Tenant-Scoped B2B Portal &bull; Maker - Checker IAM
          </p>
        </div>

        {error && (
          <div className="mb-4 p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl text-rose-300 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0 text-rose-400" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-3.5">
          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1 flex items-center gap-1.5">
              <Building2 className="w-3.5 h-3.5 text-cyan-400" />
              Mã Định Danh Doanh Nghiệp (Tenant Code)
            </label>
            <input
              type="text"
              required
              value={tenantCode}
              onChange={e => setTenantCode(e.target.value.toUpperCase())}
              className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white uppercase focus:outline-none focus:border-cyan-500 font-mono shadow-inner"
              placeholder="VPB-FINTECH"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1 flex items-center gap-1.5">
              <User className="w-3.5 h-3.5 text-blue-400" />
              Tài Khoản Cán Bộ Doanh Nghiệp
            </label>
            <input
              type="text"
              required
              value={username}
              onChange={e => setUsername(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500 shadow-inner font-mono"
              placeholder="corpadmin"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1 flex items-center gap-1.5">
              <Lock className="w-3.5 h-3.5 text-slate-400" />
              Mật Khẩu Xác Thực
            </label>
            <input
              type="password"
              required
              value={password}
              onChange={e => setPassword(e.target.value)}
              className="w-full px-3.5 py-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-cyan-500 shadow-inner font-mono"
              placeholder="••••••••••••"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-300 mb-1.5">
              Phân Quyền Vai Trò
            </label>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => { setRole('CORP_ADMIN'); setUsername('corp_admin'); }}
                className={`py-2 px-1 text-[11px] font-bold rounded-xl border transition flex flex-col items-center gap-1 ${
                  role === 'CORP_ADMIN'
                    ? 'bg-cyan-500/20 border-cyan-400 text-cyan-300'
                    : 'bg-slate-950/60 border-slate-800 text-slate-400 hover:border-slate-700'
                }`}
              >
                <ShieldCheck className="w-3.5 h-3.5" />
                Corp Admin
              </button>
              <button
                type="button"
                onClick={() => { setRole('MAKER'); setUsername('corp_maker'); }}
                className={`py-2 px-1 text-[11px] font-bold rounded-xl border transition flex flex-col items-center gap-1 ${
                  role === 'MAKER'
                    ? 'bg-blue-500/20 border-blue-400 text-blue-300'
                    : 'bg-slate-950/60 border-slate-800 text-slate-400 hover:border-slate-700'
                }`}
              >
                <UserCheck className="w-3.5 h-3.5" />
                Maker (Tạo)
              </button>
              <button
                type="button"
                onClick={() => { setRole('CHECKER'); setUsername('corp_checker'); }}
                className={`py-2 px-1 text-[11px] font-bold rounded-xl border transition flex flex-col items-center gap-1 ${
                  role === 'CHECKER'
                    ? 'bg-emerald-500/20 border-emerald-400 text-emerald-300'
                    : 'bg-slate-950/60 border-slate-800 text-slate-400 hover:border-slate-700'
                }`}
              >
                <CheckSquare className="w-3.5 h-3.5" />
                Checker (Duyệt)
              </button>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 mt-2 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-bold text-xs shadow-lg shadow-cyan-500/25 transition cursor-pointer flex items-center justify-center gap-2 active:scale-[0.99]"
          >
            {loading ? (
              <span className="animate-pulse">Đang Xác Thực Doanh Nghiệp...</span>
            ) : (
              <>
                <span>Đăng Nhập Cổng Doanh Nghiệp</span>
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </form>

        {/* 1-Click Fast Logins */}
        <div className="mt-5 pt-5 border-t border-slate-800/80 space-y-2">
          <div className="text-[11px] font-bold text-slate-400 text-center mb-1 flex items-center justify-center gap-1.5">
            <Zap className="w-3.5 h-3.5 text-cyan-400" />
            <span>Đăng Nhập Nhanh 1-Click (Môi trường Test)</span>
          </div>
          <div className="grid grid-cols-3 gap-2">
            <button
              type="button"
              onClick={() => handleQuickLogin('CORP_ADMIN', 'corp_admin')}
              className="py-2 px-1 rounded-xl bg-cyan-950/40 hover:bg-cyan-900/50 border border-cyan-800/50 text-cyan-300 font-semibold text-[10px] transition text-center"
            >
              👑 Corp Admin
            </button>
            <button
              type="button"
              onClick={() => handleQuickLogin('MAKER', 'corp_maker')}
              className="py-2 px-1 rounded-xl bg-blue-950/40 hover:bg-blue-900/50 border border-blue-800/50 text-blue-300 font-semibold text-[10px] transition text-center"
            >
              ✍️ Maker
            </button>
            <button
              type="button"
              onClick={() => handleQuickLogin('CHECKER', 'corp_checker')}
              className="py-2 px-1 rounded-xl bg-emerald-950/40 hover:bg-emerald-900/50 border border-emerald-800/50 text-emerald-300 font-semibold text-[10px] transition text-center"
            >
              🛡️ Checker
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
