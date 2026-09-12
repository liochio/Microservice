import React, { useEffect, useState } from 'react';
import { WatchdogTelemetry } from '../../types/security';
import { securityApi } from '../../api/securityApi';
import { Activity, Wifi, BatteryCharging, Radio } from 'lucide-react';
import { Badge } from '../common/Badge';

export const WatchdogStatus: React.FC = () => {
  const [telemetry, setTelemetry] = useState<WatchdogTelemetry | null>(null);

  useEffect(() => {
    securityApi.getWatchdogTelemetry().then(setTelemetry);
    const interval = setInterval(() => {
      securityApi.getWatchdogTelemetry().then(setTelemetry);
    }, 5000);
    return () => clearInterval(interval);
  }, []);

  if (!telemetry) return null;

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-bold text-base text-slate-100 flex items-center gap-2">
          <Activity className="w-5 h-5 text-emerald-400" />
          Giám Sát Phần Cứng & Heartbeat Watchdog (30s)
        </h3>
        <Badge variant={telemetry.status === 'ONLINE' ? 'emerald' : 'rose'} pulse>
          <Radio className="w-3.5 h-3.5" />
          {telemetry.status}
        </Badge>
      </div>

      <div className="grid grid-cols-4 gap-3 text-xs">
        <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800">
          <span className="text-slate-400">Heartbeat Gần Nhất:</span>
          <p className="font-bold text-emerald-400 font-mono mt-0.5">{telemetry.lastHeartbeatSecondsAgo}s trước</p>
        </div>

        <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800">
          <span className="text-slate-400">Độ Trễ Ping:</span>
          <p className="font-bold text-cyan-400 font-mono mt-0.5">{telemetry.pingMs} ms</p>
        </div>

        <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800">
          <span className="text-slate-400">Pin Thiết Bị:</span>
          <p className="font-bold text-amber-400 font-mono mt-0.5">{telemetry.batteryPct}%</p>
        </div>

        <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800">
          <span className="text-slate-400">Sóng Wi-Fi:</span>
          <p className="font-bold text-slate-200 font-mono mt-0.5">{telemetry.wifiRssi} dBm</p>
        </div>
      </div>
    </div>
  );
};
