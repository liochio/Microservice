import React, { useState } from 'react';
import { WatchdogStatus } from '../../../components/security/WatchdogStatus';
import { TamperSiren } from '../../../components/security/TamperSiren';
import { CrashWriteOff } from '../../../components/security/CrashWriteOff';
import { QrPairingModal } from '../../../components/security/QrPairingModal';
import { Button } from '../../../components/common/Button';
import { QrCode } from 'lucide-react';

export const SecurityPage: React.FC = () => {
  const [isFrozen, setIsFrozen] = useState<boolean>(false);
  const [isQrOpen, setIsQrOpen] = useState<boolean>(false);

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-white">Trung Tâm An Ninh & Giám Sát Phần Cứng</h2>
          <p className="text-xs text-slate-400">
            Heartbeat Watchdog 30s, Cảm biến rung MPU6050, Quy trình gỡ đóng băng OTP và Xử lý rơi vỡ Write-Off.
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={() => setIsQrOpen(true)} leftIcon={<QrCode className="w-4 h-4" />}>
          Ghép Đôi Thiết Bị
        </Button>
      </div>

      <WatchdogStatus />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <TamperSiren isFrozen={isFrozen} onStateChanged={() => setIsFrozen(!isFrozen)} />
        <CrashWriteOff />
      </div>

      <QrPairingModal
        isOpen={isQrOpen}
        onClose={() => setIsQrOpen(false)}
        deviceId="ESP32-PIGGY-01"
      />
    </div>
  );
};
