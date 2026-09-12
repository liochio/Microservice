import React, { useState } from 'react';
import { Modal } from '../common/Modal';
import { Button } from '../common/Button';
import { useToast } from '../common/Toast';
import { QrCode, RefreshCw, Unlink, CheckCircle2 } from 'lucide-react';

interface QrPairingModalProps {
  isOpen: boolean;
  onClose: () => void;
  deviceId: string;
}

export const QrPairingModal: React.FC<QrPairingModalProps> = ({ isOpen, onClose, deviceId }) => {
  const [deviceToken, setDeviceToken] = useState<string>('HMAC_SHA256_LIOCHIO_DEV_982314');
  const [isResetting, setIsResetting] = useState<boolean>(false);
  const { success, warning } = useToast();

  const handleHardReset = () => {
    setIsResetting(true);
    setTimeout(() => {
      setIsResetting(false);
      setDeviceToken(`HMAC_SHA256_LIOCHIO_DEV_${Math.floor(100000 + Math.random() * 900000)}`);
      warning('Hard-Reset 15s Thành Công', 'Thiết bị đã xóa token cũ và sinh cặp khóa HMAC mới.');
    }, 1500);
  };

  const handleUnbind = () => {
    success('Hủy Ghép Đôi Thành Công', 'Thiết bị đã được hủy liên kết khỏi tài khoản.');
    onClose();
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Khởi Tạo & Ghép Đôi Thiết Bị (Device Pairing)">
      <div className="space-y-5 text-center">
        {/* Mock QR Code visual */}
        <div className="w-48 h-48 mx-auto p-3 rounded-2xl bg-white flex items-center justify-center shadow-2xl">
          <div className="w-full h-full border-4 border-slate-900 rounded-xl flex flex-col items-center justify-center p-2 text-slate-900">
            <QrCode className="w-24 h-24" />
            <span className="text-[9px] font-mono font-bold mt-1">LIOCHIO-PAIRING-V2</span>
          </div>
        </div>

        <div className="space-y-1 text-xs">
          <p className="font-bold text-slate-200">Mã Thiết Bị: <span className="text-emerald-400 font-mono">{deviceId}</span></p>
          <p className="text-[11px] text-slate-500 font-mono break-all">Device Token: {deviceToken}</p>
        </div>

        <div className="grid grid-cols-2 gap-3 pt-2">
          <Button variant="outline" size="sm" isLoading={isResetting} onClick={handleHardReset} leftIcon={<RefreshCw className="w-3.5 h-3.5" />}>
            Hard-Reset (15s)
          </Button>
          <Button variant="danger" size="sm" onClick={handleUnbind} leftIcon={<Unlink className="w-3.5 h-3.5" />}>
            Hủy Liên Kết (Unbind)
          </Button>
        </div>
      </div>
    </Modal>
  );
};
