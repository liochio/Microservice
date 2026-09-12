import React, { useState } from 'react';
import { FileText, RefreshCw } from 'lucide-react';
import { Badge } from '../components/common/CardAndBadge';
import { Button } from '../components/common/Button';

export const AuditLogsPage: React.FC = () => {
  const [logs] = useState([
    {
      id: 'log-9921',
      action: 'UPDATE_GLOBAL_LIMITS',
      actor: 'superadmin_root',
      ip: '10.0.4.12',
      details: 'Updated maxDailyTxCeiling to 200,000,000 VND',
      timestamp: '2026-09-10 12:30:15',
      status: 'SUCCESS',
    },
    {
      id: 'log-9920',
      action: 'PROVISION_TENANT',
      actor: 'superadmin_root',
      ip: '10.0.4.12',
      details: 'Provisioned new tenant: Techcombank Junior Savings (TCB-KIDS)',
      timestamp: '2026-09-10 11:15:02',
      status: 'SUCCESS',
    },
    {
      id: 'log-9919',
      action: 'OUTBOX_EVENT_PUBLISHED',
      actor: 'system_cdc_worker',
      ip: '127.0.0.1',
      details: 'Dispatched EventId: 44819 to Kafka topic: ledger.postings',
      timestamp: '2026-09-10 10:45:22',
      status: 'SUCCESS',
    },
  ]);

  return (
    <div className="space-y-6 animate-fade-in max-w-6xl">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <FileText className="w-7 h-7 text-amber-400" />
            Immutable Audit Trail & Outbox
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Tamper-proof system audit logs and transactional outbox event streams
          </p>
        </div>
        <Button variant="outline" className="flex items-center gap-2">
          <RefreshCw className="w-4 h-4" />
          Refresh
        </Button>
      </div>

      <div className="bg-slate-900/70 border border-slate-800/80 rounded-2xl overflow-hidden shadow-xl">
        <table className="w-full text-left text-xs text-slate-300">
          <thead className="bg-slate-950/80 text-slate-400 uppercase tracking-wider text-[10px] font-bold border-b border-slate-800">
            <tr>
              <th className="py-3 px-4">Log ID</th>
              <th className="py-3 px-4">Action</th>
              <th className="py-3 px-4">Actor</th>
              <th className="py-3 px-4">Details</th>
              <th className="py-3 px-4">IP Address</th>
              <th className="py-3 px-4">Timestamp</th>
              <th className="py-3 px-4">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {logs.map(log => (
              <tr key={log.id} className="hover:bg-slate-800/30 transition">
                <td className="py-3.5 px-4 font-mono text-amber-400 font-semibold">{log.id}</td>
                <td className="py-3.5 px-4 font-bold text-white">{log.action}</td>
                <td className="py-3.5 px-4 text-indigo-400 font-mono">{log.actor}</td>
                <td className="py-3.5 px-4 text-slate-300">{log.details}</td>
                <td className="py-3.5 px-4 font-mono text-slate-400">{log.ip}</td>
                <td className="py-3.5 px-4 text-slate-400">{log.timestamp}</td>
                <td className="py-3.5 px-4">
                  <Badge variant="success">{log.status}</Badge>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
