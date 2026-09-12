import React from 'react';
import { Wallet } from '../../types/wallet';
import { WalletCards, ShieldCheck, Lock, ArrowUpRight } from 'lucide-react';
import { Badge } from '../common/Badge';

interface WalletCardProps {
  wallet: Wallet;
  onTransferClick?: () => void;
}

export const WalletCard: React.FC<WalletCardProps> = ({ wallet, onTransferClick }) => {
  const isSavings = wallet.type === 'SAVINGS';
  const isEscrow = wallet.type === 'ESCROW';

  return (
    <div
      className={`glass-panel p-6 rounded-2xl border transition-all duration-300 relative overflow-hidden ${
        isSavings
          ? 'border-emerald-500/40 bg-gradient-to-b from-emerald-950/20 to-slate-900/80 shadow-lg shadow-emerald-950/20'
          : isEscrow
          ? 'border-amber-500/30 bg-gradient-to-b from-amber-950/20 to-slate-900/80'
          : 'border-slate-800'
      }`}
    >
      <div className="flex items-start justify-between">
        <div>
          <span className="text-[11px] font-bold font-mono tracking-wider text-slate-400 uppercase">
            {wallet.accountNumber}
          </span>
          <h4 className="text-base font-bold text-slate-100 mt-0.5">{wallet.name}</h4>
        </div>
        <Badge variant={wallet.status === 'ACTIVE' ? 'emerald' : 'rose'}>
          {wallet.status}
        </Badge>
      </div>

      <div className="my-4">
        <span className="text-[11px] text-slate-400">Số Dư Khả Dụng:</span>
        <div className="text-2xl font-extrabold text-slate-100 font-mono tracking-tight">
          {wallet.balance.toLocaleString('vi-VN')} <span className="text-sm font-normal text-emerald-400">đ</span>
        </div>
      </div>

      <p className="text-xs text-slate-400 mb-4">{wallet.description}</p>

      <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs">
        {isSavings ? (
          <span className="flex items-center gap-1 text-emerald-400 text-[11px] font-semibold">
            <ShieldCheck className="w-3.5 h-3.5" /> Closed-Loop Protected
          </span>
        ) : isEscrow ? (
          <span className="flex items-center gap-1 text-amber-400 text-[11px] font-semibold">
            <Lock className="w-3.5 h-3.5" /> Saga Escrow Locked
          </span>
        ) : (
          <span className="text-[11px] text-slate-500">Thanh toán tự do</span>
        )}

        {onTransferClick && (
          <button
            onClick={onTransferClick}
            className="flex items-center gap-1 font-semibold text-emerald-400 hover:text-emerald-300 transition-colors"
          >
            Chuyển tiền <ArrowUpRight className="w-3.5 h-3.5" />
          </button>
        )}
      </div>
    </div>
  );
};
