import React from 'react';
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
  UserCheck
} from 'lucide-react';

export const CorpLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('corp_token');
    localStorage.removeItem('corp_user');
    navigate('/login');
  };

  const navItems = [
    { path: '/', label: 'Tenant Overview', icon: Activity },
    { path: '/approvals', label: 'Maker - Checker Queue', icon: CheckSquare },
    { path: '/customer-onboarding', label: 'Customer 4-Gate Onboarding', icon: UserCheck },
    { path: '/branches', label: 'Branch Network (63 Tỉnh)', icon: MapPin },
    { path: '/staff-rbac', label: 'Staff & RBAC Matrix', icon: Users },
    { path: '/iot-fleet', label: 'Smart Piggy IoT Fleet', icon: Cpu },
    { path: '/branding', label: 'White-Label Branding', icon: Palette },
    { path: '/mail-config', label: 'Mail Server Config', icon: Mail },
  ];

  return (
    <div className="flex min-h-screen bg-slate-950 text-slate-100">
      {/* Sidebar */}
      <aside className="w-64 bg-slate-900/90 border-r border-slate-800/80 flex flex-col justify-between shrink-0">
        <div>
          {/* Tenant Logo / Brand */}
          <div className="p-6 border-b border-slate-800/80 flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-cyan-500 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/20">
              <Building2 className="w-6 h-6 text-white" />
            </div>
            <div>
              <div className="font-extrabold text-sm tracking-wider uppercase text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-400">
                VPBank Piggy
              </div>
              <div className="text-[10px] text-slate-400 font-mono">TENANT: VPB-FINTECH</div>
            </div>
          </div>

          {/* Navigation Links */}
          <nav className="p-4 space-y-1.5">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`flex items-center gap-3 px-4 py-3 rounded-xl font-medium text-xs transition-all ${
                    isActive
                      ? 'bg-gradient-to-r from-cyan-500/20 to-blue-600/20 text-cyan-300 border border-cyan-500/30 shadow-sm'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                  }`}
                >
                  <Icon className={`w-4 h-4 ${isActive ? 'text-cyan-400' : 'text-slate-500'}`} />
                  {item.label}
                </Link>
              );
            })}
          </nav>
        </div>

        {/* Footer info & Logout */}
        <div className="p-4 border-t border-slate-800/80">
          <div className="flex items-center gap-3 px-3 py-2 mb-3 bg-slate-950/60 rounded-xl border border-slate-800">
            <div className="w-2.5 h-2.5 rounded-full bg-cyan-400 animate-pulse" />
            <div className="text-[11px] text-slate-300 font-mono">Tenant Quota: 82% Used</div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl text-xs font-semibold text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 border border-rose-500/20 transition cursor-pointer"
          >
            <LogOut className="w-4 h-4" />
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0">
        <header className="h-16 border-b border-slate-800/80 bg-slate-900/50 backdrop-blur px-8 flex items-center justify-between sticky top-0 z-30">
          <div className="flex items-center gap-2 text-xs text-slate-400 font-mono">
            <Building2 className="w-4 h-4 text-cyan-400" />
            <span>CORP ADMIN CONSOLE &bull; ISOLATED TENANT PARTITION</span>
          </div>
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2 bg-cyan-500/10 border border-cyan-500/30 text-cyan-300 text-xs px-3 py-1 rounded-full font-semibold">
              <span>Corp Administrator</span>
            </div>
            <div className="w-8 h-8 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center font-bold text-xs text-cyan-300">
              VP
            </div>
          </div>
        </header>

        <main className="flex-1 p-8 overflow-y-auto">
          {children}
        </main>
      </div>
    </div>
  );
};
