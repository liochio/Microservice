import React, { useState } from 'react';
import { 
  Building2, 
  Users, 
  UserCheck, 
  Sliders, 
  ShieldAlert, 
  Plus, 
  Search, 
  CheckCircle2, 
  XCircle, 
  Cpu, 
  ShieldCheck, 
  AlertTriangle,
  Lock,
  Unlock,
  Trash2,
  RefreshCw
} from 'lucide-react';
import { Card, Badge } from '../components/common/CardAndBadge';
import { Button } from '../components/common/Button';
import { Modal } from '../components/common/Modal';

interface Tenant {
  id: string;
  code: string;
  name: string;
  type: 'COMMERCIAL_BANK' | 'ENTERPRISE_FINTECH' | 'SME_PARTNER';
  status: 'ACTIVE' | 'PENDING_APPROVAL' | 'FROZEN' | 'TERMINATED';
  domain: string;
  maxUsersQuota: number;
  currentUsers: number;
  maxDevicesQuota: number;
  currentDevices: number;
  rateLimitRps: number;
  contactEmail: string;
  contactPhone: string;
  contractExpiry: string;
  features: string[];
}

export const TenantManagementPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'directory' | 'onboarding' | 'quota' | 'decommission'>('directory');
  const [searchTerm, setSearchTerm] = useState('');
  const [isNewModalOpen, setIsNewModalOpen] = useState(false);

  const [tenants, setTenants] = useState<Tenant[]>([
    {
      id: 'ten-01',
      code: 'VPB-FINTECH',
      name: 'VPBank Piggy Digital',
      type: 'COMMERCIAL_BANK',
      status: 'ACTIVE',
      domain: 'vpb-piggy.liochio.vn',
      maxUsersQuota: 50000,
      currentUsers: 18450,
      maxDevicesQuota: 10000,
      currentDevices: 820,
      rateLimitRps: 5000,
      contactEmail: 'admin@vpbank-fintech.com',
      contactPhone: '1900545415',
      contractExpiry: '2028-12-31',
      features: ['SMART_PIGGY', 'PARENTAL_MATCHING', 'AI_ROBO', 'DOUBLE_ENTRY_LEDGER'],
    },
    {
      id: 'ten-02',
      code: 'TCB-KIDS',
      name: 'Techcombank Junior Savings',
      type: 'COMMERCIAL_BANK',
      status: 'ACTIVE',
      domain: 'junior.techcombank.com.vn',
      maxUsersQuota: 100000,
      currentUsers: 24200,
      maxDevicesQuota: 25000,
      currentDevices: 650,
      rateLimitRps: 8000,
      contactEmail: 'digital@tcb.com.vn',
      contactPhone: '1800588822',
      contractExpiry: '2029-06-30',
      features: ['SMART_PIGGY', 'PARENTAL_MATCHING', 'AI_ROBO', 'DOUBLE_ENTRY_LEDGER', 'CUSTOM_DOMAIN'],
    },
    {
      id: 'ten-03',
      code: 'VIETTEL-PAY',
      name: 'Viettel Digital Family',
      type: 'ENTERPRISE_FINTECH',
      status: 'ACTIVE',
      domain: 'family.viettelpay.vn',
      maxUsersQuota: 20000,
      currentUsers: 5850,
      maxDevicesQuota: 5000,
      currentDevices: 72,
      rateLimitRps: 3000,
      contactEmail: 'corp@viettel.vn',
      contactPhone: '18008098',
      contractExpiry: '2027-12-31',
      features: ['SMART_PIGGY', 'PARENTAL_MATCHING'],
    },
    {
      id: 'ten-04',
      code: 'MB-JUNIOR',
      name: 'MB Bank Family Onboarding',
      type: 'COMMERCIAL_BANK',
      status: 'PENDING_APPROVAL',
      domain: 'kids.mbbank.com.vn',
      maxUsersQuota: 30000,
      currentUsers: 0,
      maxDevicesQuota: 8000,
      currentDevices: 0,
      rateLimitRps: 4000,
      contactEmail: 'corp-admin@mbbank.com.vn',
      contactPhone: '1900545426',
      contractExpiry: '2028-12-31',
      features: ['SMART_PIGGY', 'PARENTAL_MATCHING', 'AI_ROBO'],
    },
  ]);

  const [newForm, setNewForm] = useState({
    code: '',
    name: '',
    type: 'COMMERCIAL_BANK' as const,
    domain: '',
    maxUsersQuota: 20000,
    maxDevicesQuota: 5000,
    rateLimitRps: 2000,
    contactEmail: '',
    contactPhone: '',
  });

  const handleCreateTenant = (e: React.FormEvent) => {
    e.preventDefault();
    const newTenant: Tenant = {
      id: `ten-${Date.now().toString().slice(-4)}`,
      code: newForm.code.toUpperCase(),
      name: newForm.name,
      type: newForm.type,
      status: 'PENDING_APPROVAL',
      domain: newForm.domain,
      maxUsersQuota: Number(newForm.maxUsersQuota),
      currentUsers: 0,
      maxDevicesQuota: Number(newForm.maxDevicesQuota),
      currentDevices: 0,
      rateLimitRps: Number(newForm.rateLimitRps),
      contactEmail: newForm.contactEmail,
      contactPhone: newForm.contactPhone,
      contractExpiry: '2028-12-31',
      features: ['SMART_PIGGY', 'PARENTAL_MATCHING'],
    };
    setTenants([newTenant, ...tenants]);
    setIsNewModalOpen(false);
  };

  const handleToggleFreeze = (id: string) => {
    setTenants(tenants.map(t => {
      if (t.id === id) {
        return {
          ...t,
          status: t.status === 'FROZEN' ? 'ACTIVE' : 'FROZEN',
        };
      }
      return t;
    }));
  };

  const handleApproveOnboarding = (id: string) => {
    setTenants(tenants.map(t => {
      if (t.id === id) {
        return { ...t, status: 'ACTIVE' };
      }
      return t;
    }));
  };

  const filteredTenants = tenants.filter(t => 
    t.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
    t.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
    t.domain.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6 animate-fade-in max-w-7xl">
      {/* Top Banner */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-slate-900/80 border border-slate-800 p-6 rounded-3xl backdrop-blur-xl">
        <div>
          <div className="flex items-center gap-2 font-mono text-xs text-amber-400 font-bold mb-1">
            <Building2 className="w-4 h-4" />
            <span>SA_TENANT_MGMT &bull; ENTERPRISE MULTI-TENANCY ENGINE</span>
          </div>
          <h1 className="text-2xl font-black text-white tracking-tight">
            Quản Trị Khách Hàng Doanh Nghiệp (Tenants) & Hạn Ngạch
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Cấp phát dữ liệu phân vùng độc lập, thẩm định hồ sơ Onboarding, thiết lập Hạn mức Quota và Đóng băng dịch vụ.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button onClick={() => setIsNewModalOpen(true)} className="flex items-center gap-2">
            <Plus className="w-4 h-4" />
            <span>Thẩm Định Doanh Nghiệp Mới</span>
          </Button>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-800/80 gap-2 pb-2 text-xs font-bold">
        <button
          onClick={() => setActiveTab('directory')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'directory'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <Users className="w-4 h-4" />
          <span>SA_TENANT_DIRECTORY (Danh Bạ Đối Tác)</span>
        </button>

        <button
          onClick={() => setActiveTab('onboarding')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'onboarding'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <UserCheck className="w-4 h-4" />
          <span>SA_TENANT_ONBOARDING (Hàng Đợi Thẩm Định)</span>
        </button>

        <button
          onClick={() => setActiveTab('quota')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'quota'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <Sliders className="w-4 h-4" />
          <span>SA_TENANT_QUOTA (Hạn Ngạch & Gói Cước)</span>
        </button>

        <button
          onClick={() => setActiveTab('decommission')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'decommission'
              ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <ShieldAlert className="w-4 h-4" />
          <span>SA_TENANT_DECOMMISSION (Đóng Băng & Thu Hồi)</span>
        </button>
      </div>

      {/* SEARCH BAR */}
      <div className="flex items-center gap-3 bg-slate-900/60 p-3 rounded-2xl border border-slate-800">
        <Search className="w-4 h-4 text-slate-500 ml-2" />
        <input
          type="text"
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
          placeholder="Tìm kiếm theo Tên Doanh Nghiệp, Mã Tenant (VPB, TCB, MB...), Domain..."
          className="w-full bg-transparent text-xs text-white placeholder-slate-500 focus:outline-none font-mono"
        />
      </div>

      {/* TAB 1: DIRECTORY */}
      {activeTab === 'directory' && (
        <Card className="p-0 overflow-hidden">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="bg-slate-950/80 text-slate-400 uppercase tracking-wider text-[10px] font-bold border-b border-slate-800 font-mono">
              <tr>
                <th className="py-3.5 px-4">Doanh Nghiệp / Đối Tác</th>
                <th className="py-3.5 px-4">Loại Hình</th>
                <th className="py-3.5 px-4">Người Dùng B2C</th>
                <th className="py-3.5 px-4">Heo Đất IoT</th>
                <th className="py-3.5 px-4">Hạn Hợp Đồng</th>
                <th className="py-3.5 px-4">Trạng Thái</th>
                <th className="py-3.5 px-4 text-right">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {filteredTenants.map(t => (
                <tr key={t.id} className="hover:bg-slate-800/30 transition">
                  <td className="py-4 px-4">
                    <div className="font-bold text-white text-sm">{t.name}</div>
                    <div className="text-[11px] text-slate-400 font-mono flex items-center gap-2 mt-0.5">
                      <span className="text-amber-400">{t.code}</span>
                      <span>&bull;</span>
                      <span>{t.domain}</span>
                    </div>
                  </td>
                  <td className="py-4 px-4 font-mono text-[11px]">
                    <Badge variant={t.type === 'COMMERCIAL_BANK' ? 'primary' : 'info'}>
                      {t.type}
                    </Badge>
                  </td>
                  <td className="py-4 px-4 font-mono">
                    <div className="font-bold text-slate-200">{t.currentUsers.toLocaleString()} / {t.maxUsersQuota.toLocaleString()}</div>
                    <div className="w-24 bg-slate-800 h-1.5 rounded-full overflow-hidden mt-1">
                      <div className="bg-indigo-500 h-full rounded-full" style={{ width: `${Math.min(100, (t.currentUsers / t.maxUsersQuota) * 100)}%` }} />
                    </div>
                  </td>
                  <td className="py-4 px-4 font-mono">
                    <div className="font-bold text-slate-200">{t.currentDevices.toLocaleString()} / {t.maxDevicesQuota.toLocaleString()}</div>
                    <div className="w-24 bg-slate-800 h-1.5 rounded-full overflow-hidden mt-1">
                      <div className="bg-amber-500 h-full rounded-full" style={{ width: `${Math.min(100, (t.currentDevices / t.maxDevicesQuota) * 100)}%` }} />
                    </div>
                  </td>
                  <td className="py-4 px-4 font-mono text-slate-400">{t.contractExpiry}</td>
                  <td className="py-4 px-4">
                    <Badge variant={t.status === 'ACTIVE' ? 'success' : t.status === 'FROZEN' ? 'danger' : 'warning'}>
                      {t.status}
                    </Badge>
                  </td>
                  <td className="py-4 px-4 text-right">
                    <button
                      onClick={() => handleToggleFreeze(t.id)}
                      className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition cursor-pointer ${
                        t.status === 'FROZEN'
                          ? 'bg-emerald-500/10 text-emerald-400 hover:bg-emerald-500/20 border border-emerald-500/30'
                          : 'bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 border border-rose-500/30'
                      }`}
                    >
                      {t.status === 'FROZEN' ? 'Mở Khóa' : 'Đóng Băng'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}

      {/* TAB 2: ONBOARDING WORKFLOW */}
      {activeTab === 'onboarding' && (
        <div className="space-y-4">
          <div className="p-4 bg-amber-500/10 border border-amber-500/30 rounded-2xl text-amber-300 text-xs flex items-center justify-between">
            <div className="flex items-center gap-2">
              <UserCheck className="w-4 h-4 text-amber-400" />
              <span>Quy trình Thẩm định Doanh nghiệp: Kiểm tra hồ sơ pháp nhân & Cấp phát Database Schema độc lập.</span>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {tenants.filter(t => t.status === 'PENDING_APPROVAL').map(t => (
              <Card key={t.id} className="space-y-3">
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="font-bold text-white text-base">{t.name}</h3>
                    <p className="text-xs text-amber-400 font-mono mt-0.5">{t.code} &bull; {t.domain}</p>
                  </div>
                  <Badge variant="warning">CHỜ DUYỆT</Badge>
                </div>

                <div className="grid grid-cols-2 gap-2 text-xs text-slate-300 pt-2 border-t border-slate-800">
                  <div>Email: <span className="text-white font-mono">{t.contactEmail}</span></div>
                  <div>Hotline: <span className="text-white font-mono">{t.contactPhone}</span></div>
                  <div>Hạn mức Users: <span className="text-white font-mono">{t.maxUsersQuota.toLocaleString()}</span></div>
                  <div>Hạn mức IoT: <span className="text-white font-mono">{t.maxDevicesQuota.toLocaleString()}</span></div>
                </div>

                <div className="pt-3 border-t border-slate-800 flex justify-end gap-2">
                  <button
                    onClick={() => handleApproveOnboarding(t.id)}
                    className="px-4 py-2 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white rounded-xl text-xs font-bold transition flex items-center gap-1.5 cursor-pointer shadow-lg shadow-emerald-500/20"
                  >
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    <span>Phê Duyệt & Cấp Schema Tức Thì</span>
                  </button>
                </div>
              </Card>
            ))}
          </div>
        </div>
      )}

      {/* TAB 3: QUOTA LIMITS */}
      {activeTab === 'quota' && (
        <Card className="space-y-4">
          <h2 className="text-sm font-bold text-white flex items-center gap-2 pb-3 border-b border-slate-800">
            <Sliders className="w-4 h-4 text-amber-400" />
            <span>Cấu Hình Trần Hạn Ngạch & Rate Limit Toàn Bộ Tenants</span>
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {tenants.map(t => (
              <div key={t.id} className="p-4 bg-slate-950/60 rounded-2xl border border-slate-800 space-y-3">
                <div className="font-bold text-white flex justify-between items-center">
                  <span>{t.name}</span>
                  <span className="text-xs text-amber-400 font-mono">{t.code}</span>
                </div>
                <div className="space-y-2 text-xs">
                  <div className="flex justify-between text-slate-400">
                    <span>Rate Limit API:</span>
                    <span className="font-mono text-emerald-400 font-bold">{t.rateLimitRps} RPS</span>
                  </div>
                  <div className="flex justify-between text-slate-400">
                    <span>User Quota:</span>
                    <span className="font-mono text-slate-200 font-bold">{t.maxUsersQuota.toLocaleString()}</span>
                  </div>
                  <div className="flex justify-between text-slate-400">
                    <span>IoT Solenoid Quota:</span>
                    <span className="font-mono text-slate-200 font-bold">{t.maxDevicesQuota.toLocaleString()}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* TAB 4: DECOMMISSION */}
      {activeTab === 'decommission' && (
        <Card className="space-y-4 border-rose-900/50">
          <div className="flex items-center gap-3 pb-3 border-b border-rose-900/40 text-rose-300">
            <ShieldAlert className="w-5 h-5 text-rose-400" />
            <div>
              <h2 className="text-sm font-bold text-white">Đóng Băng Khẩn Cấp & Thu Hồi Dịch Vụ Tenant</h2>
              <p className="text-xs text-slate-400">Khi kích hoạt đóng băng, toàn bộ Access Token của Tenant sẽ bị thu hồi tức thì trên Redis Cluster.</p>
            </div>
          </div>

          <div className="space-y-3">
            {tenants.map(t => (
              <div key={t.id} className="flex items-center justify-between p-3.5 bg-slate-950/80 rounded-xl border border-slate-800">
                <div>
                  <div className="text-xs font-bold text-white">{t.name} ({t.code})</div>
                  <div className="text-[11px] text-slate-500 font-mono">{t.domain}</div>
                </div>
                <div className="flex items-center gap-2">
                  <Badge variant={t.status === 'FROZEN' ? 'danger' : 'success'}>
                    {t.status}
                  </Badge>
                  <button
                    onClick={() => handleToggleFreeze(t.id)}
                    className="px-3 py-1.5 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30 rounded-lg text-xs font-bold transition cursor-pointer"
                  >
                    {t.status === 'FROZEN' ? 'Re-Activate Tenant' : 'Khóa Đóng Băng Khẩn Cấp'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* MODAL: CREATE TENANT */}
      <Modal
        isOpen={isNewModalOpen}
        onClose={() => setIsNewModalOpen(false)}
        title="Thẩm Định & Khởi Tạo Doanh Nghiệp Mới"
        maxWidth="lg"
      >
        <form onSubmit={handleCreateTenant} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">Tên Doanh Nghiệp / Ngân Hàng *</label>
              <input
                type="text"
                required
                value={newForm.name}
                onChange={e => setNewForm({ ...newForm, name: e.target.value })}
                placeholder="VD: MB Bank Digital Family"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">Mã Định Danh (Tenant Code) *</label>
              <input
                type="text"
                required
                value={newForm.code}
                onChange={e => setNewForm({ ...newForm, code: e.target.value.toUpperCase() })}
                placeholder="VD: MB-DIGITAL"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white uppercase font-mono focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">Loại Hình Doanh Nghiệp</label>
              <select
                value={newForm.type}
                onChange={e => setNewForm({ ...newForm, type: e.target.value as any })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              >
                <option value="COMMERCIAL_BANK">Ngân Hàng Thương Mại</option>
                <option value="ENTERPRISE_FINTECH">Tổ Chức FinTech Quy Mô Lớn</option>
                <option value="SME_PARTNER">Đối Tác B2B SME</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">Tên Miền Tùy Chỉnh (Custom Domain)</label>
              <input
                type="text"
                value={newForm.domain}
                onChange={e => setNewForm({ ...newForm, domain: e.target.value })}
                placeholder="VD: piggy.mbbank.com.vn"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white font-mono focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">Hạn Ngạch Users</label>
              <input
                type="number"
                value={newForm.maxUsersQuota}
                onChange={e => setNewForm({ ...newForm, maxUsersQuota: Number(e.target.value) })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white font-mono focus:outline-none focus:border-amber-500"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">Hạn Ngạch IoT Heo</label>
              <input
                type="number"
                value={newForm.maxDevicesQuota}
                onChange={e => setNewForm({ ...newForm, maxDevicesQuota: Number(e.target.value) })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white font-mono focus:outline-none focus:border-amber-500"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">Rate Limit (RPS)</label>
              <input
                type="number"
                value={newForm.rateLimitRps}
                onChange={e => setNewForm({ ...newForm, rateLimitRps: Number(e.target.value) })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white font-mono focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">Email Đại Diện Pháp Nhân *</label>
              <input
                type="email"
                required
                value={newForm.contactEmail}
                onChange={e => setNewForm({ ...newForm, contactEmail: e.target.value })}
                placeholder="corp@mbbank.com.vn"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-300 mb-1">Số Điện Thoại Trực Ban *</label>
              <input
                type="tel"
                required
                value={newForm.contactPhone}
                onChange={e => setNewForm({ ...newForm, contactPhone: e.target.value })}
                placeholder="1900545426"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div className="pt-4 border-t border-slate-800 flex justify-end gap-3">
            <Button type="button" variant="outline" onClick={() => setIsNewModalOpen(false)}>
              Hủy Bỏ
            </Button>
            <Button type="submit">
              Xác Nhận Khởi Tạo Hồ Sơ
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
