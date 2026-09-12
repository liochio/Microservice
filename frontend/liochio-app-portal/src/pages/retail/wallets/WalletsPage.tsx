import React, { useEffect, useState, useContext } from 'react';
import { AuthContext } from '../../../context/AuthContext';
import { WalletCard } from '../../../components/wallets/WalletCard';
import { TransferGuardModal } from '../../../components/wallets/TransferGuardModal';
import { Button } from '../../../components/common/Button';
import { walletApi } from '../../../api/walletApi';
import { Wallet } from '../../../types/wallet';
import { ArrowRightLeft, ShieldCheck } from 'lucide-react';

export const WalletsPage: React.FC = () => {
  const { user } = useContext(AuthContext);
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [isTransferOpen, setIsTransferOpen] = useState<boolean>(false);

  const loadWallets = () => {
    if (user) walletApi.getWallets(user.id).then(setWallets);
  };

  useEffect(() => {
    loadWallets();
  }, [user]);

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-white">Quản Lý Đa Ví & Rào Chắn Khép Kín</h2>
          <p className="text-xs text-slate-400">
            Hệ thống 3 ví chuyên biệt: AVAILABLE (Khả dụng), SAVINGS (Heo đất Closed-Loop), ESCROW (Ký quỹ 2 Pha).
          </p>
        </div>
        <Button variant="primary" size="sm" onClick={() => setIsTransferOpen(true)} leftIcon={<ArrowRightLeft className="w-4 h-4" />}>
          Chuyển Tiền
        </Button>
      </div>

      {/* Wallets Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {wallets.map((w) => (
          <WalletCard key={w.id} wallet={w} onTransferClick={() => setIsTransferOpen(true)} />
        ))}
      </div>

      {/* Closed-Loop Concept Info Card */}
      <div className="glass-panel p-6 rounded-2xl border border-slate-800 flex items-start gap-4">
        <div className="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0">
          <ShieldCheck className="w-6 h-6" />
        </div>
        <div className="space-y-1 text-xs">
          <h4 className="font-bold text-slate-200">QUY TẮC RÀO CHẮN KHÉP KÍN (CLOSED-LOOP ENFORCEMENT)</h4>
          <p className="text-slate-400">
            Tiền trong Ví Heo Đất (SAVINGS) là tiền mặt nằm trong con heo vật lý tại nhà. Do đó, hệ thống cấm tuyệt đối các giao dịch chuyển tiền trực tiếp P2P sang ví người dùng khác nhằm ngăn chặn việc rút tiền ảo khi tiền mặt vẫn còn trong heo.
          </p>
        </div>
      </div>

      {/* Transfer Modal */}
      <TransferGuardModal
        isOpen={isTransferOpen}
        onClose={() => setIsTransferOpen(false)}
        wallets={wallets}
        onSuccess={loadWallets}
      />
    </div>
  );
};
