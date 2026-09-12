import React, { useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import { WebSocketContext } from '../../context/WebSocketContext';
import { useAudio } from '../../hooks/useAudio';
import { UserRole } from '../../types/auth';
import { Badge } from './Badge';
import {
  Radio,
  Volume2,
  VolumeX,
  UserCheck,
  Sparkles,
  LogOut,
  Server,
} from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, role, switchRole, logout } = useContext(AuthContext);
  const { isConnected } = useContext(WebSocketContext);
  const { soundEnabled, setSoundEnabled } = useAudio();
  const navigate = useNavigate();

  const handleRoleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    switchRole(e.target.value as UserRole);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="h-16 border-b border-slate-800/80 bg-[#0c121e]/95 backdrop-blur-md sticky top-0 z-40 px-4 md:px-8 flex items-center justify-between">
      {/* Brand & Live Badge */}
      <div className="flex items-center gap-3">
        <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-emerald-600 to-cyan-500 flex items-center justify-center shadow-lg shadow-emerald-500/20">
          <Sparkles className="w-5 h-5 text-black font-bold" />
        </div>
        <div>
          <h1 className="font-extrabold text-base tracking-tight text-white flex items-center gap-2">
            LIOCHIO <span className="text-xs px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/40">FinTech Core</span>
          </h1>
          <p className="text-[10px] text-slate-400">Smart Piggy Bank & Double-Entry Ledger</p>
        </div>
      </div>

      {/* Right Controls: Backend Status, Role, Audio, User Profile, Logout */}
      <div className="flex items-center gap-2.5 md:gap-3.5">
        {/* Backend Live Status Indicator */}
        <Badge variant={isConnected ? 'emerald' : 'rose'} size="sm" pulse={isConnected}>
          <Server className="w-3.5 h-3.5" />
          <span className="hidden sm:inline">Backend 8089: Online</span>
        </Badge>

        {/* Audio FX Toggle */}
        <button
          onClick={() => setSoundEnabled(!soundEnabled)}
          title={soundEnabled ? 'Tắt âm thanh hiệu ứng' : 'Bật âm thanh hiệu ứng'}
          className="p-2 rounded-lg bg-slate-800/60 hover:bg-slate-700/80 border border-slate-700 text-slate-300 hover:text-emerald-400 transition-colors"
        >
          {soundEnabled ? <Volume2 className="w-4 h-4 text-emerald-400" /> : <VolumeX className="w-4 h-4 text-slate-500" />}
        </button>

        {/* Quick Role Switcher for Demo */}
        <div className="flex items-center gap-1.5 bg-slate-900 border border-slate-700 rounded-xl px-2.5 py-1">
          <UserCheck className="w-4 h-4 text-cyan-400" />
          <span className="text-xs text-slate-400 hidden lg:inline">Vai trò:</span>
          <select
            value={role}
            onChange={handleRoleChange}
            className="bg-transparent text-xs font-bold text-emerald-400 focus:outline-none cursor-pointer"
          >
            <option value="PARENT" className="bg-slate-900 text-slate-100">👨‍👩‍👧 Phụ Huynh (Parent)</option>
            <option value="CHILD" className="bg-slate-900 text-slate-100">🧒 Trẻ Em (Child)</option>
            <option value="ADMIN" className="bg-slate-900 text-slate-100">🛡️ Quản Trị (Admin)</option>
          </select>
        </div>

        {/* User Profile Avatar */}
        <div className="flex items-center gap-2 pl-2 border-l border-slate-800">
          <div className="w-8 h-8 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-sm shadow-inner">
            {user?.avatarUrl || '👤'}
          </div>
          <div className="hidden xl:block text-left">
            <p className="text-xs font-semibold text-slate-200 leading-tight">{user?.fullName}</p>
            <p className="text-[10px] text-emerald-400 font-mono">@{user?.username}</p>
          </div>
        </div>

        {/* PROMINENT LOGOUT BUTTON */}
        <button
          onClick={handleLogout}
          title="Đăng xuất khỏi hệ thống"
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-rose-500/15 hover:bg-rose-500/25 border border-rose-500/40 text-rose-300 hover:text-rose-100 text-xs font-bold transition-all shadow-sm shadow-rose-950/20"
        >
          <LogOut className="w-3.5 h-3.5 text-rose-400" />
          <span>Đăng Xuất</span>
        </button>
      </div>
    </header>
  );
};
