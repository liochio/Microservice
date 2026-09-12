import React, { useState } from 'react';
import { MapPin, Plus, Search, Phone, User } from 'lucide-react';
import { Card, Badge } from '../../components/common/CardAndBadge';
import { Button } from '../../components/common/Button';
import { Modal } from '../../components/common/Modal';
import { Branch } from '../../types';

export const BranchesPage: React.FC = () => {
  const [branches, setBranches] = useState<Branch[]>([
    {
      id: 'br-01',
      tenantId: 'tenant-001',
      provinceId: 'p-01',
      provinceName: 'Thành phố Hà Nội',
      code: 'VPB-HN-HOANGMAI',
      name: 'Chi nhánh Hoàng Mai',
      address: 'Số 120 Đường Kim Đồng, P. Giáp Bát, Q. Hoàng Mai, Hà Nội',
      phone: '0243 864 5566',
      managerName: 'Nguyễn Văn Minh',
      status: 'ACTIVE',
      createdAt: '2026-01-20',
    },
    {
      id: 'br-02',
      tenantId: 'tenant-001',
      provinceId: 'p-79',
      provinceName: 'Thành phố Hồ Chí Minh',
      code: 'VPB-SG-Q1',
      name: 'Chi nhánh Quận 1 Bến Thành',
      address: 'Số 45 Lê Duẩn, P. Bến Nghé, Quận 1, TP. Hồ Chí Minh',
      phone: '0283 822 9988',
      managerName: 'Trần Thị Thu Thảo',
      status: 'ACTIVE',
      createdAt: '2026-02-10',
    },
    {
      id: 'br-03',
      tenantId: 'tenant-001',
      provinceId: 'p-48',
      provinceName: 'Thành phố Đà Nẵng',
      code: 'VPB-DN-HAICHAU',
      name: 'Chi nhánh Hải Châu Đà Nẵng',
      address: 'Số 88 Nguyễn Văn Linh, Q. Hải Châu, TP. Đà Nẵng',
      phone: '0236 358 2211',
      managerName: 'Lê Hoàng Long',
      status: 'ACTIVE',
      createdAt: '2026-02-25',
    },
  ]);

  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newBranch, setNewBranch] = useState({
    code: '',
    name: '',
    provinceId: 'p-01',
    provinceName: 'Thành phố Hà Nội',
    address: '',
    phone: '',
    managerName: '',
  });

  const provinces = [
    { id: 'p-01', name: 'Thành phố Hà Nội' },
    { id: 'p-79', name: 'Thành phố Hồ Chí Minh' },
    { id: 'p-48', name: 'Thành phố Đà Nẵng' },
    { id: 'p-31', name: 'Thành phố Hải Phòng' },
    { id: 'p-92', name: 'Thành phố Cần Thơ' },
  ];

  const handleCreateBranch = (e: React.FormEvent) => {
    e.preventDefault();
    const selProvince = provinces.find(p => p.id === newBranch.provinceId);
    const created: Branch = {
      id: `br-${Date.now().toString().slice(-4)}`,
      tenantId: 'tenant-001',
      provinceId: newBranch.provinceId,
      provinceName: selProvince?.name || 'Thành phố Hà Nội',
      code: newBranch.code.toUpperCase(),
      name: newBranch.name,
      address: newBranch.address,
      phone: newBranch.phone,
      managerName: newBranch.managerName,
      status: 'ACTIVE',
      createdAt: new Date().toISOString().split('T')[0],
    };

    setBranches([created, ...branches]);
    setIsModalOpen(false);
    setNewBranch({
      code: '',
      name: '',
      provinceId: 'p-01',
      provinceName: 'Thành phố Hà Nội',
      address: '',
      phone: '',
      managerName: '',
    });
  };

  const filtered = branches.filter(b => 
    b.name.toLowerCase().includes(search.toLowerCase()) || 
    b.code.toLowerCase().includes(search.toLowerCase()) ||
    (b.provinceName && b.provinceName.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div className="space-y-6 animate-fade-in max-w-6xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <MapPin className="w-7 h-7 text-cyan-400" />
            Branch Network Management
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Map regional branches to 63 Vietnam Provinces & assign branch directors
          </p>
        </div>
        <Button onClick={() => setIsModalOpen(true)} className="flex items-center gap-2">
          <Plus className="w-4 h-4" />
          Add Corporate Branch
        </Button>
      </div>

      <div className="flex items-center gap-3 bg-slate-900/60 p-3 rounded-2xl border border-slate-800">
        <Search className="w-4 h-4 text-slate-500 ml-2" />
        <input
          type="text"
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder="Search branch by name, branch code, or province..."
          className="w-full bg-transparent text-xs text-white placeholder-slate-500 focus:outline-none"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {filtered.map(b => (
          <Card key={b.id} className="p-5 flex flex-col justify-between space-y-4">
            <div>
              <div className="flex items-start justify-between">
                <div>
                  <span className="text-[10px] font-mono font-bold text-cyan-400 bg-cyan-500/10 px-2 py-0.5 rounded border border-cyan-500/20">
                    {b.code}
                  </span>
                  <h3 className="font-bold text-white text-base mt-2">{b.name}</h3>
                </div>
                <Badge variant="success">{b.status}</Badge>
              </div>

              <div className="mt-4 space-y-2 text-xs text-slate-400">
                <div className="flex items-center gap-2">
                  <MapPin className="w-4 h-4 text-slate-500 shrink-0" />
                  <span className="text-slate-200 font-medium">{b.provinceName}</span>
                </div>
                <div className="text-slate-400 pl-6 text-[11px] leading-relaxed">
                  {b.address}
                </div>
                <div className="flex items-center gap-2 pt-2 border-t border-slate-800/80">
                  <User className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                  <span className="text-slate-300">Director: <strong className="text-white">{b.managerName || 'Chưa gán'}</strong></span>
                </div>
                <div className="flex items-center gap-2">
                  <Phone className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                  <span className="text-slate-300 font-mono">{b.phone}</span>
                </div>
              </div>
            </div>
          </Card>
        ))}
      </div>

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Add Corporate Branch"
        maxWidth="lg"
      >
        <form onSubmit={handleCreateBranch} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Branch Name *</label>
              <input
                type="text"
                required
                value={newBranch.name}
                onChange={e => setNewBranch({ ...newBranch, name: e.target.value })}
                placeholder="e.g. Chi nhánh Cầu Giấy"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Branch Code *</label>
              <input
                type="text"
                required
                value={newBranch.code}
                onChange={e => setNewBranch({ ...newBranch, code: e.target.value })}
                placeholder="e.g. VPB-HN-CAUGIAY"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white uppercase focus:outline-none focus:border-cyan-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">Province / City *</label>
            <select
              value={newBranch.provinceId}
              onChange={e => setNewBranch({ ...newBranch, provinceId: e.target.value })}
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
            >
              {provinces.map(p => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">Address *</label>
            <input
              type="text"
              required
              value={newBranch.address}
              onChange={e => setNewBranch({ ...newBranch, address: e.target.value })}
              placeholder="e.g. 241 Xuân Thủy, Cầu Giấy, Hà Nội"
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Manager Name</label>
              <input
                type="text"
                value={newBranch.managerName}
                onChange={e => setNewBranch({ ...newBranch, managerName: e.target.value })}
                placeholder="e.g. Lê Hải Yến"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Phone</label>
              <input
                type="tel"
                value={newBranch.phone}
                onChange={e => setNewBranch({ ...newBranch, phone: e.target.value })}
                placeholder="0243 999 8888"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
              />
            </div>
          </div>

          <div className="pt-4 border-t border-slate-800 flex justify-end gap-3">
            <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit">
              Save Branch
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
