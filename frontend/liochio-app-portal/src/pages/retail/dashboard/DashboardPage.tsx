import React, { useEffect, useState, useContext } from 'react';
import { AuthContext } from '../../../context/AuthContext';
import { WalletCard } from '../../../components/wallets/WalletCard';
import { OledDisplay } from '../../../components/piggy/OledDisplay';
import { LevelProgress } from '../../../components/piggy/LevelProgress';
import { TrialBalanceGauge } from '../../../components/ledger/TrialBalanceGauge';
import { GaussianKdeChart } from '../../../components/ai/GaussianKdeChart';
import { walletApi } from '../../../api/walletApi';
import { piggyApi } from '../../../api/piggyApi';
import { aiApi } from '../../../api/aiApi';
import { Wallet } from '../../../types/wallet';
import { PiggyState } from '../../../types/piggy';
import { GaussianKdeModel } from '../../../types/ai';
import { Sparkles, TrendingUp, ShieldCheck, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';

export const DashboardPage: React.FC = () => {
  const { user, role } = useContext(AuthContext);
  const [wallets, setWallets] = useState<Wallet[]>([]);
  const [piggyState, setPiggyState] = useState<PiggyState | null>(null);
  const [kdeModel, setKdeModel] = useState<GaussianKdeModel | null>(null);

  useEffect(() => {
    if (!user) return;
    walletApi.getWallets(user.id).then(setWallets);
    piggyApi.getPiggyState().then(setPiggyState);
    aiApi.getGaussianKdeDistribution().then(setKdeModel);
  }, [user]);

  const totalBalance = wallets.reduce((sum, w) => sum + w.balance, 0);

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Welcome Banner */}
      <div className="glass-panel p-6 rounded-3xl border border-slate-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-extrabold text-white">
              Xin chào, {user?.fullName}!
            </h2>
            <span className="text-xs px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 font-mono">
              Role: {role}
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Hệ sinh thái tài chính đóng gói thông minh kết nối phần cứng ESP32 & Lõi Sổ Kép.
          </p>
        </div>

        <div className="text-right">
          <span className="text-xs text-slate-400">Tổng Tài Sản Gia Đình:</span>
          <p className="text-2xl font-black text-emerald-400 font-mono mt-0.5">
            {totalBalance.toLocaleString('vi-VN')} đ
          </p>
        </div>
      </div>

      {/* Role specific highlights */}
      {role === 'CHILD' ? (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {piggyState && <OledDisplay state={piggyState} />}
          {piggyState && (
            <LevelProgress
              xp={piggyState.xp}
              level={piggyState.level}
              levelTitle={piggyState.levelTitle}
              xpToNextLevel={piggyState.xpToNextLevel}
            />
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          {wallets.map((w) => (
            <WalletCard key={w.id} wallet={w} />
          ))}
        </div>
      )}

      {/* Quick Analytics & Model */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {kdeModel && <GaussianKdeChart model={kdeModel} />}
        <div className="space-y-6">
          <TrialBalanceGauge />
          {piggyState && (
            <div className="glass-panel p-6 rounded-2xl border border-slate-800 flex items-center justify-between">
              <div>
                <h4 className="font-bold text-sm text-slate-200">Nuôi Heo Đất & Rút Tiền 2 Pha</h4>
                <p className="text-xs text-slate-400 mt-0.5">Khám phá khe nạp tiền và quy trình Solenoid 60s</p>
              </div>
              <Link
                to="/piggy"
                className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold flex items-center gap-1.5 shadow-lg shadow-emerald-950/30"
              >
                Vào Heo Đất <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
