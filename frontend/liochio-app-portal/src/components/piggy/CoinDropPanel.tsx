import React, { useState } from 'react';
import { Button } from '../common/Button';
import { useAudio } from '../../hooks/useAudio';
import { piggyApi } from '../../api/piggyApi';
import { useToast } from '../common/Toast';
import { ArrowDownCircle, Sparkles, Gift } from 'lucide-react';

interface CoinDropPanelProps {
  onSuccess: () => void;
  matchingBonusRate: number;
}

export const CoinDropPanel: React.FC<CoinDropPanelProps> = ({ onSuccess, matchingBonusRate }) => {
  const [isDropping, setIsDropping] = useState<boolean>(false);
  const [selectedDenom, setSelectedDenom] = useState<number>(50000);
  const { playCoinDrop } = useAudio();
  const { success } = useToast();

  const denominations = [
    { value: 10000, label: '10,000 đ' },
    { value: 20000, label: '20,000 đ' },
    { value: 50000, label: '50,000 đ' },
    { value: 100000, label: '100,000 đ' },
    { value: 200000, label: '200,000 đ' },
    { value: 500000, label: '500,000 đ' },
  ];

  const handleDropCoin = async () => {
    setIsDropping(true);
    try {
      const res = await piggyApi.dropCoin({ denomination: selectedDenom }, matchingBonusRate);
      playCoinDrop();
      success(
        'Nạp Tiền Thành Công!',
        `Đã nhét ${selectedDenom.toLocaleString('vi-VN')} đ + Thưởng Ba Mẹ ${res.bonusAmount.toLocaleString('vi-VN')} đ. Nhận +${res.xpEarned} XP!`
      );
      onSuccess();
    } finally {
      setIsDropping(false);
    }
  };

  const calculatedBonus = selectedDenom * matchingBonusRate;
  const totalCredited = selectedDenom + calculatedBonus;

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-5">
      <div className="flex items-center justify-between">
        <h3 className="font-bold text-base text-slate-100 flex items-center gap-2">
          <ArrowDownCircle className="w-5 h-5 text-emerald-400" />
          Giả Lập Khe Nhét Tiền Vật Lý (Coin Slot)
        </h3>
        <span className="text-xs px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 flex items-center gap-1 font-mono">
          <Gift className="w-3.5 h-3.5" />
          +{(matchingBonusRate * 100).toFixed(0)}% Thưởng Cha Mẹ
        </span>
      </div>

      <p className="text-xs text-slate-400">
        Chọn mệnh giá tiền mặt thả vào khe heo đất. ESP32 sẽ lập tức quét cảm biến hồng ngoại, phát âm thanh và cộng dồn điểm tích lũy.
      </p>

      {/* Denominations Grid */}
      <div className="grid grid-cols-3 gap-2.5">
        {denominations.map((d) => (
          <button
            key={d.value}
            onClick={() => setSelectedDenom(d.value)}
            className={`p-3 rounded-xl border text-sm font-semibold transition-all ${
              selectedDenom === d.value
                ? 'bg-emerald-500/20 border-emerald-500 text-emerald-300 shadow-md shadow-emerald-950/30 scale-[1.02]'
                : 'bg-slate-900/60 border-slate-800 text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
            }`}
          >
            {d.label}
          </button>
        ))}
      </div>

      {/* Bonus Calculation Card */}
      <div className="p-3.5 rounded-xl bg-slate-900/90 border border-slate-800 flex items-center justify-between text-xs font-mono">
        <div>
          <span className="text-slate-400">Thực nhận vào Heo:</span>
          <p className="text-base font-bold text-emerald-400 mt-0.5">
            {totalCredited.toLocaleString('vi-VN')} đ
          </p>
        </div>
        <div className="text-right">
          <span className="text-slate-500">Thưởng cha mẹ:</span>
          <p className="text-xs font-semibold text-amber-400 mt-0.5">
            +{calculatedBonus.toLocaleString('vi-VN')} đ
          </p>
        </div>
      </div>

      {/* Action Button */}
      <Button
        variant="primary"
        size="lg"
        className="w-full"
        isLoading={isDropping}
        onClick={handleDropCoin}
        leftIcon={<Sparkles className="w-5 h-5" />}
      >
        Nhét Tiền Vào Heo Đất (Ting Ting)
      </Button>
    </div>
  );
};
