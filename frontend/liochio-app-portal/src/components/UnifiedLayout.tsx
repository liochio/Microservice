import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { 
  Building2, 
  MapPin, 
  Users, 
  Cpu, 
  Palette, 
  Mail, 
  LogOut, 
  Activity, 
  CheckSquare, 
  UserCheck,
  PiggyBank,
  Wallet,
  Target,
  ShieldCheck,
  Bot,
  Layers,
  ChevronRight,
  Shield
} from 'lucide-react';

interface UnifiedLayoutProps {
  children: React.ReactNode;
}

export const UnifiedLayout: React.FC<UnifiedLayoutProps> = ({ children }) => {
  const location = useLocation();
  const navigate = useNavigate();

  // Retrieve stored user session
  const storedUserJson = localStorage.getItem('app_user') || localStorage.getItem('corp_user') || '{}';
  let currentUser: any = {};
  try {
    currentUser = JSON.parse(storedUserJson);
  } catch (e) {
    currentUser = { username: 'Guest', roles: ['ROLE_CUSTOMER'] };
  }

  const userRoles: string[] = currentUser.roles || [];

  // Compute allowed workspaces strictly by role
  const allowedWorkspaces: ('corp' | 'business' | 'retail')[] = [];
  if (userRoles.includes('ROLE_CORP_ADMIN')) {
    allowedWorkspaces.push('corp', 'business');
  } else if (userRoles.includes('ROLE_MAKER') || userRoles.includes('ROLE_CHECKER')) {
    allowedWorkspaces.push('corp');
  } else if (userRoles.includes('ROLE_CUSTOMER') || userRoles.includes('ROLE_PARENT') || userRoles.includes('ROLE_CHILD')) {
    allowedWorkspaces.push('retail');
  } else {
    // Fallback based on domain
    if (currentUser.domain === 'CORP_PORTAL') {
      allowedWorkspaces.push('corp');
    } else {
      allowedWorkspaces.push('retail');
    }
  }

  // Determine active workspace from URL path
  let activeWorkspace: 'corp' | 'retail' | 'business' = allowedWorkspaces[0] || 'retail';
  if (location.pathname.startsWith('/corp')) {
    activeWorkspace = 'corp';
  } else if (location.pathname.startsWith('/business')) {
    activeWorkspace = 'business';
  } else if (location.pathname.startsWith('/retail')) {
    activeWorkspace = 'retail';
  }

  // Auto redirect if user accidentally lands on an unauthorized workspace
  React.useEffect(() => {
    if (allowedWorkspaces.length > 0 && !allowedWorkspaces.includes(activeWorkspace)) {
      if (allowedWorkspaces.includes('corp')) {
        navigate(userRoles.includes('ROLE_CORP_ADMIN') ? '/corp/dashboard' : '/corp/approvals', { replace: true });
      } else if (allowedWorkspaces.includes('retail')) {
        navigate('/retail/piggy', { replace: true });
      }
    }
  }, [activeWorkspace, allowedWorkspaces, navigate, userRoles]);

  const handleLogout = () => {
    localStorage.removeItem('app_token');
    localStorage.removeItem('app_user');
    localStorage.removeItem('corp_token');
    localStorage.removeItem('corp_user');
    localStorage.removeItem('liochio_jwt_token');
    navigate('/login');
  };

  // Nav menus by workspace
  const isCorpAdmin = userRoles.includes('ROLE_CORP_ADMIN');
  const isChild = userRoles.includes('ROLE_CHILD') && !userRoles.includes('ROLE_PARENT');

  const corpNavItems = [
    { path: '/corp/approvals', label: 'Maker - Checker Queue', icon: CheckSquare, badge: 'Hot' },
    { path: '/corp/dashboard', label: 'Bàn Làm Việc B2B', icon: Activity },
    { path: '/corp/branches', label: 'Mạng Lưới Chi Nhánh', icon: MapPin },
    ...(isCorpAdmin ? [{ path: '/corp/staff-rbac', label: 'Phân Quyền Nhân Viên', icon: Users }] : []),
  ];

  const retailNavItems = [
    { path: '/retail/piggy', label: 'Heo Đất Thông Minh IoT', icon: PiggyBank, badge: 'Live' },
    { path: '/retail/wallets', label: 'Ví & Sổ Cái Kép', icon: Wallet },
    { path: '/retail/goals', label: 'Hũ Tiết Kiệm & Nhiệm Vụ', icon: Target },
    ...(!isChild ? [{ path: '/retail/parental', label: 'Giám Sát Phụ Huynh', icon: ShieldCheck }] : []),
    { path: '/retail/ai', label: 'Trợ Lý AI Tài Chính', icon: Bot },
  ];

  const businessNavItems = [
    { path: '/business/iot-fleet', label: 'Smart Piggy IoT Fleet', icon: Cpu },
    { path: '/business/customer-onboarding', label: 'Duyệt Khách Hàng eKYC', icon: UserCheck },
    { path: '/business/branding', label: 'Nhận Diện Thương Hiệu', icon: Palette },
    { path: '/business/mail-config', label: 'Cấu Hình Mail Server', icon: Mail },
  ];

  let currentNavItems = retailNavItems;
  if (activeWorkspace === 'corp') currentNavItems = corpNavItems;
  if (activeWorkspace === 'business') currentNavItems = businessNavItems;

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 overflow-hidden font-sans">
      {/* Sidebar */}
      <aside className="w-64 bg-slate-900/95 border-r border-slate-800 flex flex-col justify-between shrink-0">
        <div>
          {/* Logo & Platform Info */}
          <div className="p-5 border-b border-slate-800/80 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-indigo-500/20">
              <Layers className="w-6 h-6 text-white" />
            </div>
            <div>
              <div className="font-black text-sm tracking-wider uppercase text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-indigo-300">
                Liochio Unified
              </div>
              <div className="text-[10px] text-slate-400 font-mono tracking-tight">APP PORTAL :5173</div>
            </div>
          </div>

          {/* RBAC Workspace Switcher in Sidebar */}
          <div className="p-3 border-b border-slate-800/50">
            <div className="text-[11px] font-semibold uppercase tracking-wider text-slate-400 px-2 mb-2">
              Không Gian Làm Việc
            </div>
            {allowedWorkspaces.length > 1 ? (
              <div className={`grid grid-cols-${allowedWorkspaces.length} gap-1 bg-slate-950/60 p-1 rounded-lg border border-slate-800`}>
                {allowedWorkspaces.includes('corp') && (
                  <button
                    onClick={() => navigate('/corp/dashboard')}
                    className={`py-1.5 px-1 text-xs rounded font-medium transition flex flex-col items-center gap-0.5 ${
                      activeWorkspace === 'corp' 
                        ? 'bg-cyan-600/90 text-white shadow-sm' 
                        : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                    }`}
                  >
                    <Building2 className="w-3.5 h-3.5" />
                    <span className="text-[10px]">Corp B2B</span>
                  </button>
                )}
                {allowedWorkspaces.includes('business') && (
                  <button
                    onClick={() => navigate('/business/iot-fleet')}
                    className={`py-1.5 px-1 text-xs rounded font-medium transition flex flex-col items-center gap-0.5 ${
                      activeWorkspace === 'business' 
                        ? 'bg-indigo-600/90 text-white shadow-sm' 
                        : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                    }`}
                  >
                    <Cpu className="w-3.5 h-3.5" />
                    <span className="text-[10px]">Business</span>
                  </button>
                )}
              </div>
            ) : (
              <div className="px-3 py-2 rounded-lg bg-slate-950/80 border border-slate-800/80 flex items-center gap-2.5">
                {allowedWorkspaces[0] === 'retail' && (
                  <>
                    <div className="w-7 h-7 rounded-lg bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0">
                      <PiggyBank className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="text-xs font-bold text-emerald-400">Khách Hàng Cá Nhân</div>
                      <div className="text-[9px] text-slate-400 font-mono">RETAIL & HEO ĐẤT IOT</div>
                    </div>
                  </>
                )}
                {allowedWorkspaces[0] === 'corp' && (
                  <>
                    <div className="w-7 h-7 rounded-lg bg-cyan-500/20 text-cyan-400 flex items-center justify-center shrink-0">
                      <Building2 className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="text-xs font-bold text-cyan-400">Doanh Nghiệp B2B</div>
                      <div className="text-[9px] text-slate-400 font-mono">MAKER - CHECKER QUEUE</div>
                    </div>
                  </>
                )}
              </div>
            )}
          </div>

          {/* Navigation Links */}
          <nav className="p-3 space-y-1 overflow-y-auto max-h-[calc(100vh-280px)]">
            <div className="px-3 py-1.5 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
              {activeWorkspace === 'corp' && 'Doanh Nghiệp (Maker - Checker)'}
              {activeWorkspace === 'retail' && 'Cá Nhân & Heo Đất IoT'}
              {activeWorkspace === 'business' && 'Vận Hành Ứng Dụng'}
            </div>

            {currentNavItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`flex items-center justify-between px-3 py-2.5 rounded-lg text-xs font-medium transition-all group ${
                    isActive
                      ? 'bg-gradient-to-r from-cyan-500/20 to-indigo-500/20 text-cyan-400 border border-cyan-500/30'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                  }`}
                >
                  <div className="flex items-center gap-2.5">
                    <Icon className={`w-4 h-4 transition ${isActive ? 'text-cyan-400' : 'text-slate-400 group-hover:text-slate-200'}`} />
                    <span>{item.label}</span>
                  </div>
                  {item.badge && (
                    <span className="px-1.5 py-0.5 text-[9px] rounded-full bg-cyan-500/20 text-cyan-300 font-bold border border-cyan-500/30">
                      {item.badge}
                    </span>
                  )}
                </Link>
              );
            })}
          </nav>
        </div>

        {/* User profile & Logout */}
        <div className="p-4 border-t border-slate-800/80 bg-slate-950/40">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5 overflow-hidden">
              <div className="w-8 h-8 rounded-lg bg-cyan-600/30 border border-cyan-500/40 flex items-center justify-center font-bold text-xs text-cyan-300 shrink-0">
                {(currentUser.username || 'U')[0].toUpperCase()}
              </div>
              <div className="truncate">
                <div className="text-xs font-semibold text-slate-200 truncate">{currentUser.username || 'User'}</div>
                <div className="text-[10px] text-slate-400 truncate">{currentUser.roles?.[0] || 'ROLE_USER'}</div>
              </div>
            </div>
            <button
              onClick={handleLogout}
              className="p-1.5 rounded-lg hover:bg-rose-500/20 text-slate-400 hover:text-rose-400 transition"
              title="Đăng xuất"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top Header */}
        <header className="h-14 border-b border-slate-800 bg-slate-900/60 backdrop-blur px-6 flex items-center justify-between shrink-0">
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-1.5 text-xs text-slate-400">
              <span className="font-semibold text-slate-300 uppercase">{activeWorkspace}</span>
              <ChevronRight className="w-3.5 h-3.5" />
              <span className="text-cyan-400 font-mono">
                {location.pathname}
              </span>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2 px-3 py-1 rounded-full bg-slate-800/80 border border-slate-700/80 text-xs">
              <div className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              <span className="text-[11px] text-slate-300 font-mono">Core IAM & Ledger Sync: ACTIVE</span>
            </div>

            <a
              href="http://localhost:5170"
              target="_blank"
              rel="noreferrer"
              className="flex items-center gap-1.5 text-xs text-slate-400 hover:text-cyan-300 transition"
            >
              <Shield className="w-3.5 h-3.5" />
              <span>Core SuperAdmin (:5170)</span>
            </a>
          </div>
        </header>

        {/* Page View Body */}
        <main className="flex-1 overflow-y-auto p-6">
          {children}
        </main>
      </div>
    </div>
  );
};
