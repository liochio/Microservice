import React from 'react';
import { Trophy, Star, Sparkles } from 'lucide-react';

interface LevelProgressProps {
  xp: number;
  level: number;
  levelTitle: string;
  xpToNextLevel: number;
}

export const LevelProgress: React.FC<LevelProgressProps> = ({ xp, level, levelTitle, xpToNextLevel }) => {
  const currentLevelMinXp = (level - 1) * 2000;
  const targetXp = level * 2000;
  const progressPct = Math.min(100, Math.max(0, ((xp - currentLevelMinXp) / (targetXp - currentLevelMinXp)) * 100));

  const ranks = [
    { lvl: 1, title: 'Heo Sơ Sinh', icon: '🐷' },
    { lvl: 2, title: 'Heo Chăm Chỉ', icon: '🐾' },
    { lvl: 3, title: 'Heo Cần Cù', icon: '🏅' },
    { lvl: 4, title: 'Heo Cao Thủ', icon: '👑' },
    { lvl: 5, title: 'Heo Triệu Phú', icon: '💎' },
  ];

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-amber-500/20 text-amber-400 border border-amber-500/40 flex items-center justify-center font-bold text-lg">
            <Trophy className="w-5 h-5" />
          </div>
          <div>
            <h3 className="font-bold text-sm text-slate-100 flex items-center gap-2">
              {levelTitle}
              <span className="text-xs px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300 font-mono">
                Cấp {level}
              </span>
            </h3>
            <p className="text-xs text-slate-400 font-mono">
              {xp.toLocaleString()} / {targetXp.toLocaleString()} XP (Quy đổi: 1,000 đ = 1 XP)
            </p>
          </div>
        </div>

        <div className="text-right">
          <span className="text-xs font-semibold text-emerald-400 flex items-center gap-1">
            <Sparkles className="w-3.5 h-3.5" /> Còn {(targetXp - xp).toLocaleString()} XP lên Cấp {level + 1}
          </span>
        </div>
      </div>

      {/* Progress Bar */}
      <div className="w-full bg-slate-900 rounded-full h-3.5 p-0.5 border border-slate-800 overflow-hidden">
        <div
          className="bg-gradient-to-r from-emerald-500 via-cyan-400 to-amber-400 h-full rounded-full transition-all duration-500 shadow-glow"
          style={{ width: `${progressPct}%` }}
        />
      </div>

      {/* Ranks Milestones */}
      <div className="grid grid-cols-5 gap-2 pt-2 border-t border-slate-800/80">
        {ranks.map((r) => (
          <div
            key={r.lvl}
            className={`p-2 rounded-lg text-center transition-all ${
              level >= r.lvl
                ? 'bg-emerald-950/40 border border-emerald-800/60 text-emerald-300'
                : 'bg-slate-900/30 border border-slate-800/40 text-slate-600'
            }`}
          >
            <div className="text-lg">{r.icon}</div>
            <div className="text-[10px] font-bold mt-1">Cấp {r.lvl}</div>
          </div>
        ))}
      </div>
    </div>
  );
};
