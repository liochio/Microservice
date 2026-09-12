import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  ShieldCheck,
  Search,
  ChevronDown,
  ChevronRight,
  LogOut,
  Activity,
  BarChart3,
  HeartPulse,
  DollarSign,
  Building2,
  Users,
  UserCheck,
  Sliders,
  ShieldAlert,
  Database,
  MapPin,
  GitBranch,
  Landmark,
  Coins,
  KeyRound,
  Lock,
  FolderTree,
  ToggleRight,
  BadgePercent,
  SlidersHorizontal,
  Receipt,
  TrendingUp,
  CalendarDays,
  Network,
  Fingerprint,
  MessageSquare,
  Mail,
  ScanLine,
  FileSpreadsheet,
  Shield,
  Key,
  Cpu,
  Ban,
  BellRing,
  ZapOff,
  HardDrive,
  Layers,
  FileCode,
  Radio,
  FileCheck,
  FileText,
  CheckCircle2,
  History,
  Archive,
  ExternalLink,
  Zap
} from 'lucide-react';
import { SUPERADMIN_MENU_TREE, SuperAdminMenuItem } from '../data/superadminMenuData';
import { useAuth } from '../context/AuthContext';

// Icon Map
const ICON_MAP: Record<string, React.FC<{ className?: string }>> = {
  Activity,
  BarChart3,
  HeartPulse,
  DollarSign,
  Building2,
  Users,
  UserCheck,
  Sliders,
  ShieldAlert,
  Database,
  MapPin,
  GitBranch,
  Landmark,
  Coins,
  KeyRound,
  Lock,
  FolderTree,
  ToggleRight,
  BadgePercent,
  SlidersHorizontal,
  Receipt,
  TrendingUp,
  CalendarDays,
  Network,
  Fingerprint,
  MessageSquare,
  Mail,
  ScanLine,
  FileSpreadsheet,
  Shield,
  Key,
  Cpu,
  Ban,
  BellRing,
  ZapOff,
  HardDrive,
  Layers,
  FileCode,
  Radio,
  FileCheck,
  FileText,
  CheckCircle2,
  History,
  Archive,
  ShieldCheck,
};

export const SuperAdminLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [lang, setLang] = useState<'vi' | 'en'>('vi');
  const [searchTerm, setSearchTerm] = useState('');
  const [openMenus, setOpenMenus] = useState<Record<string, boolean>>({
    SA_DASHBOARD: true,
    SA_TENANT_MGMT: true,
  });

  const toggleSubmenu = (code: string) => {
    setOpenMenus(prev => ({ ...prev, [code]: !prev[code] }));
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const [menuTree, setMenuTree] = useState<SuperAdminMenuItem[]>(SUPERADMIN_MENU_TREE);
  const [dbConnected, setDbConnected] = useState<boolean>(false);

  // Fetch Dynamic Menu Tree from Backend REST API (Spring Boot :8081 / Gateway :8080)
  React.useEffect(() => {
    const fetchDynamicMenus = async () => {
      try {
        const res = await fetch(`http://localhost:8080/api/v1/menus/tree?portalType=SUPERADMIN&lang=${lang}`);
        if (res.ok) {
          const json = await res.json();
          if (json?.data && Array.isArray(json.data) && json.data.length > 0) {
            // Backend connected and returned dynamic tree
            setDbConnected(true);
          }
        }
      } catch (err) {
        // Backend not reached, keep fallback cache
        setDbConnected(false);
      }
    };
    fetchDynamicMenus();
  }, [lang]);

  const filteredMenus = menuTree.filter(m => {
    if (!searchTerm) return true;
    const term = searchTerm.toLowerCase();
    const titleMatch = m.title[lang].toLowerCase().includes(term);
    const codeMatch = m.code.toLowerCase().includes(term);
    const childMatch = m.children?.some(
      c => c.title[lang].toLowerCase().includes(term) || c.code.toLowerCase().includes(term)
    );
    return titleMatch || codeMatch || childMatch;
  });

  return (
    <div className="flex h-screen bg-[#070b14] text-slate-100 font-sans overflow-hidden">
      {/* Sidebar */}
      <aside className="w-80 bg-slate-900/95 border-r border-slate-800/80 flex flex-col justify-between shrink-0 select-none z-20">
        <div className="flex flex-col h-full overflow-hidden">
          {/* Logo & Portal Info */}
          <div className="p-5 border-b border-slate-800/80 bg-slate-950/40">
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 rounded-2xl bg-gradient-to-tr from-amber-500 via-rose-500 to-indigo-600 flex items-center justify-center shadow-lg shadow-amber-500/20 border border-white/10 shrink-0">
                <ShieldCheck className="w-6 h-6 text-white" />
              </div>
              <div className="min-w-0">
                <div className="font-black text-sm tracking-wider uppercase text-transparent bg-clip-text bg-gradient-to-r from-amber-400 via-rose-400 to-indigo-400 truncate">
                  LIOCHIO MASTER
                </div>
                <div className="text-[10px] text-amber-400/90 font-mono tracking-wider truncate flex items-center gap-1.5 mt-0.5">
                  <span className={`w-1.5 h-1.5 rounded-full ${dbConnected ? 'bg-emerald-400 animate-pulse' : 'bg-amber-400'}`} />
                  <span>{dbConnected ? 'DB: LIVE CONNECTED' : 'DB: STANDBY CACHE'}</span>
                </div>
              </div>
            </div>

            {/* Quick Search */}
            <div className="mt-4 relative">
              <Search className="w-3.5 h-3.5 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={searchTerm}
                onChange={e => setSearchTerm(e.target.value)}
                placeholder="Tìm mã (VD: SA_TENANT, SA_SEC)..."
                className="w-full pl-9 pr-3 py-1.5 bg-slate-950/80 border border-slate-800 rounded-xl text-[11px] text-slate-200 placeholder-slate-500 focus:outline-none focus:border-amber-500 font-mono"
              />
            </div>
          </div>

          {/* Navigation Menu List */}
          <nav className="flex-1 p-3 space-y-1 overflow-y-auto scrollbar-thin scrollbar-thumb-slate-800">
            {filteredMenus.map(menu => {
              const Icon = ICON_MAP[menu.icon] || Activity;
              const isMainActive = location.pathname.startsWith(menu.path);
              const isOpen = openMenus[menu.code] || searchTerm.length > 0;

              return (
                <div key={menu.code} className="space-y-0.5">
                  <div
                    onClick={() => toggleSubmenu(menu.code)}
                    className={`flex items-center justify-between px-3 py-2 rounded-xl text-xs font-bold transition cursor-pointer group ${
                      isMainActive
                        ? 'bg-amber-500/10 text-amber-300 border border-amber-500/20'
                        : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/50'
                    }`}
                  >
                    <div className="flex items-center gap-2.5 min-w-0">
                      <Icon className={`w-4 h-4 shrink-0 ${isMainActive ? 'text-amber-400' : 'text-slate-500 group-hover:text-slate-300'}`} />
                      <div className="truncate">
                        <span className="block truncate leading-tight">{menu.title[lang]}</span>
                        <span className="text-[9px] font-mono text-slate-500 font-normal">{menu.code}</span>
                      </div>
                    </div>
                    {menu.children && menu.children.length > 0 && (
                      <button type="button" className="p-1 text-slate-500 group-hover:text-slate-300">
                        {isOpen ? <ChevronDown className="w-3.5 h-3.5" /> : <ChevronRight className="w-3.5 h-3.5" />}
                      </button>
                    )}
                  </div>

                  {/* Sub-menu Items */}
                  {isOpen && menu.children && (
                    <div className="pl-4 pr-1 py-1 space-y-0.5 border-l-2 border-slate-800/80 ml-3">
                      {menu.children.map(child => {
                        const ChildIcon = ICON_MAP[child.icon] || ChevronRight;
                        const isChildActive = location.pathname === child.path || (location.pathname === menu.path && child === menu.children?.[0]);

                        return (
                          <Link
                            key={child.code}
                            to={child.path}
                            className={`flex items-center gap-2 px-2.5 py-1.5 rounded-lg text-[11px] font-medium transition ${
                              isChildActive
                                ? 'bg-gradient-to-r from-amber-500/20 to-rose-500/20 text-white font-semibold border border-amber-500/30'
                                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40'
                            }`}
                          >
                            <ChildIcon className={`w-3.5 h-3.5 shrink-0 ${isChildActive ? 'text-amber-400' : 'text-slate-600'}`} />
                            <span className="truncate">{child.title[lang]}</span>
                          </Link>
                        );
                      })}
                    </div>
                  )}
                </div>
              );
            })}
          </nav>
        </div>

        {/* Sidebar Footer */}
        <div className="p-4 border-t border-slate-800/80 bg-slate-950/50">
          <div className="flex items-center justify-between mb-3 text-[10px] font-mono text-slate-400 bg-slate-900 px-3 py-1.5 rounded-xl border border-slate-800">
            <span className="flex items-center gap-1.5 text-emerald-400">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              API GW :8080
            </span>
            <span className="text-slate-500">IAM :8081</span>
          </div>

          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center gap-2 px-4 py-2 rounded-xl text-xs font-bold text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 border border-rose-500/20 transition cursor-pointer"
          >
            <LogOut className="w-3.5 h-3.5" />
            <span>Đăng Xuất SuperAdmin</span>
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Top Navbar */}
        <header className="h-16 border-b border-slate-800/80 bg-slate-900/60 backdrop-blur-md px-6 flex items-center justify-between sticky top-0 z-30 shrink-0">
          <div className="flex items-center gap-3 text-xs font-mono text-slate-300">
            <div className="px-2.5 py-1 rounded-lg bg-amber-500/10 border border-amber-500/30 text-amber-300 font-bold flex items-center gap-1.5">
              <ShieldCheck className="w-3.5 h-3.5" />
              <span>PORTAL_SUPERADMIN</span>
            </div>
            <span className="text-slate-600 hidden md:inline">|</span>
            <span className="text-slate-400 hidden md:inline">Platform Owner & Core Administration Console</span>
          </div>

          <div className="flex items-center gap-3">
            {/* Language Switcher */}
            <div className="flex items-center bg-slate-950 rounded-xl border border-slate-800 p-0.5 text-[11px] font-bold">
              <button
                onClick={() => setLang('vi')}
                className={`px-2 py-1 rounded-lg transition ${lang === 'vi' ? 'bg-amber-500/20 text-amber-300' : 'text-slate-400 hover:text-white'}`}
              >
                VI
              </button>
              <button
                onClick={() => setLang('en')}
                className={`px-2 py-1 rounded-lg transition ${lang === 'en' ? 'bg-amber-500/20 text-amber-300' : 'text-slate-400 hover:text-white'}`}
              >
                EN
              </button>
            </div>

            {/* User Profile Badge */}
            <div className="flex items-center gap-2.5 pl-3 border-l border-slate-800">
              <div className="text-right hidden sm:block">
                <div className="text-xs font-bold text-slate-200">{user?.fullName || 'Root Administrator'}</div>
                <div className="text-[10px] font-mono text-amber-400">SUPER_ADMIN (Tier 0)</div>
              </div>
              <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-amber-500 to-rose-600 flex items-center justify-center font-black text-xs text-white shadow-md border border-white/20">
                SA
              </div>
            </div>
          </div>
        </header>

        {/* Dynamic Page Container */}
        <main className="flex-1 p-6 lg:p-8 overflow-y-auto scrollbar-thin scrollbar-thumb-slate-800">
          {children}
        </main>
      </div>
    </div>
  );
};
