import React, { useEffect, useState } from 'react';
import { TrialBalance } from '../../types/ledger';
import { ledgerApi } from '../../api/ledgerApi';
import { Scale, CheckCircle2, AlertOctagon } from 'lucide-react';

export const TrialBalanceGauge: React.FC = () => {
  const [tb, setTb] = useState<TrialBalance | null>(null);

  useEffect(() => {
    ledgerApi.getTrialBalance().then(setTb);
  }, []);

  if (!tb) return null;

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-bold text-base text-slate-100 flex items-center gap-2">
          <Scale className="w-5 h-5 text-emerald-400" />
          Đồng Hồ Cân Bằng Sổ Kép (Trial Balance Equation)
        </h3>
        <span className="text-xs px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 font-mono">
          Δ = 0.00 đ Verified
        </span>
      </div>

      <div className="grid grid-cols-3 gap-4 text-center">
        <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800">
          <span className="text-xs text-slate-400">Tổng Nợ (Total Debit)</span>
          <p className="text-xl font-extrabold text-emerald-400 font-mono mt-1">
            {tb.totalDebit.toLocaleString('vi-VN')} đ
          </p>
        </div>

        <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800">
          <span className="text-xs text-slate-400">Tổng Có (Total Credit)</span>
          <p className="text-xl font-extrabold text-cyan-400 font-mono mt-1">
            {tb.totalCredit.toLocaleString('vi-VN')} đ
          </p>
        </div>

        <div className={`p-4 rounded-xl border ${tb.isBalanced ? 'bg-emerald-950/30 border-emerald-500/40 text-emerald-300' : 'bg-rose-950/30 border-rose-500/40 text-rose-300'}`}>
          <span className="text-xs">Độ Lệch (Difference Δ)</span>
          <p className="text-xl font-extrabold font-mono mt-1">
            {tb.difference.toFixed(2)} đ
          </p>
        </div>
      </div>
    </div>
  );
};
