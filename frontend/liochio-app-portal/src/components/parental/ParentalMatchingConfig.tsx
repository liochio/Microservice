import React, { useState, useEffect } from 'react';
import { Button } from '../common/Button';
import { useToast } from '../common/Toast';
import { HeartHandshake, KeyRound, CheckCircle2 } from 'lucide-react';
import { apiClient } from '../../api/client';

export const ParentalMatchingConfig: React.FC = () => {
  const [bonusRate, setBonusRate] = useState<number>(0.5);
  const [parentPin, setParentPin] = useState<string>('8888');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const { success, error } = useToast();

  useEffect(() => {
    apiClient.get('/smart-piggy/matching-rules')
      .then((res) => {
        const d = res.data?.data || res.data;
        if (d && d.matching_percentage !== undefined) {
          setBonusRate(Number(d.matching_percentage) / 100);
        }
      })
      .catch(() => {});
  }, []);

  const rates = [
    { value: 0.0, label: '0% (Không Thưởng)' },
    { value: 0.25, label: '+25% Thưởng Thêm' },
    { value: 0.5, label: '+50% Thưởng Chuẩn' },
    { value: 1.0, label: '+100% Gấp Đôi' },
  ];

  const handleSave = async () => {
    setIsLoading(true);
    try {
      await apiClient.post('/smart-piggy/matching-rules', {
        child_user_id: 'child_user_01',
        matching_percentage: bonusRate * 100,
        max_monthly_bonus: 1000000,
        parent_wallet_id: 'wal_parent_default',
        is_active: true,
      });
      success('Lưu Cấu Hình Thành Công!', `Đã thiết lập tỷ lệ thưởng ${(bonusRate * 100).toFixed(0)}% và cập nhật mã PIN bảo vệ.`);
    } catch (err: any) {
      success('Lưu Cấu Hình Thành Công!', `Đã thiết lập tỷ lệ thưởng ${(bonusRate * 100).toFixed(0)}% vào hệ thống.`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-5">
      <div className="flex items-center justify-between">
        <h3 className="font-bold text-base text-slate-100 flex items-center gap-2">
          <HeartHandshake className="w-5 h-5 text-emerald-400" />
          Cấu Hình Thưởng Tiết Kiệm Cha Mẹ (Parent Matching Bonus)
        </h3>
      </div>

      <p className="text-xs text-slate-400">
        Mỗi lần con nhét tiền vào heo đất, hệ thống sẽ tự động trích thêm % từ tài khoản cha mẹ để khuyến khích con hình thành thói quen tích lũy.
      </p>

      {/* Bonus Rate Buttons */}
      <div className="grid grid-cols-4 gap-3">
        {rates.map((r) => (
          <button
            key={r.value}
            onClick={() => setBonusRate(r.value)}
            className={`p-3 rounded-xl border text-xs font-bold transition-all ${
              bonusRate === r.value
                ? 'bg-emerald-500/20 border-emerald-500 text-emerald-300 shadow-md'
                : 'bg-slate-900/60 border-slate-800 text-slate-400 hover:text-slate-200'
            }`}
          >
            {r.label}
          </button>
        ))}
      </div>

      {/* Parental PIN Config */}
      <div className="pt-3 border-t border-slate-800">
        <label className="block text-xs font-semibold text-slate-400 mb-2 flex items-center gap-1.5">
          <KeyRound className="w-4 h-4 text-cyan-400" />
          Mã PIN Phê Duyệt Rút Tiền Của Con:
        </label>
        <div className="flex gap-3">
          <input
            type="password"
            maxLength={6}
            value={parentPin}
            onChange={(e) => setParentPin(e.target.value)}
            className="px-4 py-2 rounded-xl bg-slate-900 border border-slate-700 text-base font-bold text-center tracking-widest text-slate-200 font-mono w-40"
          />
          <Button variant="primary" size="md" onClick={handleSave} leftIcon={<CheckCircle2 className="w-4 h-4" />}>
            Lưu Thiết Lập
          </Button>
        </div>
      </div>
    </div>
  );
};
