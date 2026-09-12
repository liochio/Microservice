import React, { useState } from 'react';
import { 
  Building2, 
  Plus, 
  Search, 
  CheckCircle2, 
  Cpu, 
  Users
} from 'lucide-react';
import { Card, Badge } from '../components/common/CardAndBadge';
import { Button } from '../components/common/Button';
import { Modal } from '../components/common/Modal';
import { Tenant, TenantType } from '../types';

export const TenantsPage: React.FC = () => {
  const [tenants, setTenants] = useState<Tenant[]>([
    {
      id: 'tenant-001',
      code: 'VPB-FINTECH',
      name: 'VPBank Piggy Digital',
      type: 'BANK',
      status: 'ACTIVE',
      domain: 'vpb-piggy.liochio.vn',
      maxUsersQuota: 50000,
      maxDevicesQuota: 10000,
      currentUsersCount: 18450,
      currentDevicesCount: 820,
      contactEmail: 'admin@vpbank-fintech.com',
      contactPhone: '1900545415',
      contractExpiresAt: '2027-12-31',
      createdAt: '2026-01-15',
      features: ['SMART_PIGGY', 'PARENTAL_MATCHING', 'AI_ROBO', 'DOUBLE_ENTRY_LEDGER'],
    },
    {
      id: 'tenant-002',
      code: 'TCB-KIDS',
      name: 'Techcombank Junior Savings',
      type: 'BANK',
      status: 'ACTIVE',
      domain: 'junior.techcombank.com.vn',
      maxUsersQuota: 100000,
      maxDevicesQuota: 25000,
      currentUsersCount: 24200,
      currentDevicesCount: 650,
      contactEmail: 'digital@tcb.com.vn',
      contactPhone: '1800588822',
      contractExpiresAt: '2028-06-30',
      createdAt: '2026-02-01',
      features: ['SMART_PIGGY', 'PARENTAL_MATCHING', 'AI_ROBO', 'DOUBLE_ENTRY_LEDGER', 'CUSTOM_DOMAIN'],
    },
    {
      id: 'tenant-003',
      code: 'VIETEL-PAY',
      name: 'Viettel Digital Family',
      type: 'ENTERPRISE',
      status: 'ACTIVE',
      domain: 'family.viettelpay.vn',
      maxUsersQuota: 20000,
      maxDevicesQuota: 5000,
      currentUsersCount: 5850,
      currentDevicesCount: 72,
      contactEmail: 'corp@viettel.vn',
      contactPhone: '18008098',
      contractExpiresAt: '2026-12-31',
      createdAt: '2026-03-10',
      features: ['SMART_PIGGY', 'PARENTAL_MATCHING'],
    },
  ]);

  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newTenant, setNewTenant] = useState<{
    name: string;
    code: string;
    type: TenantType;
    contactEmail: string;
    contactPhone: string;
    maxUsersQuota: number;
    maxDevicesQuota: number;
    domain: string;
  }>({
    name: '',
    code: '',
    type: 'ENTERPRISE',
    contactEmail: '',
    contactPhone: '',
    maxUsersQuota: 10000,
    maxDevicesQuota: 2000,
    domain: '',
  });

  const handleCreateTenant = (e: React.FormEvent) => {
    e.preventDefault();
    const created: Tenant = {
      id: `tenant-${Date.now().toString().slice(-4)}`,
      code: newTenant.code.toUpperCase(),
      name: newTenant.name,
      type: newTenant.type,
      status: 'ACTIVE',
      domain: newTenant.domain,
      maxUsersQuota: Number(newTenant.maxUsersQuota),
      maxDevicesQuota: Number(newTenant.maxDevicesQuota),
      currentUsersCount: 0,
      currentDevicesCount: 0,
      contactEmail: newTenant.contactEmail,
      contactPhone: newTenant.contactPhone,
      contractExpiresAt: '2027-12-31',
      createdAt: new Date().toISOString().split('T')[0],
      features: ['SMART_PIGGY', 'PARENTAL_MATCHING', 'AI_ROBO'],
    };

    setTenants([created, ...tenants]);
    setIsModalOpen(false);
    setNewTenant({
      name: '',
      code: '',
      type: 'ENTERPRISE',
      contactEmail: '',
      contactPhone: '',
      maxUsersQuota: 10000,
      maxDevicesQuota: 2000,
      domain: '',
    });
  };

  const toggleTenantStatus = (id: string) => {
    setTenants(tenants.map(t => {
      if (t.id === id) {
        return { ...t, status: t.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE' };
      }
      return t;
    }));
  };

  const filteredTenants = tenants.filter(t => 
    t.name.toLowerCase().includes(search.toLowerCase()) || 
    t.code.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <Building2 className="w-7 h-7 text-amber-400" />
            B2B Tenants & Multi-Tenant Quotas
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Allocate isolated data partitions, IoT Solenoid limits, and Feature Flags per Corporate Entity
          </p>
        </div>
        <Button onClick={() => setIsModalOpen(true)} className="flex items-center gap-2">
          <Plus className="w-4 h-4" />
          Provision New Tenant
        </Button>
      </div>

      <div className="flex items-center gap-3 bg-slate-900/60 p-3 rounded-2xl border border-slate-800">
        <Search className="w-4 h-4 text-slate-500 ml-2" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search tenant by name, code, domain..."
          className="w-full bg-transparent text-xs text-white placeholder-slate-500 focus:outline-none"
        />
      </div>

      <div className="bg-slate-900/70 border border-slate-800/80 rounded-2xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="bg-slate-950/80 text-slate-400 uppercase tracking-wider text-[10px] font-bold border-b border-slate-800">
              <tr>
                <th className="py-3.5 px-4">Tenant Entity</th>
                <th className="py-3.5 px-4">Type</th>
                <th className="py-3.5 px-4">End-User Quota</th>
                <th className="py-3.5 px-4">IoT Hardware Quota</th>
                <th className="py-3.5 px-4">Features Matrix</th>
                <th className="py-3.5 px-4">Status</th>
                <th className="py-3.5 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {filteredTenants.map((t) => (
                <tr key={t.id} className="hover:bg-slate-800/30 transition">
                  <td className="py-4 px-4">
                    <div className="font-bold text-white text-sm">{t.name}</div>
                    <div className="text-[11px] text-slate-400 font-mono flex items-center gap-2 mt-0.5">
                      <span className="text-amber-400">{t.code}</span>
                      <span>&bull;</span>
                      <span>{t.domain || 'default.liochio.vn'}</span>
                    </div>
                  </td>
                  <td className="py-4 px-4">
                    <Badge variant={t.type === 'BANK' ? 'primary' : 'info'}>
                      {t.type}
                    </Badge>
                  </td>
                  <td className="py-4 px-4">
                    <div className="font-semibold text-slate-200">
                      {t.currentUsersCount.toLocaleString()} / {t.maxUsersQuota.toLocaleString()}
                    </div>
                    <div className="w-28 bg-slate-800 h-1.5 rounded-full overflow-hidden mt-1.5">
                      <div 
                        className="bg-indigo-500 h-full rounded-full"
                        style={{ width: `${Math.min(100, (t.currentUsersCount / t.maxUsersQuota) * 100)}%` }}
                      />
                    </div>
                  </td>
                  <td className="py-4 px-4">
                    <div className="font-semibold text-slate-200">
                      {t.currentDevicesCount.toLocaleString()} / {t.maxDevicesQuota.toLocaleString()}
                    </div>
                    <div className="w-28 bg-slate-800 h-1.5 rounded-full overflow-hidden mt-1.5">
                      <div 
                        className="bg-amber-500 h-full rounded-full"
                        style={{ width: `${Math.min(100, (t.currentDevicesCount / t.maxDevicesQuota) * 100)}%` }}
                      />
                    </div>
                  </td>
                  <td className="py-4 px-4">
                    <div className="flex flex-wrap gap-1 max-w-xs">
                      {t.features.map(f => (
                        <span key={f} className="px-1.5 py-0.5 bg-slate-800 text-[10px] text-slate-300 rounded border border-slate-700 font-mono">
                          {f}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="py-4 px-4">
                    <Badge variant={t.status === 'ACTIVE' ? 'success' : 'danger'}>
                      {t.status}
                    </Badge>
                  </td>
                  <td className="py-4 px-4 text-right">
                    <button
                      onClick={() => toggleTenantStatus(t.id)}
                      className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition cursor-pointer ${
                        t.status === 'ACTIVE'
                          ? 'bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 border border-rose-500/20'
                          : 'bg-emerald-500/10 text-emerald-400 hover:bg-emerald-500/20 border border-emerald-500/20'
                      }`}
                    >
                      {t.status === 'ACTIVE' ? 'Suspend' : 'Reactivate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Provision New Enterprise Tenant"
        maxWidth="lg"
      >
        <form onSubmit={handleCreateTenant} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Tenant Name *</label>
              <input
                type="text"
                required
                value={newTenant.name}
                onChange={e => setNewTenant({ ...newTenant, name: e.target.value })}
                placeholder="e.g. MB Bank Family Savings"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Tenant Code *</label>
              <input
                type="text"
                required
                value={newTenant.code}
                onChange={e => setNewTenant({ ...newTenant, code: e.target.value })}
                placeholder="e.g. MB-DIGITAL"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white uppercase focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Entity Type</label>
              <select
                value={newTenant.type}
                onChange={e => setNewTenant({ ...newTenant, type: e.target.value as TenantType })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              >
                <option value="BANK">Commercial Bank</option>
                <option value="ENTERPRISE">Enterprise Fintech</option>
                <option value="FINTECH_PARTNER">Fintech Partner</option>
                <option value="SME">SME Brand</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Custom Domain</label>
              <input
                type="text"
                value={newTenant.domain}
                onChange={e => setNewTenant({ ...newTenant, domain: e.target.value })}
                placeholder="e.g. piggy.mbbank.com.vn"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">User Quota Limit</label>
              <input
                type="number"
                value={newTenant.maxUsersQuota}
                onChange={e => setNewTenant({ ...newTenant, maxUsersQuota: Number(e.target.value) })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">IoT Device Quota Limit</label>
              <input
                type="number"
                value={newTenant.maxDevicesQuota}
                onChange={e => setNewTenant({ ...newTenant, maxDevicesQuota: Number(e.target.value) })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Contact Email *</label>
              <input
                type="email"
                required
                value={newTenant.contactEmail}
                onChange={e => setNewTenant({ ...newTenant, contactEmail: e.target.value })}
                placeholder="corp-admin@mbbank.com.vn"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Contact Phone *</label>
              <input
                type="tel"
                required
                value={newTenant.contactPhone}
                onChange={e => setNewTenant({ ...newTenant, contactPhone: e.target.value })}
                placeholder="1900545426"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div className="pt-4 border-t border-slate-800 flex justify-end gap-3">
            <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit">
              Provision Tenant
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
