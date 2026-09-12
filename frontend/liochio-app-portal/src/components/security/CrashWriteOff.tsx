import React, { useState } from 'react';
import { Button } from '../common/Button';
import { securityApi } from '../../api/securityApi';
import { useToast } from '../common/Toast';
import { ShieldX, AlertTriangle, FileSpreadsheet } from 'lucide-react';

export const CrashWriteOff: React.FC = () => {
  const [amount, setAmount] = useState<number>(350000);
  const [isProcessing, setIsProcessing] = useState<boolean>(false);
  const { success } = useToast();

  const handleWriteOff = async () => {
    setIsProcessing(true);
    try {
      const res = await securityApi.submitCrashWriteOff({
        deviceId: 'ESP32-PIGGY-01',
        lostAmount: amount,
        reason: 'FATAL_CRASH_PHYSICAL_LOSS',
        approverAdminId: 'super_admin_01',
        notes: 'Heo bị rơi vỡ gia tốc >6.0G khiến thất thoát tiền mặt vật lý.',
      });
      success('Trừ Hao Tài Sản Thành Công!', `Đã hạch toán bút toán Write-Off: ${res.journalTxId}`);
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-bold text-base text-slate-100 flex items-center gap-2">
          <ShieldX className="w-5 h-5 text-rose-400" />
          Xử Lý Tài Sản Rơi Vỡ (Fatal Crash &gt;6G Write-Off)
        </h3>
        <span className="text-xs px-2.5 py-1 rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/30 font-mono">
          Admin Audit Only
        </span>
      </div>

      <p className="text-xs text-slate-400">
        Quy trình xử lý ngoại lệ khi con heo bị va đập hoặc phá hủy vật lý dẫn đến sai lệch tiền mặt thực tế và số dư ghi nhận.
      </p>

      <div className="flex items-center gap-3">
        <input
          type="number"
          value={amount}
          step={50000}
          onChange={(e) => setAmount(Number(e.target.value))}
          className="px-4 py-2 rounded-xl bg-slate-900 border border-slate-700 text-sm font-bold text-slate-200 font-mono"
        />
        <Button variant="danger" size="md" isLoading={isProcessing} onClick={handleWriteOff} leftIcon={<FileSpreadsheet className="w-4 h-4" />}>
          Hạch Toán Trừ Hao Write-Off
        </Button>
      </div>
    </div>
  );
};
