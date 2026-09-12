import React, { useState } from 'react';
import { useAudio } from '../../hooks/useAudio';
import { securityApi } from '../../api/securityApi';
import { Button } from '../common/Button';
import { Modal } from '../common/Modal';
import { useToast } from '../common/Toast';
import { AlertOctagon, ShieldCheck, KeyRound } from 'lucide-react';

export const TamperSiren: React.FC<{ isFrozen: boolean; onStateChanged: () => void }> = ({
  isFrozen,
  onStateChanged,
}) => {
  const [isOtpOpen, setIsOtpOpen] = useState<boolean>(false);
  const [otp, setOtp] = useState<string>('8888');
  const [isUnfreezing, setIsUnfreezing] = useState<boolean>(false);
  const { playAlarmSiren } = useAudio();
  const { success, error } = useToast();

  const handleSimulateVibration = async () => {
    playAlarmSiren();
    await securityApi.triggerTamperAlert('ESP32-PIGGY-01');
    error('CẢNH BÁO RUNG LẮC MPU6050!', 'Phát hiện lực tác động bất thường. Ví Heo Đất đã tự động ĐÓNG BĂNG để chống trộm.');
    onStateChanged();
  };

  const handleUnfreeze = async () => {
    setIsUnfreezing(true);
    try {
      const res = await securityApi.unfreezeWalletOtp(otp);
      if (res.success) {
        success('Mở Khóa Ví Thành Công!', res.message);
        setIsOtpOpen(false);
        onStateChanged();
      } else {
        error('Mở Khóa Thất Bại', res.message);
      }
    } finally {
      setIsUnfreezing(false);
    }
  };

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-bold text-base text-slate-100 flex items-center gap-2">
          <AlertOctagon className="w-5 h-5 text-rose-400" />
          Cảm Biến Rung MPU6050 & Đóng Băng Khẩn Cấp
        </h3>
        {isFrozen && (
          <span className="text-xs px-2.5 py-1 rounded-full bg-rose-500/20 text-rose-400 border border-rose-500/40 font-bold animate-pulse">
            VÍ ĐANG ĐÓNG BĂNG
          </span>
        )}
      </div>

      <p className="text-xs text-slate-400">
        Cảm biến gia tốc MPU6050 liên tục đo lường rung lắc. Khi bị cạy phá hoặc di chuyển bất thường, hệ thống tự động hú còi và khóa ví.
      </p>

      <div className="flex gap-3">
        <Button variant="danger" size="md" onClick={handleSimulateVibration} leftIcon={<AlertOctagon className="w-4 h-4" />}>
          Mô Phỏng Cạy Phá / Rung Lắc Heo
        </Button>
        <Button variant="outline" size="md" onClick={() => setIsOtpOpen(true)} leftIcon={<KeyRound className="w-4 h-4" />}>
          Mở Khóa Ví Bằng OTP Phụ Huynh
        </Button>
      </div>

      {/* OTP Unfreeze Modal */}
      <Modal isOpen={isOtpOpen} onClose={() => setIsOtpOpen(false)} title="Mở Khóa Đóng Băng Ví Bằng OTP Phụ Huynh">
        <div className="space-y-4">
          <p className="text-xs text-slate-300">
            Nhập mã OTP xác thực an toàn gửi về số điện thoại Phụ Huynh để mở lại ví về trạng thái ACTIVE.
          </p>
          <input
            type="text"
            value={otp}
            onChange={(e) => setOtp(e.target.value)}
            placeholder="Nhập mã OTP (demo: 8888)"
            className="w-full px-4 py-3 rounded-xl bg-slate-900 border border-slate-700 text-center text-xl font-mono font-bold text-emerald-400 focus:outline-none focus:border-emerald-500"
          />
          <div className="flex justify-end gap-3 pt-3">
            <Button variant="outline" onClick={() => setIsOtpOpen(false)}>Hủy</Button>
            <Button variant="primary" isLoading={isUnfreezing} onClick={handleUnfreeze} leftIcon={<ShieldCheck className="w-4 h-4" />}>
              Xác Thực & Mở Khóa
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
