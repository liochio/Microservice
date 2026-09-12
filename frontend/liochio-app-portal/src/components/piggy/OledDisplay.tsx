import React from 'react';
import { PiggyState } from '../../types/piggy';
import { Cpu, Wifi, Lock, Unlock, AlertOctagon, Snowflake } from 'lucide-react';

interface OledDisplayProps {
  state: PiggyState;
  customMessage?: string;
}

export const OledDisplay: React.FC<OledDisplayProps> = ({ state, customMessage }) => {
  return (
    <div className="oled-screen rounded-2xl p-5 relative overflow-hidden">
      {/* Scanline overlay effect */}
      <div className="absolute inset-0 bg-[linear-gradient(rgba(18,16,16,0)_50%,rgba(0,0,0,0.25)_50%)] bg-[length:100%_4px] pointer-events-none opacity-40 z-10" />

      {/* OLED Header */}
      <div className="flex items-center justify-between border-b border-emerald-950 pb-3 mb-3 text-xs">
        <div className="flex items-center gap-2 oled-text">
          <Cpu className="w-4 h-4 text-emerald-400" />
          <span className="font-bold tracking-wider">ESP32 OLED 128x64</span>
        </div>
        <div className="flex items-center gap-2">
          {state.isFrozen ? (
            <span className="flex items-center gap-1 oled-alert font-bold animate-pulse">
              <Snowflake className="w-3.5 h-3.5" /> FROZEN
            </span>
          ) : state.vibrationAlert ? (
            <span className="flex items-center gap-1 oled-alert font-bold animate-pulse">
              <AlertOctagon className="w-3.5 h-3.5" /> TAMPER!
            </span>
          ) : state.solenoidLocked ? (
            <span className="flex items-center gap-1 oled-text">
              <Lock className="w-3.5 h-3.5" /> NC_LOCKED
            </span>
          ) : (
            <span className="flex items-center gap-1 oled-amber font-bold animate-pulse">
              <Unlock className="w-3.5 h-3.5" /> OPEN_5V
            </span>
          )}

          <div className="flex items-center gap-1 text-[10px] text-emerald-600 pl-2 border-l border-emerald-950">
            <Wifi className="w-3 h-3 text-emerald-400" />
            <span>RSSI -58dBm</span>
          </div>
        </div>
      </div>

      {/* OLED Screen Pixel Matrix */}
      <div className="space-y-2 py-2">
        <div className="flex items-center justify-between">
          <span className="text-[11px] text-emerald-600 uppercase">TIẾT KIỆM HIỆN CÓ:</span>
          <span className="text-[10px] text-emerald-500 font-mono">ID: {state.deviceId}</span>
        </div>

        <div className="text-2xl md:text-3xl font-bold oled-text tracking-tight font-mono">
          {state.totalSaved.toLocaleString('vi-VN')} <span className="text-sm font-normal">VND</span>
        </div>

        {/* ASCII Piggy Animation & State text */}
        <div className="p-2.5 rounded-lg bg-emerald-950/40 border border-emerald-900/60 flex items-center justify-between">
          <div className="font-mono text-xs oled-text whitespace-pre">
            {state.isFrozen
              ? `( X _ X ) [KHOA VI]`
              : state.vibrationAlert
              ? `( > _ < ) [BAO DONG!]`
              : !state.solenoidLocked
              ? `( * ^ * ) [DANG MO KHOA]`
              : `( ^ . ^ ) [HEO VUI VE]`}
          </div>
          <div className="text-right">
            <span className="text-[11px] oled-amber font-bold">
              {customMessage || (state.isFrozen ? 'DA KHOA DONG BANG' : state.levelTitle)}
            </span>
          </div>
        </div>
      </div>

      {/* OLED Footer status ticker */}
      <div className="mt-3 pt-2 border-t border-emerald-950/80 flex items-center justify-between text-[10px] text-emerald-600">
        <span>XP: {state.xp.toLocaleString()}</span>
        <span>THƯỞNG BA MẸ: +{(state.parentMatchingBonusRate * 100).toFixed(0)}%</span>
        <span>WATCHDOG: OK</span>
      </div>
    </div>
  );
};
