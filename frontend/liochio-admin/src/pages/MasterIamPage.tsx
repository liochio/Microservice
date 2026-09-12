import React, { useState } from 'react';
import { 
  KeyRound, 
  ShieldCheck, 
  Lock, 
  FolderTree, 
  ToggleRight, 
  Plus, 
  Search, 
  CheckCircle2, 
  Layers, 
  UserPlus, 
  Sliders, 
  Zap, 
  Smartphone,
  Cpu,
  Bot
} from 'lucide-react';
import { Card, Badge } from '../components/common/CardAndBadge';
import { Button } from '../components/common/Button';
import { SUPERADMIN_MENU_TREE } from '../data/superadminMenuData';

export const MasterIamPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'staff' | 'permissions' | 'dynamicMenus' | 'featureFlags'>('staff');
  const [searchTerm, setSearchTerm] = useState('');

  const [staffList] = useState([
    { id: 'sa-01', username: 'superadmin', fullName: 'Root Platform Administrator', role: 'SUPER_ADMIN_TIER_0', mfa: 'TOTP_ACTIVE', lastLogin: '2026-09-10 16:45:00', status: 'ACTIVE' },
    { id: 'sa-02', username: 'sec_officer', fullName: 'Vũ Đức Duy (Chief Security Officer)', role: 'SECURITY_OPERATOR', mfa: 'FIDO2_HARDWARE', lastLogin: '2026-09-10 14:12:33', status: 'ACTIVE' },
    { id: 'sa-03', username: 'audit_lead', fullName: 'Nguyễn Thị Thu (Lead Auditor)', role: 'COMPLIANCE_AUDITOR', mfa: 'TOTP_ACTIVE', lastLogin: '2026-09-09 18:30:11', status: 'ACTIVE' },
  ]);

  const [featureFlags, setFeatureFlags] = useState([
    { key: 'GLOBAL_FEATURE_SMART_PIGGY', name: 'IoT Heo Đất & Rút Tiền Solenoid 2-Phase Saga', desc: 'Bật/tắt toàn sàn module kết nối phần cứng ESP32', enabled: true, icon: Cpu },
    { key: 'GLOBAL_FEATURE_AI_ROBO', name: 'AI Robo-Advisor & KDE Transaction Outliers', desc: 'Động cơ phân tích hành vi chi tiêu và cố vấn 50/30/20', enabled: true, icon: Bot },
    { key: 'GLOBAL_FEATURE_PARENTAL_MATCH', name: 'Phụ Huynh Thưởng Khuyến Khích (+50%/+100%)', desc: 'Tính năng nhân đôi tiền nạp tích lũy cho trẻ em', enabled: true, icon: Smartphone },
    { key: 'GLOBAL_FEATURE_EKYC_OCR', name: 'Nhận Diện Hóa Đơn Mua Sắm & eKYC Tự Động', desc: 'Tích hợp OCR camera bóc tách dữ liệu chi tiêu', enabled: true, icon: Zap },
    { key: 'GLOBAL_FEATURE_MAKER_CHECKER', name: 'Bắt Buộc Phê Duyệt Kép Maker-Checker B2B', desc: 'Mọi thao tác điều chỉnh hạn mức phải qua 2 người', enabled: true, icon: ShieldCheck },
  ]);

  const toggleFeature = (key: string) => {
    setFeatureFlags(featureFlags.map(f => f.key === key ? { ...f, enabled: !f.enabled } : f));
  };

  return (
    <div className="space-y-6 animate-fade-in max-w-7xl">
      {/* Header */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-slate-900/80 border border-slate-800 p-6 rounded-3xl backdrop-blur-xl">
        <div>
          <div className="flex items-center gap-2 font-mono text-xs text-amber-400 font-bold mb-1">
            <KeyRound className="w-4 h-4" />
            <span>SA_MASTER_IAM &bull; IDENTITY, DYNAMIC MENUS & FEATURE FLAGS</span>
          </div>
          <h1 className="text-2xl font-black text-white tracking-tight">
            Quản Trị Định Danh & Phân Quyền Gốc Sàn
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Quản trị tài khoản đặc quyền Root, cây quyền hạn API, bộ thiết kế Cây Menu Động và Cờ Chức Năng (Feature Flags).
          </p>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-800/80 gap-2 pb-2 text-xs font-bold">
        <button
          onClick={() => setActiveTab('staff')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'staff'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <ShieldCheck className="w-4 h-4" />
          <span>SA_IAM_STAFF (Tài Khoản Root / SuperAdmin)</span>
        </button>

        <button
          onClick={() => setActiveTab('permissions')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'permissions'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <Lock className="w-4 h-4" />
          <span>SA_IAM_PERMISSIONS (Cây Quyền Hạn Hệ Thống)</span>
        </button>

        <button
          onClick={() => setActiveTab('dynamicMenus')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'dynamicMenus'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <FolderTree className="w-4 h-4" />
          <span>SA_IAM_DYNAMIC_MENU (Thiết Kế Menu Động)</span>
        </button>

        <button
          onClick={() => setActiveTab('featureFlags')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'featureFlags'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <ToggleRight className="w-4 h-4" />
          <span>SA_IAM_FEATURE_FLAGS (Cờ Chức Năng)</span>
        </button>
      </div>

      {/* TAB 1: STAFF MANAGEMENT */}
      {activeTab === 'staff' && (
        <Card className="p-0 overflow-hidden">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="bg-slate-950/80 text-slate-400 uppercase tracking-wider text-[10px] font-bold border-b border-slate-800 font-mono">
              <tr>
                <th className="py-3.5 px-4">Tài Khoản Operator</th>
                <th className="py-3.5 px-4">Vai Trò Đặc Quyền</th>
                <th className="py-3.5 px-4">Bảo Mật MFA</th>
                <th className="py-3.5 px-4">Đăng Nhập Gần Nhất</th>
                <th className="py-3.5 px-4">Trạng Thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono">
              {staffList.map(s => (
                <tr key={s.id} className="hover:bg-slate-800/30 transition">
                  <td className="py-4 px-4 font-sans">
                    <div className="font-bold text-white text-sm">{s.fullName}</div>
                    <div className="text-[11px] text-amber-400 font-mono">@{s.username} &bull; ID: {s.id}</div>
                  </td>
                  <td className="py-4 px-4 text-slate-200">
                    <Badge variant="primary">{s.role}</Badge>
                  </td>
                  <td className="py-4 px-4 text-emerald-400 font-semibold">{s.mfa}</td>
                  <td className="py-4 px-4 text-slate-400">{s.lastLogin}</td>
                  <td className="py-4 px-4 font-sans">
                    <Badge variant="success">{s.status}</Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}

      {/* TAB 2: MASTER PERMISSIONS */}
      {activeTab === 'permissions' && (
        <Card className="space-y-4">
          <h2 className="text-sm font-bold text-white flex items-center gap-2 pb-3 border-b border-slate-800">
            <Lock className="w-4 h-4 text-amber-400" />
            <span>Kho Lưu Trữ Mã Quyền Hạn (Action & Resource Permissions)</span>
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {[
              { mod: 'TENANT_DOMAIN', perms: ['TENANT:READ', 'TENANT:PROVISION', 'TENANT:FREEZE', 'TENANT:PURGE_DATA'] },
              { mod: 'LEDGER_DOMAIN', perms: ['LEDGER:ENTRY_READ', 'LEDGER:MANUAL_POST', 'LEDGER:REVERSAL_EXECUTE', 'LEDGER:AUDIT_EXPORT'] },
              { mod: 'IOT_HARDWARE', perms: ['IOT:FLEET_TELEMETRY', 'IOT:REMOTE_KILL', 'IOT:FIRMWARE_OTA_ROLLOUT', 'IOT:SOLENOID_TRIGGER'] },
              { mod: 'SECURITY_AML', perms: ['SEC:GLOBAL_BLACKLIST_MODIFY', 'SEC:TOKEN_REVOKE', 'SEC:CIRCUIT_BREAKER_KILL'] },
            ].map(group => (
              <div key={group.mod} className="p-4 bg-slate-950/60 rounded-2xl border border-slate-800 space-y-2.5">
                <div className="font-bold text-xs text-amber-300 font-mono">{group.mod}</div>
                <div className="flex flex-wrap gap-1.5">
                  {group.perms.map(p => (
                    <span key={p} className="px-2 py-1 bg-slate-900 text-[10px] text-slate-300 font-mono rounded-lg border border-slate-800 font-semibold">
                      {p}
                    </span>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* TAB 3: DYNAMIC MENU DESIGNER */}
      {activeTab === 'dynamicMenus' && (
        <Card className="space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-800">
            <div>
              <h2 className="text-sm font-bold text-white flex items-center gap-2">
                <FolderTree className="w-4 h-4 text-indigo-400" />
                <span>Cây Danh Mục Menu Động 3 Cấp Sàn & Doanh Nghiệp (Master Menu Tree)</span>
              </h2>
              <p className="text-xs text-slate-400 mt-0.5">Tự động cấu hình cấu trúc hiển thị trên SuperAdmin, Corp Admin và PWA.</p>
            </div>
            <Badge variant="primary">{SUPERADMIN_MENU_TREE.length} Cụm Modules Gốc</Badge>
          </div>

          <div className="space-y-3">
            {SUPERADMIN_MENU_TREE.map(m => (
              <div key={m.code} className="p-3.5 bg-slate-950/70 border border-slate-800 rounded-xl space-y-2">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2 font-bold text-white text-xs">
                    <span className="text-amber-400 font-mono">[{m.code}]</span>
                    <span>{m.title.vi}</span>
                    <span className="text-slate-500 font-normal">({m.title.en})</span>
                  </div>
                  <Badge variant="info">{m.children?.length || 0} Sub-screens</Badge>
                </div>

                {m.children && (
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-2 pt-2 border-t border-slate-900">
                    {m.children.map(c => (
                      <div key={c.code} className="p-2 bg-slate-900/60 rounded-lg border border-slate-800/80 text-[11px]">
                        <div className="font-bold text-slate-200">{c.title.vi}</div>
                        <div className="text-[9px] font-mono text-amber-400/80 mt-0.5">{c.code}</div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* TAB 4: FEATURE FLAGS */}
      {activeTab === 'featureFlags' && (
        <Card className="space-y-4">
          <h2 className="text-sm font-bold text-white flex items-center gap-2 pb-3 border-b border-slate-800">
            <ToggleRight className="w-4 h-4 text-emerald-400" />
            <span>Cờ Chức Năng Bật / Tắt Toàn Sàn (Global Feature Toggles)</span>
          </h2>

          <div className="space-y-3">
            {featureFlags.map(f => {
              const Icon = f.icon;
              return (
                <div key={f.key} className="flex items-center justify-between p-4 bg-slate-950/70 rounded-2xl border border-slate-800">
                  <div className="flex items-center gap-3.5">
                    <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-amber-400">
                      <Icon className="w-5 h-5" />
                    </div>
                    <div>
                      <div className="font-bold text-white text-xs">{f.name}</div>
                      <div className="text-[11px] text-slate-400 mt-0.5">{f.desc}</div>
                      <div className="text-[9px] font-mono text-slate-500 mt-0.5">{f.key}</div>
                    </div>
                  </div>

                  <button
                    onClick={() => toggleFeature(f.key)}
                    className={`px-4 py-2 rounded-xl text-xs font-bold transition cursor-pointer flex items-center gap-2 ${
                      f.enabled
                        ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40 shadow-sm'
                        : 'bg-slate-900 text-slate-500 border border-slate-800'
                    }`}
                  >
                    <span className={`w-2 h-2 rounded-full ${f.enabled ? 'bg-emerald-400 animate-pulse' : 'bg-slate-600'}`} />
                    <span>{f.enabled ? 'ENABLED' : 'DISABLED'}</span>
                  </button>
                </div>
              );
            })}
          </div>
        </Card>
      )}
    </div>
  );
};
