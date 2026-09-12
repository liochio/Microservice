import React, { useState } from 'react';
import { Cpu, BatteryCharging, ShieldAlert, Lock, Unlock, Wifi, WifiOff, RefreshCw } from 'lucide-react';
import { Card, Badge } from '../../components/common/CardAndBadge';
import { Button } from '../../components/common/Button';

export const IotFleetPage: React.FC = () => {
  const [devices, setDevices] = useState([
    {
      deviceId: 'PIGGY-ESP32-901',
      deviceName: 'Heo Đất Biệt Đội Siêu Nhân (Bé Bo)',
      childName: 'Nguyễn Duy Bo',
      branchName: 'Chi nhánh Hoàng Mai',
      batteryMv: 4120,
      batteryPct: 95,
      solenoidLocked: true,
      tamperAlert: false,
      isOnline: true,
      lastHeartbeat: '2 giây trước',
    },
    {
      deviceId: 'PIGGY-ESP32-902',
      deviceName: 'Heo Hồng Công Chúa (Bé Bông)',
      childName: 'Trần Bảo Anh',
      branchName: 'Chi nhánh Quận 1 Bến Thành',
      batteryMv: 3820,
      batteryPct: 65,
      solenoidLocked: true,
      tamperAlert: false,
      isOnline: true,
      lastHeartbeat: '5 giây trước',
    },
    {
      deviceId: 'PIGGY-ESP32-903',
      deviceName: 'Heo Xanh Pikachu (Bé Tí)',
      childName: 'Lê Hoàng Phúc',
      branchName: 'Chi nhánh Hải Châu Đà Nẵng',
      batteryMv: 3450,
      batteryPct: 20,
      solenoidLocked: true,
      tamperAlert: true,
      isOnline: false,
      lastHeartbeat: '15 phút trước',
    },
  ]);

  const toggleRemoteLock = (deviceId: string) => {
    setDevices(devices.map(d => {
      if (d.deviceId === deviceId) {
        return { ...d, solenoidLocked: !d.solenoidLocked };
      }
      return d;
    }));
  };

  return (
    <div className="space-y-6 animate-fade-in max-w-6xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <Cpu className="w-7 h-7 text-cyan-400" />
            Smart Piggy IoT Fleet Monitor
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Real-time telemetry from ESP32 TP4056 Battery, SW-420 Tamper Vibration, and Solenoid Lock status
          </p>
        </div>
        <Button variant="outline" className="flex items-center gap-2">
          <RefreshCw className="w-4 h-4" />
          Live Telemetry Refresh
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {devices.map(dev => (
          <Card key={dev.deviceId} className="p-5 flex flex-col justify-between space-y-4">
            <div>
              <div className="flex items-start justify-between">
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs font-bold text-cyan-400 bg-cyan-500/10 px-2 py-0.5 rounded border border-cyan-500/20">
                      {dev.deviceId}
                    </span>
                    {dev.isOnline ? (
                      <span className="flex items-center gap-1 text-[10px] text-emerald-400">
                        <Wifi className="w-3 h-3" /> Online
                      </span>
                    ) : (
                      <span className="flex items-center gap-1 text-[10px] text-rose-400">
                        <WifiOff className="w-3 h-3" /> Offline
                      </span>
                    )}
                  </div>
                  <h3 className="font-bold text-white text-sm mt-2">{dev.deviceName}</h3>
                </div>
              </div>

              {dev.tamperAlert && (
                <div className="mt-3 p-2.5 rounded-xl bg-rose-500/20 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2 animate-pulse">
                  <ShieldAlert className="w-4 h-4 shrink-0 text-rose-400" />
                  <span>Cảnh báo rung lắc / Cạy nắp bất thường!</span>
                </div>
              )}

              <div className="mt-4 space-y-2.5 text-xs text-slate-300">
                <div className="flex justify-between">
                  <span className="text-slate-400">Assigned Child:</span>
                  <span className="font-bold text-white">{dev.childName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Branch:</span>
                  <span className="text-slate-200">{dev.branchName}</span>
                </div>

                <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 space-y-2">
                  <div className="flex justify-between items-center text-[11px]">
                    <span className="text-slate-400 flex items-center gap-1">
                      <BatteryCharging className="w-3.5 h-3.5 text-cyan-400" />
                      TP4056 Battery:
                    </span>
                    <span className="font-mono font-bold text-white">{dev.batteryPct}% ({dev.batteryMv}mV)</span>
                  </div>
                  <div className="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
                    <div 
                      className={`h-full rounded-full ${dev.batteryPct > 40 ? 'bg-emerald-500' : 'bg-rose-500'}`}
                      style={{ width: `${dev.batteryPct}%` }}
                    />
                  </div>
                </div>

                <div className="flex justify-between items-center pt-2 border-t border-slate-800">
                  <span className="text-slate-400">Solenoid State:</span>
                  <Badge variant={dev.solenoidLocked ? 'success' : 'warning'}>
                    {dev.solenoidLocked ? 'NC_CLOSED (Locked)' : 'NC_OPEN (Unlatched)'}
                  </Badge>
                </div>
              </div>
            </div>

            <div className="pt-2 border-t border-slate-800">
              <button
                onClick={() => toggleRemoteLock(dev.deviceId)}
                className={`w-full py-2 rounded-xl text-xs font-semibold flex items-center justify-center gap-2 transition cursor-pointer ${
                  dev.solenoidLocked
                    ? 'bg-amber-500/10 text-amber-300 hover:bg-amber-500/20 border border-amber-500/20'
                    : 'bg-emerald-500/10 text-emerald-300 hover:bg-emerald-500/20 border border-emerald-500/20'
                }`}
              >
                {dev.solenoidLocked ? <Unlock className="w-3.5 h-3.5" /> : <Lock className="w-3.5 h-3.5" />}
                {dev.solenoidLocked ? 'Emergency Test Unlock' : 'Force Solenoid Lock'}
              </button>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
};
