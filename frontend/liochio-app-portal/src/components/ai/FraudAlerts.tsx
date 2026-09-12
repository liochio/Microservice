import React, { useEffect, useState } from 'react';
import { ZScoreAnomaly } from '../../types/ai';
import { aiApi } from '../../api/aiApi';
import { ShieldAlert, AlertTriangle } from 'lucide-react';
import { Badge } from '../common/Badge';

export const FraudAlerts: React.FC = () => {
  const [anomalies, setAnomalies] = useState<ZScoreAnomaly[]>([]);

  useEffect(() => {
    aiApi.getZScoreAnomalies().then(setAnomalies);
  }, []);

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-bold text-base text-slate-100 flex items-center gap-2">
          <ShieldAlert className="w-5 h-5 text-rose-400" />
          Phát Hiện Dị Thường Giao Dịch (Z-Score & Fraud Guard)
        </h3>
        <span className="text-xs px-2.5 py-1 rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/30 font-mono">
          Z &gt; 3.0σ Anomaly
        </span>
      </div>

      <div className="space-y-2.5">
        {anomalies.map((a) => (
          <div key={a.txId} className="p-3.5 rounded-xl bg-slate-900/70 border border-slate-800 flex items-start justify-between text-xs">
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <span className="font-mono font-bold text-slate-300">{a.txId}</span>
                <Badge variant={a.severity === 'CRITICAL_FRAUD' ? 'rose' : 'amber'}>
                  Z = {a.zScore.toFixed(1)}σ ({a.severity})
                </Badge>
              </div>
              <p className="text-slate-400">{a.flaggedReason}</p>
            </div>
            <div className="text-right">
              <span className="font-bold text-slate-200 font-mono">
                {a.amount.toLocaleString('vi-VN')} đ
              </span>
              <p className="text-[10px] text-slate-500 mt-0.5">
                {new Date(a.timestamp).toLocaleTimeString('vi-VN')}
              </p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
