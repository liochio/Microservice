import React, { useEffect, useState } from 'react';
import { Budget503020Rule } from '../../types/ai';
import { aiApi } from '../../api/aiApi';
import { PieChart, CheckCircle2 } from 'lucide-react';

export const Budget503020: React.FC = () => {
  const [budget, setBudget] = useState<Budget503020Rule | null>(null);

  useEffect(() => {
    aiApi.getBudget503020().then(setBudget);
  }, []);

  if (!budget) return null;

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-bold text-base text-slate-100 flex items-center gap-2">
          <PieChart className="w-5 h-5 text-emerald-400" />
          Phân Bổ Ngân Sách Quy Tắc 50/30/20
        </h3>
        <span className="text-xs px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 font-mono">
          Điểm Sức Khỏe: {budget.healthScore}/100
        </span>
      </div>

      {/* 3 Categories Gauges */}
      <div className="grid grid-cols-3 gap-3">
        {/* 50% Needs */}
        <div className="p-3.5 rounded-xl bg-slate-900/60 border border-slate-800 space-y-1.5">
          <span className="text-xs text-slate-400">50% Thiết Yếu (Needs)</span>
          <p className="text-base font-bold text-emerald-400 font-mono">
            {budget.needsActual.toLocaleString('vi-VN')} đ
          </p>
          <div className="w-full bg-slate-950 rounded-full h-1.5 overflow-hidden">
            <div
              className="bg-emerald-400 h-full rounded-full"
              style={{ width: `${(budget.needsActual / budget.needsTarget) * 100}%` }}
            />
          </div>
          <span className="text-[10px] text-slate-500">Mục tiêu: {budget.needsTarget.toLocaleString('vi-VN')} đ</span>
        </div>

        {/* 30% Wants */}
        <div className="p-3.5 rounded-xl bg-slate-900/60 border border-slate-800 space-y-1.5">
          <span className="text-xs text-slate-400">30% Linh Hoạt (Wants)</span>
          <p className="text-base font-bold text-cyan-400 font-mono">
            {budget.wantsActual.toLocaleString('vi-VN')} đ
          </p>
          <div className="w-full bg-slate-950 rounded-full h-1.5 overflow-hidden">
            <div
              className="bg-cyan-400 h-full rounded-full"
              style={{ width: `${(budget.wantsActual / budget.wantsTarget) * 100}%` }}
            />
          </div>
          <span className="text-[10px] text-slate-500">Mục tiêu: {budget.wantsTarget.toLocaleString('vi-VN')} đ</span>
        </div>

        {/* 20% Savings */}
        <div className="p-3.5 rounded-xl bg-slate-900/60 border border-slate-800 space-y-1.5">
          <span className="text-xs text-slate-400">20% Tiết Kiệm (Savings)</span>
          <p className="text-base font-bold text-amber-400 font-mono">
            {budget.savingsActual.toLocaleString('vi-VN')} đ
          </p>
          <div className="w-full bg-slate-950 rounded-full h-1.5 overflow-hidden">
            <div
              className="bg-amber-400 h-full rounded-full"
              style={{ width: `${(budget.savingsActual / budget.savingsTarget) * 100}%` }}
            />
          </div>
          <span className="text-[10px] text-slate-500">Mục tiêu: {budget.savingsTarget.toLocaleString('vi-VN')} đ</span>
        </div>
      </div>

      {/* AI Recommendations */}
      <div className="p-3 rounded-xl bg-slate-900/40 border border-slate-800/80 space-y-1 text-xs text-slate-300">
        {budget.recommendations.map((rec, i) => (
          <p key={i} className="flex items-center gap-2">
            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
            <span>{rec}</span>
          </p>
        ))}
      </div>
    </div>
  );
};
