import React, { useState } from 'react';
import { Users, Plus, Search, Lock } from 'lucide-react';
import { Card, Badge } from '../../components/common/CardAndBadge';
import { Button } from '../../components/common/Button';
import { Modal } from '../../components/common/Modal';
import { CorpStaff, UserRole } from '../../types';

export const StaffRbacPage: React.FC = () => {
  const [staffList, setStaffList] = useState<CorpStaff[]>([
    {
      id: 'staff-01',
      tenantId: 'tenant-001',
      username: 'corpadmin_vpb',
      fullName: 'Trần Đại Quang',
      email: 'corpadmin@vpbank.com.vn',
      role: 'CORP_ADMIN',
      branchName: 'Headquarters - Hanoi',
      phone: '0901234567',
      status: 'ACTIVE',
      createdAt: '2026-01-15',
    },
    {
      id: 'staff-02',
      tenantId: 'tenant-001',
      username: 'manager_hoangmai',
      fullName: 'Nguyễn Văn Minh',
      email: 'minh.nv@vpbank.com.vn',
      role: 'BRANCH_MANAGER',
      branchName: 'Chi nhánh Hoàng Mai',
      phone: '0912345678',
      status: 'ACTIVE',
      createdAt: '2026-01-20',
    },
    {
      id: 'staff-03',
      tenantId: 'tenant-001',
      username: 'staff_audit_01',
      fullName: 'Phạm Quỳnh Anh',
      email: 'quynhanh.p@vpbank.com.vn',
      role: 'CORP_STAFF',
      branchName: 'Chi nhánh Quận 1 Bến Thành',
      phone: '0988776655',
      status: 'ACTIVE',
      createdAt: '2026-02-15',
    },
  ]);

  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [revokedMessage, setRevokedMessage] = useState<string | null>(null);

  const [newStaff, setNewStaff] = useState<{
    fullName: string;
    username: string;
    email: string;
    phone: string;
    role: UserRole;
    branchName: string;
  }>({
    fullName: '',
    username: '',
    email: '',
    phone: '',
    role: 'CORP_STAFF',
    branchName: 'Chi nhánh Hoàng Mai',
  });

  const handleCreateStaff = (e: React.FormEvent) => {
    e.preventDefault();
    const created: CorpStaff = {
      id: `staff-${Date.now().toString().slice(-4)}`,
      tenantId: 'tenant-001',
      username: newStaff.username,
      fullName: newStaff.fullName,
      email: newStaff.email,
      phone: newStaff.phone,
      role: newStaff.role,
      branchName: newStaff.branchName,
      status: 'ACTIVE',
      createdAt: new Date().toISOString().split('T')[0],
    };

    setStaffList([created, ...staffList]);
    setIsModalOpen(false);
    setNewStaff({
      fullName: '',
      username: '',
      email: '',
      phone: '',
      role: 'CORP_STAFF',
      branchName: 'Chi nhánh Hoàng Mai',
    });
  };

  const handleRevokeSession = (username: string) => {
    setRevokedMessage(`Active JWT session for user [${username}] revoked immediately via Redis Pub/Sub.`);
    setTimeout(() => setRevokedMessage(null), 4000);
  };

  const filtered = staffList.filter(s => 
    s.fullName.toLowerCase().includes(search.toLowerCase()) || 
    s.username.toLowerCase().includes(search.toLowerCase()) ||
    s.email.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6 animate-fade-in max-w-6xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <Users className="w-7 h-7 text-cyan-400" />
            Staff Accounts & RBAC Permissions
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Enterprise RBAC hierarchy with immediate Redis token revocation capability
          </p>
        </div>
        <Button onClick={() => setIsModalOpen(true)} className="flex items-center gap-2">
          <Plus className="w-4 h-4" />
          Add Staff Member
        </Button>
      </div>

      {revokedMessage && (
        <div className="p-3 bg-amber-500/10 border border-amber-500/30 rounded-xl text-amber-300 text-xs flex items-center gap-2 animate-fade-in">
          <Lock className="w-4 h-4 text-amber-400 shrink-0" />
          <span>{revokedMessage}</span>
        </div>
      )}

      <div className="flex items-center gap-3 bg-slate-900/60 p-3 rounded-2xl border border-slate-800">
        <Search className="w-4 h-4 text-slate-500 ml-2" />
        <input
          type="text"
          value={search}
          onChange={e => setSearch(e.target.value)}
          placeholder="Search staff by name, email, or username..."
          className="w-full bg-transparent text-xs text-white placeholder-slate-500 focus:outline-none"
        />
      </div>

      <div className="bg-slate-900/70 border border-slate-800/80 rounded-2xl overflow-hidden shadow-xl">
        <table className="w-full text-left text-xs text-slate-300">
          <thead className="bg-slate-950/80 text-slate-400 uppercase tracking-wider text-[10px] font-bold border-b border-slate-800">
            <tr>
              <th className="py-3 px-4">Staff Member</th>
              <th className="py-3 px-4">Assigned Role</th>
              <th className="py-3 px-4">Assigned Branch</th>
              <th className="py-3 px-4">Contact</th>
              <th className="py-3 px-4">Status</th>
              <th className="py-3 px-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {filtered.map(staff => (
              <tr key={staff.id} className="hover:bg-slate-800/30 transition">
                <td className="py-4 px-4">
                  <div className="font-bold text-white text-sm">{staff.fullName}</div>
                  <div className="text-[11px] text-cyan-400 font-mono">@{staff.username}</div>
                </td>
                <td className="py-4 px-4">
                  <Badge variant={staff.role === 'CORP_ADMIN' ? 'primary' : staff.role === 'BRANCH_MANAGER' ? 'info' : 'neutral'}>
                    {staff.role}
                  </Badge>
                </td>
                <td className="py-4 px-4 font-medium text-slate-200">
                  {staff.branchName || 'Unassigned'}
                </td>
                <td className="py-4 px-4">
                  <div className="text-slate-300">{staff.email}</div>
                  <div className="text-[11px] text-slate-500 font-mono">{staff.phone}</div>
                </td>
                <td className="py-4 px-4">
                  <Badge variant={staff.status === 'ACTIVE' ? 'success' : 'danger'}>
                    {staff.status}
                  </Badge>
                </td>
                <td className="py-4 px-4 text-right">
                  <button
                    onClick={() => handleRevokeSession(staff.username)}
                    className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-rose-500/10 text-rose-400 hover:bg-rose-500/20 border border-rose-500/20 transition cursor-pointer"
                  >
                    Revoke Token
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Add Corporate Staff Member"
        maxWidth="md"
      >
        <form onSubmit={handleCreateStaff} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">Full Name *</label>
            <input
              type="text"
              required
              value={newStaff.fullName}
              onChange={e => setNewStaff({ ...newStaff, fullName: e.target.value })}
              placeholder="e.g. Lê Thanh Tùng"
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Username *</label>
              <input
                type="text"
                required
                value={newStaff.username}
                onChange={e => setNewStaff({ ...newStaff, username: e.target.value })}
                placeholder="tung.lt"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Role *</label>
              <select
                value={newStaff.role}
                onChange={e => setNewStaff({ ...newStaff, role: e.target.value as UserRole })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
              >
                <option value="CORP_STAFF">CORP_STAFF (Support/Tech)</option>
                <option value="BRANCH_MANAGER">BRANCH_MANAGER (Regional)</option>
                <option value="CORP_ADMIN">CORP_ADMIN (Full Tenant Rights)</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Email *</label>
              <input
                type="email"
                required
                value={newStaff.email}
                onChange={e => setNewStaff({ ...newStaff, email: e.target.value })}
                placeholder="tung.lt@vpbank.com.vn"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Phone *</label>
              <input
                type="tel"
                required
                value={newStaff.phone}
                onChange={e => setNewStaff({ ...newStaff, phone: e.target.value })}
                placeholder="0912345678"
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
              />
            </div>
          </div>

          <div className="pt-4 border-t border-slate-800 flex justify-end gap-3">
            <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button type="submit">
              Register Staff
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
