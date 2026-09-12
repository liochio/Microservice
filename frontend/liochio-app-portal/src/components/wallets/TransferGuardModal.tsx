import React, { useState } from 'react';
import { Modal } from '../common/Modal';
import { Button } from '../common/Button';
import { Wallet, WalletType } from '../../types/wallet';
import { walletApi } from '../../api/walletApi';
import { useToast } from '../common/Toast';
import { ShieldAlert, AlertTriangle, ArrowRightLeft } from 'lucide-react';

interface TransferGuardModalProps {
  isOpen: boolean;
  onClose: () => void;
  wallets: Wallet[];
  onSuccess: () => void;
}

export const TransferGuardModal: React.FC<TransferGuardModalProps> = ({
  isOpen,
  onClose,
  wallets,
  onSuccess,
}) => {
  const [fromType, setFromType] = useState<WalletType>('AVAILABLE');
  const [toType, setToType] = useState<WalletType>('SAVINGS');
  const [amount, setAmount] = useState<number>(100000);
  const [targetUserId, setTargetUserId] = useState<string>('SELF');
  const [isProcessing, setIsProcessing] = useState<boolean>(false);
  const { success, error } = useToast();

  const isClosedLoopViolation = fromType === 'SAVINGS' && targetUserId !== 'SELF';

  const handleTransfer = async () => {
    setIsProcessing(true);
    try {
      const res = await walletApi.transferClosedLoop({
        fromWalletType: fromType,
        toWalletType: toType,
        toUserId: targetUserId,
        amount,
      });

      if (res.status === 'BLOCKED_BY_CLOSED_LOOP') {
        error('Vi Phạm Rào Chắn Khép Kín (Closed-Loop)', res.message);
      } else {
        success('Chuyển Tiền Thành Công!', res.message);
        onSuccess();
        onClose();
      }
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Chuyển Tiền & Rào Chắn Khép Kín (Closed-Loop Guard)">
      <div className="space-y-4">
        {/* Closed Loop Warning Banner */}
        {isClosedLoopViolation && (
          <div className="p-3.5 rounded-xl bg-rose-500/15 border border-rose-500/40 flex items-start gap-2.5 text-xs text-rose-300 animate-shake">
            <AlertTriangle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
            <div>
              <p className="font-bold">CẢNH BÁO BẢO MẬT CLOSED-LOOP:</p>
              <p className="mt-0.5">
                Ví Tiết Kiệm (SAVINGS) đại diện cho tiền mặt vật lý trong Heo Đất. Không thể chuyển trực tiếp P2P sang tài khoản người khác! Chỉ được phép rút tiền mặt hoặc chuyển nội bộ về Ví Khả Dụng.
              </p>
            </div>
          </div>
        )}

        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1.5">Từ Ví (Nguồn):</label>
          <select
            value={fromType}
            onChange={(e) => setFromType(e.target.value as WalletType)}
            className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-sm font-semibold text-slate-200 focus:outline-none focus:border-emerald-500"
          >
            <option value="AVAILABLE">Ví Khả Dụng (AVAILABLE)</option>
            <option value="SAVINGS">Ví Tiết Kiệm Heo Đất (SAVINGS - Closed-Loop)</option>
          </select>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1.5">Hình Thức Đích Đến:</label>
          <select
            value={targetUserId}
            onChange={(e) => setTargetUserId(e.target.value)}
            className="w-full px-3.5 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-sm font-semibold text-slate-200 focus:outline-none focus:border-emerald-500"
          >
            <option value="SELF">Chuyển Nội Bộ (Ví Tiết Kiệm &lt;-&gt; Ví Khả Dụng)</option>
            <option value="OTHER_USER_99">Chuyển P2P Cho Tài Khoản Khác (Cấm từ SAVINGS)</option>
          </select>
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-400 mb-1.5">Số Tiền Chuyển (VND):</label>
          <input
            type="number"
            value={amount}
            step={50000}
            onChange={(e) => setAmount(Number(e.target.value))}
            className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-lg font-bold text-emerald-400 focus:outline-none focus:border-emerald-500 font-mono"
          />
        </div>

        <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
          <Button variant="outline" onClick={onClose}>
            Hủy
          </Button>
          <Button
            variant={isClosedLoopViolation ? 'danger' : 'primary'}
            isLoading={isProcessing}
            onClick={handleTransfer}
            leftIcon={<ArrowRightLeft className="w-4 h-4" />}
          >
            {isClosedLoopViolation ? 'Thử Chuyển (Sẽ Bị Chặn)' : 'Xác Nhận Chuyển'}
          </Button>
        </div>
      </div>
    </Modal>
  );
};
