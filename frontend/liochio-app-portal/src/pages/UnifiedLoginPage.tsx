import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { 
  Building2, 
  PiggyBank, 
  ShieldAlert, 
  ArrowRight, 
  Lock, 
  User, 
  KeyRound, 
  Sparkles,
  Layers,
  AlertTriangle
} from 'lucide-react';

export const UnifiedLoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [username, setUsername] = useState('corp_maker');
  const [password, setPassword] = useState('Password123!');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    const cleanUsername = username.trim().toLowerCase();

    // 1. Cross-portal Security Wall: Chặn tuyệt đối superadmin trên cổng người dùng
    if (cleanUsername === 'superadmin') {
      setLoading(false);
      setError('TRUY CẬP BỊ TỪ CHỐI: Tài khoản SuperAdmin chỉ được phép đăng nhập tại Cổng Quản Trị Hạ Tầng Core (http://localhost:5170). Không cho phép đăng nhập trên App Portal!');
      return;
    }

    try {
      // 2. Determine target portal type: CORP for corporate accounts, CONSUMER for retail
      let targetPortal = 'CONSUMER';
      if (['corp_admin', 'corp_maker', 'corp_checker'].includes(cleanUsername)) {
        targetPortal = 'CORP';
      }

      // Call real auth backend
      const payload = {
        username: cleanUsername,
        password: password,
        portalType: targetPortal,
      };

      let res;
      try {
        res = await axios.post('http://localhost:8081/api/v1/auth/login', payload, {
          headers: { 'Content-Type': 'application/json' },
          timeout: 5000,
        });
      } catch (err8081: any) {
        if (err8081?.response?.data?.message) {
          throw new Error(err8081.response.data.message);
        }
        // Fallback to 8080 if 8081 is unreachable
        res = await axios.post('http://localhost:8080/api/v1/auth/login', payload, {
          headers: { 'Content-Type': 'application/json' },
          timeout: 5000,
        });
      }

      if (res && res.data) {
        const d = res.data.data || res.data;
        const roles: string[] = (d.roles || []).map((r: any) => 
          typeof r === 'string' ? r.toUpperCase() : (r.code || r.name || '').toUpperCase()
        );

        // Security check: Chặn nếu role trả về là SuperAdmin
        if (roles.some((r: string) => r.includes('SUPER_ADMIN'))) {
          throw new Error('TRUY CẬP BỊ TỪ CHỐI: Tài khoản Quản trị hạ tầng (SuperAdmin) chỉ được phép truy cập cổng Core :5170!');
        }

        // Determine Domain & RBAC Redirect Path
        let userDomain: 'CORP_PORTAL' | 'RETAIL_FINTECH' = 'RETAIL_FINTECH';
        let redirectPath = '/retail/piggy';

        if (roles.includes('ROLE_CORP_ADMIN')) {
          userDomain = 'CORP_PORTAL';
          redirectPath = '/corp/dashboard';
        } else if (roles.includes('ROLE_MAKER') || roles.includes('ROLE_CHECKER')) {
          userDomain = 'CORP_PORTAL';
          redirectPath = '/corp/approvals';
        } else if (roles.includes('ROLE_CUSTOMER') || roles.includes('ROLE_PARENT') || roles.includes('ROLE_CHILD')) {
          userDomain = 'RETAIL_FINTECH';
          redirectPath = '/retail/piggy';
        }

        const sessionData = {
          userId: d.userId || d.user_id,
          username: d.username || cleanUsername,
          fullName: d.fullName || d.full_name || cleanUsername,
          tenantId: d.tenantId || 'default',
          roles: roles,
          domain: userDomain,
          token: d.accessToken || d.access_token || `jwt_${Date.now()}`,
          accountRef: d.coreAccountRef || (userDomain === 'CORP_PORTAL' ? 'ACC_CORP_OPS' : 'ACC_RETAIL_PARENT')
        };

        // Persist session tokens
        localStorage.setItem('app_token', sessionData.token);
        localStorage.setItem('app_user', JSON.stringify(sessionData));
        localStorage.setItem('corp_token', sessionData.token);
        localStorage.setItem('corp_user', JSON.stringify(sessionData));
        localStorage.setItem('liochio_jwt_token', sessionData.token);
        localStorage.setItem('tenant_id', sessionData.tenantId);

        navigate(redirectPath, { replace: true });
        return;
      }

      throw new Error('Không nhận được dữ liệu xác thực từ hệ thống.');
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại tài khoản và mật khẩu.';
      setError(msg);
      setLoading(false);
    }
  };

  const fillQuickAccount = (uname: string) => {
    setUsername(uname);
    setPassword('Password123!');
    setError(null);
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center items-center p-4 relative overflow-hidden font-sans">
      {/* Glow effects */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-cyan-600/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/4 w-80 h-80 bg-indigo-600/10 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-md bg-slate-900/90 border border-slate-800 rounded-2xl p-8 shadow-2xl backdrop-blur relative z-10">
        {/* Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-gradient-to-tr from-cyan-500 to-indigo-600 shadow-lg shadow-indigo-500/25 mb-4">
            <Layers className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-2xl font-black tracking-tight text-white">
            LIOCHIO APP PORTAL
          </h1>
          <p className="text-xs text-slate-400 mt-1 font-medium">
            Cổng Ứng Dụng Hợp Nhất: Corporate B2B & Retail FinTech IoT
          </p>
        </div>

        {/* Error notification */}
        {error && (
          <div className="mb-6 p-4 rounded-xl bg-rose-500/15 border border-rose-500/30 flex items-start gap-3">
            <ShieldAlert className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
            <div className="text-xs text-rose-300 leading-relaxed">{error}</div>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Tên Tài Khoản / Email
            </label>
            <div className="relative">
              <User className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                placeholder="corp_maker / retail_user..."
                className="w-full pl-10 pr-4 py-2.5 bg-slate-950 border border-slate-700/80 rounded-xl text-slate-100 text-sm focus:outline-none focus:border-cyan-400 focus:ring-1 focus:ring-cyan-400 transition"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Mật Khẩu
            </label>
            <div className="relative">
              <KeyRound className="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                placeholder="••••••••"
                className="w-full pl-10 pr-4 py-2.5 bg-slate-950 border border-slate-700/80 rounded-xl text-slate-100 text-sm focus:outline-none focus:border-cyan-400 focus:ring-1 focus:ring-cyan-400 transition"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 px-4 bg-gradient-to-r from-cyan-500 to-indigo-600 hover:from-cyan-400 hover:to-indigo-500 text-white rounded-xl font-bold text-sm shadow-lg shadow-cyan-500/25 flex items-center justify-center gap-2 transition disabled:opacity-50 mt-2"
          >
            {loading ? (
              <span>Đang xác thực qua Core IAM...</span>
            ) : (
              <>
                <span>Đăng Nhập Vào Hệ Thống</span>
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </form>

        {/* 1-Click Quick Fill Test Accounts */}
        <div className="mt-8 pt-6 border-t border-slate-800">
          <div className="text-[11px] font-semibold uppercase tracking-wider text-slate-400 mb-3 flex items-center gap-1.5">
            <Sparkles className="w-3.5 h-3.5 text-amber-400" />
            <span>Tài khoản kiểm thử nhanh (Password123!)</span>
          </div>
          <div className="grid grid-cols-2 gap-2">
            <button
              onClick={() => fillQuickAccount('corp_admin')}
              className="p-2.5 rounded-lg bg-slate-950/80 border border-slate-800 hover:border-cyan-500/50 text-left transition text-[11px]"
            >
              <div className="font-bold text-cyan-400">🏢 corp_admin</div>
              <div className="text-[10px] text-slate-400">Quản trị DN (Corp + Business)</div>
            </button>
            <button
              onClick={() => fillQuickAccount('corp_maker')}
              className="p-2.5 rounded-lg bg-slate-950/80 border border-slate-800 hover:border-cyan-500/50 text-left transition text-[11px]"
            >
              <div className="font-bold text-cyan-400">🏢 corp_maker</div>
              <div className="text-[10px] text-slate-400">Lập đề xuất chi (Chỉ Corp B2B)</div>
            </button>
            <button
              onClick={() => fillQuickAccount('corp_checker')}
              className="p-2.5 rounded-lg bg-slate-950/80 border border-slate-800 hover:border-cyan-500/50 text-left transition text-[11px]"
            >
              <div className="font-bold text-cyan-400">🏢 corp_checker</div>
              <div className="text-[10px] text-slate-400">Duyệt chi B2B (Chỉ Corp B2B)</div>
            </button>
            <button
              onClick={() => fillQuickAccount('retail_user')}
              className="p-2.5 rounded-lg bg-slate-950/80 border border-slate-800 hover:border-emerald-500/50 text-left transition text-[11px]"
            >
              <div className="font-bold text-emerald-400">🐷 retail_user</div>
              <div className="text-[10px] text-slate-400">Phụ huynh (Chỉ Retail)</div>
            </button>
            <button
              onClick={() => fillQuickAccount('be_nam')}
              className="p-2.5 rounded-lg bg-slate-950/80 border border-slate-800 hover:border-emerald-500/50 text-left transition text-[11px]"
            >
              <div className="font-bold text-emerald-400">🧒 be_nam</div>
              <div className="text-[10px] text-slate-400">Trẻ em tích xu (Chỉ Retail)</div>
            </button>
            <button
              onClick={() => fillQuickAccount('superadmin')}
              className="p-2.5 rounded-lg bg-slate-950/80 border border-rose-900/50 hover:border-rose-500/50 text-left transition text-[11px]"
            >
              <div className="font-bold text-rose-400">⛔ superadmin</div>
              <div className="text-[10px] text-rose-300">Thử test chặn chéo (:5170)</div>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
