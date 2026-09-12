import React, { useState } from 'react';
import { Modal } from '../common/Modal';
import { Button } from '../common/Button';
import { usePiggySaga } from '../../hooks/usePiggySaga';
import { useToast } from '../common/Toast';
import { Lock, Unlock, ShieldAlert, AlertTriangle, CheckCircle2, RotateCcw } from 'lucide-react';

interface WithdrawalModalProps {
  isOpen: boolean;
  onClose: () => void;
  maxSavingsAmount: number;
  onWithdrawalComplete: () => void;
}

export const WithdrawalModal: React.FC<WithdrawalModalProps> = ({
  isOpen,
  onClose,
  maxSavingsAmount,
  onWithdrawalComplete,
}) => {
  const [withdrawAmount, setWithdrawAmount] = useState<number>(100000);
  const [parentPin, setParentPin] = useState<string>('8888');
  const [pinError, setPinError] = useState<string>('');
  const { session, isProcessing, startWithdrawal, confirmSettle, handleTimeout, simulateJam, resetSaga } = usePiggySaga();
  const { success, error } = useToast();

  const handleStart = async () => {
    if (!parentPin || parentPin.trim().length < 4) {
      setPinError('Vui lòng nhập đầy đủ mã PIN Phụ Huynh (tối thiểu 4 chữ số)!');
      return;
    }
    setPinError('');
    try {
      await startWithdrawal(withdrawAmount, parentPin);
    } catch (err: any) {
      setPinError(err?.message || 'Không thể khởi tạo phiên rút tiền.');
    }
  };

  const handleSettle = async () => {
    await confirmSettle();
    success('Rút Tiền Thành Công!', `Đã lấy ${withdrawAmount.toLocaleString('vi-VN')} đ từ Heo Đất.`);
    onWithdrawalComplete();
  };

  const handleManualTimeout = async () => {
    await handleTimeout();
    error('Hết Thời Gian 60s', 'Tiền đã được Rollback về lại Ví Heo Đất an toàn.');
  };

  const handleClose = () => {
    resetSaga();
    onClose();
  };

  const progressPct = session ? Math.max(0, (session.remainingSeconds / 60) * 100) : 100;

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Quy Trình Rút Tiền Mặt 2 Pha (2-Phase Saga)">
      {!session || session.status === 'IDLE' ? (
        <div className="space-y-5">
          <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/30 flex items-start gap-3 text-xs text-amber-200">
            <ShieldAlert className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
            <div>
              <p className="font-bold">BẢO VỆ ĐỘC QUYỀN 2-PHASE SAGA</p>
              <p className="mt-1 text-slate-300">
                Khi bấm bắt đầu, tiền sẽ chuyển sang trạng thái <strong>HOLDING (Ký quỹ)</strong> và chốt khóa Solenoid 5V NC mở trong 60 giây. Cần có <strong>Mã PIN Phụ Huynh</strong> để kích hoạt.
              </p>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-2">Số tiền muốn rút (VND):</label>
            <input
              type="number"
              value={withdrawAmount}
              max={maxSavingsAmount}
              step={50000}
              onChange={(e) => setWithdrawAmount(Number(e.target.value))}
              className="w-full px-4 py-3 rounded-xl bg-slate-900 border border-slate-700 text-lg font-bold text-emerald-400 focus:outline-none focus:border-emerald-500 font-mono"
            />
            <span className="text-[11px] text-slate-500 mt-1 block">
              Tối đa có thể rút: {maxSavingsAmount.toLocaleString('vi-VN')} đ
            </span>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-2">Mã PIN Phụ Huynh (Bảo vệ con trẻ):</label>
            <input
              type="password"
              maxLength={6}
              value={parentPin}
              onChange={(e) => setParentPin(e.target.value)}
              placeholder="Nhập PIN cha mẹ (demo: 8888)"
              className="w-full px-4 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-center text-xl font-bold tracking-widest text-slate-200 focus:outline-none focus:border-emerald-500"
            />
            {pinError && <p className="text-xs text-rose-400 mt-1">{pinError}</p>}
          </div>

          <div className="flex justify-end gap-3 pt-3">
            <Button variant="outline" onClick={handleClose}>
              Hủy Bỏ
            </Button>
            <Button variant="primary" isLoading={isProcessing} onClick={handleStart} leftIcon={<Unlock className="w-4 h-4" />}>
              Mở Khóa Solenoid (60s)
            </Button>
          </div>
        </div>
      ) : session.status === 'HOLDING' ? (
        <div className="space-y-6 text-center">
          {/* Circular SVG Countdown Ring */}
          <div className="relative w-40 h-40 mx-auto flex items-center justify-center">
            <svg className="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
              <circle cx="50" cy="50" r="42" className="text-slate-800" strokeWidth="8" stroke="currentColor" fill="transparent" />
              <circle
                cx="50"
                cy="50"
                r="42"
                className="text-amber-500 transition-all duration-1000 ease-linear"
                strokeWidth="8"
                strokeDasharray={264}
                strokeDashoffset={264 - (264 * progressPct) / 100}
                strokeLinecap="round"
                stroke="currentColor"
                fill="transparent"
              />
            </svg>
            <div className="absolute inset-0 flex flex-col items-center justify-center">
              <span className="text-3xl font-extrabold font-mono text-amber-400">
                {session.remainingSeconds}s
              </span>
              <span className="text-[10px] text-slate-400 uppercase tracking-widest">ĐẾM NGƯỢC</span>
            </div>
          </div>

          <div className="space-y-1">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-500/20 text-amber-400 text-xs font-bold border border-amber-500/40 animate-pulse">
              <Unlock className="w-4 h-4" /> SOLENOID 5V NC: ĐANG MỞ
            </div>
            <p className="text-xs text-slate-300">
              Cửa heo đất đã mở. Hãy rút tiền mặt và bấm <strong>"Xác Nhận Đã Lấy Tiền"</strong> để chốt sổ cái!
            </p>
          </div>

          {/* Action buttons */}
          <div className="grid grid-cols-2 gap-3 pt-2">
            <Button
              variant="primary"
              size="lg"
              className="w-full"
              isLoading={isProcessing}
              onClick={handleSettle}
              leftIcon={<CheckCircle2 className="w-5 h-5" />}
            >
              Nút Bấm Settle Vật Lý
            </Button>
            <Button
              variant="danger"
              size="lg"
              className="w-full"
              onClick={simulateJam}
              leftIcon={<AlertTriangle className="w-5 h-5" />}
            >
              Báo Kẹt Cơ Khí (Jam)
            </Button>
          </div>

          <button
            onClick={handleManualTimeout}
            className="text-xs text-slate-500 hover:text-slate-300 underline block mx-auto"
          >
            Mô phỏng Hết Giờ (Timeout Rollback)
          </button>
        </div>
      ) : session.status === 'SETTLED' ? (
        <div className="text-center py-6 space-y-4">
          <div className="w-16 h-16 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/40 flex items-center justify-center mx-auto shadow-lg shadow-emerald-950/40">
            <CheckCircle2 className="w-10 h-10" />
          </div>
          <h4 className="text-lg font-bold text-emerald-400">RÚT TIỀN THÀNH CÔNG (PHASE 2 SETTLED)</h4>
          <p className="text-xs text-slate-300 max-w-sm mx-auto">
            Khóa Solenoid đã tự đóng lại an toàn. Sổ cái kép đã đối soát và trừ tiền chính xác khỏi ví SAVINGS.
          </p>
          <Button variant="primary" onClick={handleClose} className="w-full">
            Hoàn Tất & Đóng
          </Button>
        </div>
      ) : session.status === 'TIMED_OUT' ? (
        <div className="text-center py-6 space-y-4">
          <div className="w-16 h-16 rounded-full bg-rose-500/20 text-rose-400 border border-rose-500/40 flex items-center justify-center mx-auto">
            <RotateCcw className="w-10 h-10" />
          </div>
          <h4 className="text-lg font-bold text-rose-400">HẾT GIỜ 60S (TIMEOUT ROLLBACK)</h4>
          <p className="text-xs text-slate-300 max-w-sm mx-auto">
            Không nhận được tín hiệu xác nhận từ nút bấm vật lý. Toàn bộ tiền đã được hoàn trả về Ví Heo Đất.
          </p>
          <Button variant="secondary" onClick={handleClose} className="w-full">
            Đóng Cửa Sổ
          </Button>
        </div>
      ) : (
        <div className="text-center py-6 space-y-4">
          <div className="w-16 h-16 rounded-full bg-rose-600/30 text-rose-400 border border-rose-500 flex items-center justify-center mx-auto animate-pulse">
            <AlertTriangle className="w-10 h-10" />
          </div>
          <h4 className="text-lg font-bold text-rose-400">CẢNH BÁO KẸT CƠ KHÍ (MECHANICAL JAM)</h4>
          <p className="text-xs text-rose-200/90 max-w-sm mx-auto">
            {session.failureReason || 'Cảm biến hồng ngoại phát hiện kẹt tiền. Hệ thống đã khóa Solenoid và gửi thông báo khẩn tới phụ huynh.'}
          </p>
          <Button variant="danger" onClick={handleClose} className="w-full">
            Đã Hiểu & Xử Lý Phần Cứng
          </Button>
        </div>
      )}
    </Modal>
  );
};
