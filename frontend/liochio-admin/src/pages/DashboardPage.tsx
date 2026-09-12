import React, { useState } from 'react';
import { 
  Building2, 
  Cpu, 
  Users, 
  DollarSign, 
  ArrowUpRight, 
  CheckCircle2, 
  Server,
  Zap,
  Activity
} from 'lucide-react';
import { Card, Badge } from '../components/common/CardAndBadge';

export const DashboardPage: React.FC = () => {
  const [stats] = useState({
    totalTenants: 12,
    activeTenants: 11,
    totalDevices: 1540,
    onlineDevices: 1482,
    totalUsers: 48500,
    dailyTxVolume: 2450000000,
    systemHealth: 'HEALTHY',
  });

  return (
    <div className="space-y-8 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black tracking-tight text-white flex items-center gap-2">
            <span>Global Control Tower</span>
            <span className="text-xs px-2.5 py-0.5 rounded-md bg-amber-500/20 text-amber-300 font-mono font-medium border border-amber-500/30">
              PORT: 5170
            </span>
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Real-time monitoring across all B2B Tenants, Fleet IoT Hardware, and Core Banking Outbox
          </p>
        </div>
        <Badge variant="success" className="px-3 py-1 text-xs">
          <CheckCircle2 className="w-3.5 h-3.5 mr-1" />
          All Microservices Operational
        </Badge>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
        <Card>
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Enterprise Tenants</span>
            <div className="p-2.5 bg-blue-500/10 text-blue-400 rounded-xl">
              <Building2 className="w-5 h-5" />
            </div>
          </div>
          <div className="text-3xl font-extrabold text-white">{stats.totalTenants}</div>
          <div className="text-xs text-emerald-400 mt-2 flex items-center gap-1 font-medium">
            <ArrowUpRight className="w-3.5 h-3.5" />
            <span>{stats.activeTenants} Active &bull; 1 Under Review</span>
          </div>
        </Card>

        <Card>
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">IoT Heo Đất Fleet</span>
            <div className="p-2.5 bg-amber-500/10 text-amber-400 rounded-xl">
              <Cpu className="w-5 h-5" />
            </div>
          </div>
          <div className="text-3xl font-extrabold text-white">{stats.totalDevices.toLocaleString()}</div>
          <div className="text-xs text-emerald-400 mt-2 flex items-center gap-1 font-medium">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>{stats.onlineDevices} Online (96.2% Heartbeat)</span>
          </div>
        </Card>

        <Card>
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total End-Users</span>
            <div className="p-2.5 bg-indigo-500/10 text-indigo-400 rounded-xl">
              <Users className="w-5 h-5" />
            </div>
          </div>
          <div className="text-3xl font-extrabold text-white">{stats.totalUsers.toLocaleString()}</div>
          <div className="text-xs text-indigo-400 mt-2 flex items-center gap-1 font-medium">
            <Zap className="w-3.5 h-3.5" />
            <span>+1,250 registered this week</span>
          </div>
        </Card>

        <Card>
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">24h Ledger Volume</span>
            <div className="p-2.5 bg-emerald-500/10 text-emerald-400 rounded-xl">
              <DollarSign className="w-5 h-5" />
            </div>
          </div>
          <div className="text-2xl font-extrabold text-white">{(stats.dailyTxVolume).toLocaleString('vi-VN')} đ</div>
          <div className="text-xs text-slate-400 mt-2 flex items-center gap-1 font-medium">
            <Activity className="w-3.5 h-3.5 text-emerald-400" />
            <span>100% Zero-Bypass Verified</span>
          </div>
        </Card>
      </div>

      <Card className="p-6">
        <h2 className="text-base font-bold text-white mb-4 flex items-center gap-2">
          <Server className="w-4 h-4 text-amber-400" />
          Backend Microservices Matrix & Latency
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800 flex items-center justify-between">
            <div>
              <div className="text-xs text-slate-400">Spring Cloud Gateway</div>
              <div className="text-sm font-bold text-slate-200 mt-0.5">Port :8080</div>
            </div>
            <Badge variant="success">Active (3.8ms)</Badge>
          </div>

          <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800 flex items-center justify-between">
            <div>
              <div className="text-xs text-slate-400">Java IAM Auth Core</div>
              <div className="text-sm font-bold text-slate-200 mt-0.5">Port :8081</div>
            </div>
            <Badge variant="success">Active (4.5ms)</Badge>
          </div>

          <div className="p-4 rounded-xl bg-slate-950/60 border border-slate-800 flex items-center justify-between">
            <div>
              <div className="text-xs text-slate-400">Python FinTech & IoT Core</div>
              <div className="text-sm font-bold text-slate-200 mt-0.5">Port :8089</div>
            </div>
            <Badge variant="success">Active (6.1ms)</Badge>
          </div>
        </div>
      </Card>
    </div>
  );
};
