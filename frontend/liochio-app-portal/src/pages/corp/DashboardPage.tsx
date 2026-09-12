import React, { useState } from 'react';
import { 
  Users, 
  MapPin, 
  Cpu, 
  DollarSign, 
  ArrowUpRight, 
  CheckCircle2, 
  ShieldCheck,
  TrendingUp,
  BatteryCharging
} from 'lucide-react';
import { Card, Badge } from '../../components/common/CardAndBadge';

export const DashboardPage: React.FC = () => {
  const [stats] = useState({
    totalStaff: 48,
    totalBranches: 12,
    activePiggyDevices: 820,
    piggyBatteryHealth: 98.4,
    monthlyVolume: 850000000,
    parentBonusPaid: 42500000,
  });

  return (
    <div className="space-y-8 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <span>VPBank FinTech Corporate Console</span>
            <span className="text-xs px-2.5 py-0.5 rounded-md bg-cyan-500/20 text-cyan-300 font-mono font-medium border border-cyan-500/30">
              PORT: 5171
            </span>
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Dedicated Tenant Dashboard: Manage branches, staff roles, IoT fleet, and brand customisation
          </p>
        </div>
        <Badge variant="success" className="px-3 py-1 text-xs">
          Tenant Isolation: ACTIVE
        </Badge>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
        <Card>
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Branch Network</span>
            <div className="p-2.5 bg-cyan-500/10 text-cyan-400 rounded-xl">
              <MapPin className="w-5 h-5" />
            </div>
          </div>
          <div className="text-3xl font-extrabold text-white">{stats.totalBranches}</div>
          <div className="text-xs text-cyan-400 mt-2 flex items-center gap-1 font-medium">
            <ArrowUpRight className="w-3.5 h-3.5" />
            <span>Across 6 Provinces / Cities</span>
          </div>
        </Card>

        <Card>
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Active Staff & RBAC</span>
            <div className="p-2.5 bg-blue-500/10 text-blue-400 rounded-xl">
              <Users className="w-5 h-5" />
            </div>
          </div>
          <div className="text-3xl font-extrabold text-white">{stats.totalStaff}</div>
          <div className="text-xs text-slate-400 mt-2 flex items-center gap-1 font-medium">
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
            <span>Branch Managers & Auditors</span>
          </div>
        </Card>

        <Card>
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Piggy IoT Fleet</span>
            <div className="p-2.5 bg-indigo-500/10 text-indigo-400 rounded-xl">
              <Cpu className="w-5 h-5" />
            </div>
          </div>
          <div className="text-3xl font-extrabold text-white">{stats.activePiggyDevices}</div>
          <div className="text-xs text-emerald-400 mt-2 flex items-center gap-1 font-medium">
            <BatteryCharging className="w-3.5 h-3.5" />
            <span>{stats.piggyBatteryHealth}% Battery Health Average</span>
          </div>
        </Card>

        <Card>
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Monthly Piggy Volume</span>
            <div className="p-2.5 bg-emerald-500/10 text-emerald-400 rounded-xl">
              <DollarSign className="w-5 h-5" />
            </div>
          </div>
          <div className="text-2xl font-extrabold text-white">{(stats.monthlyVolume).toLocaleString('vi-VN')} đ</div>
          <div className="text-xs text-slate-400 mt-2 flex items-center gap-1 font-medium">
            <TrendingUp className="w-3.5 h-3.5 text-emerald-400" />
            <span>Parent Bonus: {(stats.parentBonusPaid).toLocaleString('vi-VN')} đ</span>
          </div>
        </Card>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="p-6">
          <h2 className="text-base font-bold text-white mb-3 flex items-center gap-2">
            <ShieldCheck className="w-5 h-5 text-cyan-400" />
            Tenant Security & Access Rules
          </h2>
          <p className="text-xs text-slate-400 leading-relaxed mb-4">
            Staff access is constrained by Branch isolation. Branch Managers can only inspect IoT devices and transactions tied to their registered Province/Branch.
          </p>
          <div className="flex gap-2">
            <Badge variant="primary">Double-Entry Audited</Badge>
            <Badge variant="info">Zero-Bypass Solenoid</Badge>
            <Badge variant="success">Redis Token Revocation</Badge>
          </div>
        </Card>

        <Card className="p-6">
          <h2 className="text-base font-bold text-white mb-3 flex items-center gap-2">
            <Cpu className="w-5 h-5 text-indigo-400" />
            Fleet Firmware Version
          </h2>
          <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 space-y-2 text-xs">
            <div className="flex justify-between text-slate-300">
              <span>Firmware Version:</span>
              <span className="font-mono text-cyan-400 font-bold">ESP32-LIOCHIO-v2.4.1</span>
            </div>
            <div className="flex justify-between text-slate-300">
              <span>Solenoid Timeout Safeguard:</span>
              <span className="font-mono text-emerald-400 font-bold">60.0s Hardware Auto-Lock</span>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};
