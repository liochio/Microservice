import React, { useEffect, useRef } from 'react';
import { GaussianKdeModel } from '../../types/ai';
import { BrainCircuit, Clock, ShieldCheck } from 'lucide-react';

interface GaussianKdeChartProps {
  model: GaussianKdeModel;
}

export const GaussianKdeChart: React.FC<GaussianKdeChartProps> = ({ model }) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const width = canvas.width;
    const height = canvas.height;
    const padding = 40;

    // Clear
    ctx.clearRect(0, 0, width, height);

    // Draw Grid
    ctx.strokeStyle = '#1e293b';
    ctx.lineWidth = 1;
    for (let x = padding; x < width - padding; x += 50) {
      ctx.beginPath();
      ctx.moveTo(x, padding);
      ctx.lineTo(x, height - padding);
      ctx.stroke();
    }
    for (let y = padding; y < height - padding; y += 35) {
      ctx.beginPath();
      ctx.moveTo(padding, y);
      ctx.lineTo(width - padding, y);
      ctx.stroke();
    }

    const data = model.distribution;
    if (data.length === 0) return;

    const maxDensity = Math.max(...data.map((d) => d.probabilityDensity), 0.3);
    const maxDays = 14;

    const getCanvasX = (days: number) => padding + (days / maxDays) * (width - 2 * padding);
    const getCanvasY = (density: number) => height - padding - (density / maxDensity) * (height - 2 * padding);

    // Draw Area Fill (Gradient)
    const gradient = ctx.createLinearGradient(0, padding, 0, height - padding);
    gradient.addColorStop(0, 'rgba(16, 185, 129, 0.45)');
    gradient.addColorStop(1, 'rgba(16, 185, 129, 0.02)');

    ctx.beginPath();
    ctx.moveTo(getCanvasX(data[0].intervalDays), height - padding);
    data.forEach((pt) => {
      ctx.lineTo(getCanvasX(pt.intervalDays), getCanvasY(pt.probabilityDensity));
    });
    ctx.lineTo(getCanvasX(data[data.length - 1].intervalDays), height - padding);
    ctx.closePath();
    ctx.fillStyle = gradient;
    ctx.fill();

    // Draw Line
    ctx.beginPath();
    data.forEach((pt, i) => {
      const x = getCanvasX(pt.intervalDays);
      const y = getCanvasY(pt.probabilityDensity);
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    });
    ctx.strokeStyle = '#34d399';
    ctx.lineWidth = 3;
    ctx.stroke();

    // Draw Median Marker (Cyan)
    const medX = getCanvasX(model.medianIntervalDays);
    ctx.beginPath();
    ctx.setLineDash([4, 4]);
    ctx.moveTo(medX, padding);
    ctx.lineTo(medX, height - padding);
    ctx.strokeStyle = '#06b6d4';
    ctx.lineWidth = 2;
    ctx.stroke();

    // Draw P90 Anomaly Threshold (Amber)
    const p90X = getCanvasX(model.p90LateThresholdDays);
    ctx.beginPath();
    ctx.setLineDash([4, 4]);
    ctx.moveTo(p90X, padding);
    ctx.lineTo(p90X, height - padding);
    ctx.strokeStyle = '#f59e0b';
    ctx.lineWidth = 2;
    ctx.stroke();
    ctx.setLineDash([]); // reset

    // Labels
    ctx.fillStyle = '#94a3b8';
    ctx.font = '10px JetBrains Mono';
    ctx.fillText('0 ngày', padding - 10, height - padding + 15);
    ctx.fillText('7 ngày', getCanvasX(7) - 15, height - padding + 15);
    ctx.fillText('14 ngày', width - padding - 15, height - padding + 15);
  }, [model]);

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-bold text-base text-slate-100 flex items-center gap-2">
          <BrainCircuit className="w-5 h-5 text-emerald-400" />
          Phân Phối Thói Quen Nạp Tiền (Gaussian KDE Model)
        </h3>
        <span className="text-xs px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 font-mono">
          Non-parametric AI
        </span>
      </div>

      <p className="text-xs text-slate-400">
        Mô hình AI học phân phối xác suất khoảng cách giữa các lần nạp để phát hiện thói quen và gửi lời nhắc nhở cá nhân hóa vào thời điểm tối ưu.
      </p>

      {/* Canvas */}
      <div className="bg-slate-950/80 rounded-xl p-3 border border-slate-800/80">
        <canvas ref={canvasRef} width={600} height={220} className="w-full h-auto" />
      </div>

      {/* Legend & Stats */}
      <div className="grid grid-cols-3 gap-3 text-xs">
        <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800">
          <span className="text-slate-400">Trung Vị (Median):</span>
          <p className="font-bold text-cyan-400 text-sm mt-0.5 font-mono">
            {model.medianIntervalDays.toFixed(1)} ngày
          </p>
        </div>
        <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800">
          <span className="text-slate-400">Ngưỡng Trễ (P90):</span>
          <p className="font-bold text-amber-400 text-sm mt-0.5 font-mono">
            {model.p90LateThresholdDays.toFixed(1)} ngày
          </p>
        </div>
        <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800">
          <span className="text-slate-400">Dự Đoán Lần Nạp Kế:</span>
          <p className="font-bold text-emerald-400 text-sm mt-0.5 font-mono">
            {model.predictedNextDepositDays.toFixed(1)} ngày
          </p>
        </div>
      </div>

      {/* Robo Advisor Notification Box */}
      <div className="p-3.5 rounded-xl bg-emerald-950/30 border border-emerald-800/50 flex items-start gap-2.5 text-xs text-emerald-200">
        <Clock className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
        <p>{model.recommendedPrompt}</p>
      </div>
    </div>
  );
};
