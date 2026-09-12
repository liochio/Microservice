import React, { useContext } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import {
  LayoutDashboard,
  PiggyBank,
  Wallet,
  BrainCircuit,
  BookOpenCheck,
  ShieldAlert,
  HeartHandshake,
  LogOut,
  ChevronRight,
} from 'lucide-react';
import { clsx } from 'clsx';

export const Sidebar: React.FC = () => {
  const { role, user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    {
      to: '/dashboard',
      label: 'Tổng Quan',
      icon: LayoutDashboard,
      roles: ['PARENT', 'CHILD', 'ADMIN'],
    },
    {
      to: '/piggy',
      label: 'Heo Đất & Rút 2 Pha',
      icon: PiggyBank,
      roles: ['PARENT', 'CHILD', 'ADMIN'],
      badge: 'IoT Core',
    },
    {
      to: '/wallets',
      label: 'Quản Lý Ví (Closed-Loop)',
      icon: Wallet,
      roles: ['PARENT', 'CHILD', 'ADMIN'],
    },
    {
      to: '/ai',
      label: 'AI Robo-Advisor & KDE',
      icon: BrainCircuit,
      roles: ['PARENT', 'CHILD', 'ADMIN'],
      badge: 'Gaussian AI',
    },
    {
      to: '/ledger',
      label: 'Sổ Cái Kép (Ledger)',
      icon: BookOpenCheck,
      roles: ['PARENT', 'ADMIN'],
      badge: 'Delta = 0',
    },
    {
      to: '/security',
      label: 'An Ninh & Watchdog',
      icon: ShieldAlert,
      roles: ['PARENT', 'ADMIN'],
      badge: 'MPU6050',
    },
    {
      to: '/parental',
      label: 'Kiểm Soát Cha Mẹ',
      icon: HeartHandshake,
      roles: ['PARENT', 'ADMIN'],
      badge: '+50% Bonus',
    },
  ];

  const visibleItems = navItems.filter((item) => role === 'ADMIN' || item.roles.includes(role));

  return (
    <aside className="w-64 border-r border-slate-800/80 bg-[#0c121e]/95 flex flex-col justify-between shrink-0 min-h-[calc(100vh-4rem)]">
      {/* Navigation List */}
      <div className="p-4 space-y-1.5">
        <div className="px-3 py-2 text-[11px] font-bold uppercase tracking-wider text-slate-500 flex items-center justify-between">
          <span>Phân Hệ RBAC</span>
          <span className="text-[10px] px-1.5 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-mono">
            {role}
          </span>
        </div>

        {visibleItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                clsx(
                  'flex items-center justify-between px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all group',
                  isActive
                    ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 shadow-lg shadow-emerald-950/20'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
                )
              }
            >
              <div className="flex items-center gap-3">
                <Icon className="w-4 h-4 text-slate-400 group-hover:text-emerald-400 transition-colors" />
                <span>{item.label}</span>
              </div>
              {item.badge ? (
                <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-900/60 text-emerald-300 font-mono border border-emerald-700/40">
                  {item.badge}
                </span>
              ) : (
                <ChevronRight className="w-3.5 h-3.5 text-slate-600 opacity-0 group-hover:opacity-100 transition-opacity" />
              )}
            </NavLink>
          );
        })}
      </div>

      {/* Role info card & Logout button */}
      <div className="p-4 border-t border-slate-800/80 space-y-3">
        <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span>Đang đăng nhập:</span>
            <span className="font-bold text-emerald-400">@{user?.username}</span>
          </div>
          <p className="text-[11px] text-slate-500 mt-1">
            {role === 'PARENT' && '👨‍👩‍👧 Quyền Phụ Huynh: Quản trị gia đình & PIN'}
            {role === 'CHILD' && '🧒 Quyền Con Cái: Gamification tích điểm'}
            {role === 'ADMIN' && '🛡️ Quyền Quản Trị: Toàn quyền Audit Sổ Kép & Rủi Ro'}
          </p>
        </div>

        <button
          onClick={handleLogout}
          className="w-full flex items-center justify-center gap-2 px-3 py-2.5 text-xs font-bold text-rose-300 hover:text-rose-100 bg-rose-500/10 hover:bg-rose-500/20 rounded-xl transition-all border border-rose-500/30 shadow-md shadow-rose-950/30"
        >
          <LogOut className="w-4 h-4 text-rose-400" />
          <span>Đăng Xuất Khỏi Hệ Thống</span>
        </button>
      </div>
    </aside>
  );
};
