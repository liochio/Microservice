import React, { useEffect, useState } from 'react';
import { OledDisplay } from '../../../components/piggy/OledDisplay';
import { CoinDropPanel } from '../../../components/piggy/CoinDropPanel';
import { WithdrawalModal } from '../../../components/piggy/WithdrawalModal';
import { LevelProgress } from '../../../components/piggy/LevelProgress';
import { GoalsQuests } from '../../../components/piggy/GoalsQuests';
import { Button } from '../../../components/common/Button';
import { piggyApi } from '../../../api/piggyApi';
import { PiggyState } from '../../../types/piggy';
import { Unlock, QrCode } from 'lucide-react';
import { QrPairingModal } from '../../../components/security/QrPairingModal';

export const SmartPiggyPage: React.FC = () => {
  const [piggyState, setPiggyState] = useState<PiggyState | null>(null);
  const [isWithdrawalOpen, setIsWithdrawalOpen] = useState<boolean>(false);
  const [isQrOpen, setIsQrOpen] = useState<boolean>(false);

  const loadPiggy = () => {
    piggyApi.getPiggyState().then(setPiggyState);
  };

  useEffect(() => {
    loadPiggy();
  }, []);

  if (!piggyState) return null;

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-white">Heo Đất Thông Minh & Rút Tiền 2 Pha</h2>
          <p className="text-xs text-slate-400">
            Tương tác phần cứng ESP32, nạp tiền tự động, tích điểm XP và chốt khóa Solenoid NC.
          </p>
        </div>

        <div className="flex gap-3">
          <Button variant="outline" size="sm" onClick={() => setIsQrOpen(true)} leftIcon={<QrCode className="w-4 h-4" />}>
            Ghép Đôi QR
          </Button>
          <Button variant="primary" size="sm" onClick={() => setIsWithdrawalOpen(true)} leftIcon={<Unlock className="w-4 h-4" />}>
            Rút Tiền Mặt (2-Phase)
          </Button>
        </div>
      </div>

      {/* Row 1: OLED Display + Coin Drop Panel */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <OledDisplay state={piggyState} />
        <CoinDropPanel onSuccess={loadPiggy} matchingBonusRate={piggyState.parentMatchingBonusRate} />
      </div>

      {/* Row 2: Level Progress + Savings Goals */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <LevelProgress
          xp={piggyState.xp}
          level={piggyState.level}
          levelTitle={piggyState.levelTitle}
          xpToNextLevel={piggyState.xpToNextLevel}
        />
        <GoalsQuests />
      </div>

      {/* Withdrawal Saga Modal */}
      <WithdrawalModal
        isOpen={isWithdrawalOpen}
        onClose={() => setIsWithdrawalOpen(false)}
        maxSavingsAmount={piggyState.totalSaved}
        onWithdrawalComplete={loadPiggy}
      />

      {/* QR Pairing Modal */}
      <QrPairingModal
        isOpen={isQrOpen}
        onClose={() => setIsQrOpen(false)}
        deviceId={piggyState.deviceId}
      />
    </div>
  );
};
