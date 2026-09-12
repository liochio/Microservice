import React, { useState, useEffect } from 'react';
import { SavingsGoal } from '../../types/piggy';
import { piggyApi } from '../../api/piggyApi';
import { Target, CheckCircle2, Plus } from 'lucide-react';
import { Button } from '../common/Button';

export const GoalsQuests: React.FC = () => {
  const [goals, setGoals] = useState<SavingsGoal[]>([]);

  useEffect(() => {
    piggyApi.getSavingsGoals().then(setGoals);
  }, []);

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-bold text-base text-slate-100 flex items-center gap-2">
          <Target className="w-5 h-5 text-cyan-400" />
          Hũ Mục Tiêu Tiết Kiệm (Quests)
        </h3>
        <Button variant="outline" size="sm" leftIcon={<Plus className="w-3.5 h-3.5" />}>
          Thêm Mục Tiêu
        </Button>
      </div>

      <div className="space-y-3">
        {goals.map((g) => {
          const pct = Math.min(100, Math.round((g.currentAmount / g.targetAmount) * 100));
          return (
            <div key={g.id} className="p-3.5 rounded-xl bg-slate-900/60 border border-slate-800 space-y-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <span className="text-xl">{g.icon}</span>
                  <div>
                    <h4 className="text-xs font-bold text-slate-200">{g.title}</h4>
                    <p className="text-[11px] text-slate-400 font-mono">
                      {g.currentAmount.toLocaleString('vi-VN')} đ / {g.targetAmount.toLocaleString('vi-VN')} đ
                    </p>
                  </div>
                </div>
                {g.completed ? (
                  <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 text-[10px] font-bold border border-emerald-500/40 flex items-center gap-1">
                    <CheckCircle2 className="w-3 h-3" /> Đạt 100%
                  </span>
                ) : (
                  <span className="text-xs font-bold font-mono text-cyan-400">{pct}%</span>
                )}
              </div>

              <div className="w-full bg-slate-950 rounded-full h-2 overflow-hidden">
                <div
                  className={`h-full rounded-full transition-all duration-500 ${
                    g.completed ? 'bg-emerald-400' : 'bg-gradient-to-r from-cyan-500 to-emerald-400'
                  }`}
                  style={{ width: `${pct}%` }}
                />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
