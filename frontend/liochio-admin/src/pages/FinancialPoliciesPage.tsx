import React, { useState } from 'react';
import { 
  BadgePercent, 
  SlidersHorizontal, 
  Receipt, 
  TrendingUp, 
  CalendarDays, 
  Save, 
  CheckCircle2, 
  DollarSign, 
  Clock,
  HelpCircle
} from 'lucide-react';
import { Card, Badge } from '../components/common/CardAndBadge';
import { Button } from '../components/common/Button';

export const FinancialPoliciesPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'limits' | 'fees' | 'interest' | 'calendar'>('limits');
  const [savedSuccess, setSavedSuccess] = useState(false);

  const [limits, setLimits] = useState({
    minPerTx: 10000,
    maxPerTx: 50000000,
    maxDailyTotal: 200000000,
    biometricThreshold: 10000000,
    maxSagaHoldingMinutes: 15,
  });

  const [fees, setFees] = useState([
    { type: 'PIGGY_WITHDRAWAL_CASH', name: 'Phí Rút Tiền Mặt Vật Lý (Solenoid Khóa)', rate: '1.2% / Giao dịch (Tối thiểu 5.000 đ)', tier: 'Standard B2C' },
    { type: 'CLOSED_LOOP_TRANSFER', name: 'Chuyển Tiền Ví Kín Nội Bộ Hệ Sinh Thái', rate: '0.0% (Miễn Phí Toàn Sàn)', tier: 'Zero Fee Policy' },
    { type: 'TENANT_API_INTEREST', name: 'Phí Dịch Vụ API Cố Vấn AI & Đối Soát', rate: '50 đ / Request gọi', tier: 'B2B Enterprise' },
    { type: 'PARENTAL_MATCH_POOL', name: 'Khoản Thưởng Khuyến Khích Nạp Tiết Kiệm', rate: '+50% (Phụ Huynh Ký Quỹ)', tier: 'Gamification Pool' },
  ]);

  const [interestEngine, setInterestEngine] = useState({
    baseAnnualRate: 6.8,
    dailyCompounding: true,
    tier1Threshold: 10000000,
    tier1BonusRate: 0.5,
    tier2Threshold: 50000000,
    tier2BonusRate: 1.2,
  });

  const [cutoff, setCutoff] = useState({
    dailyCutoffTime: '23:30:00',
    reconciliationWindowMinutes: 60,
    allowWeekendSettlement: true,
  });

  const handleSave = () => {
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 3000);
  };

  return (
    <div className="space-y-6 animate-fade-in max-w-7xl">
      {/* Header */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-slate-900/80 border border-slate-800 p-6 rounded-3xl backdrop-blur-xl">
        <div>
          <div className="flex items-center gap-2 font-mono text-xs text-amber-400 font-bold mb-1">
            <BadgePercent className="w-4 h-4" />
            <span>SA_FIN_POLICIES &bull; GLOBAL FINANCIAL POLICIES & LEDGER CONTROLS</span>
          </div>
          <h1 className="text-2xl font-black text-white tracking-tight">
            Cấu Hình Chính Sách Tài Chính Sàn
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Thiết lập trần hạn mức rủi ro luân chuyển tiền, biểu phí giao dịch cơ sở, công thức sinh lời lãi suất và lịch Cut-off chốt sổ.
          </p>
        </div>

        <Button onClick={handleSave} className="flex items-center gap-2">
          <Save className="w-4 h-4" />
          <span>Lưu Cấu Hình Tài Chính</span>
        </Button>
      </div>

      {savedSuccess && (
        <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs flex items-center gap-2 animate-fade-in font-mono">
          <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          <span>Chính sách tài chính đã được cập nhật thành công xuống Redis Cluster và Ledger Core Engine.</span>
        </div>
      )}

      {/* Tabs */}
      <div className="flex border-b border-slate-800/80 gap-2 pb-2 text-xs font-bold">
        <button
          onClick={() => setActiveTab('limits')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'limits'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <SlidersHorizontal className="w-4 h-4" />
          <span>SA_FIN_LIMITS (Hạn Mức Sàn / Trần)</span>
        </button>

        <button
          onClick={() => setActiveTab('fees')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'fees'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <Receipt className="w-4 h-4" />
          <span>SA_FIN_FEE_RATES (Biểu Phí Cơ Sở)</span>
        </button>

        <button
          onClick={() => setActiveTab('interest')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'interest'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <TrendingUp className="w-4 h-4" />
          <span>SA_FIN_INTEREST_ENGINE (Động Cơ Tính Lãi)</span>
        </button>

        <button
          onClick={() => setActiveTab('calendar')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'calendar'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <CalendarDays className="w-4 h-4" />
          <span>SA_FIN_CALENDAR_CUTOFF (Lịch Làm Việc & Cut-off)</span>
        </button>
      </div>

      {/* TAB 1: LIMITS */}
      {activeTab === 'limits' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Card className="space-y-4">
            <h2 className="text-sm font-bold text-white flex items-center gap-2 pb-3 border-b border-slate-800">
              <DollarSign className="w-4 h-4 text-emerald-400" />
              <span>Hạn Mức Trần Giao Dịch Toàn Hệ Thống (VND)</span>
            </h2>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-300 font-bold mb-1">Giao Dịch Tối Thiểu (Min Per-Txn)</label>
                <input
                  type="number"
                  value={limits.minPerTx}
                  onChange={e => setLimits({ ...limits, minPerTx: Number(e.target.value) })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-white font-mono"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-bold mb-1">Trần Giao Dịch Đơn Lẻ (Max Per-Txn Ceiling)</label>
                <input
                  type="number"
                  value={limits.maxPerTx}
                  onChange={e => setLimits({ ...limits, maxPerTx: Number(e.target.value) })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-white font-mono"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-bold mb-1">Trần Tổng Hạn Mức Luân Chuyển Ngày (Daily Ceiling)</label>
                <input
                  type="number"
                  value={limits.maxDailyTotal}
                  onChange={e => setLimits({ ...limits, maxDailyTotal: Number(e.target.value) })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-white font-mono"
                />
              </div>
            </div>
          </Card>

          <Card className="space-y-4">
            <h2 className="text-sm font-bold text-white flex items-center gap-2 pb-3 border-b border-slate-800">
              <Clock className="w-4 h-4 text-amber-400" />
              <span>Ngưỡng Xác Thực Sinh Trắc Học & Saga Timeout</span>
            </h2>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-300 font-bold mb-1">Ngưỡng Bắt Buộc Sinh Trắc Học (FaceID / Biometric)</label>
                <input
                  type="number"
                  value={limits.biometricThreshold}
                  onChange={e => setLimits({ ...limits, biometricThreshold: Number(e.target.value) })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-white font-mono"
                />
              </div>

              <div>
                <label className="block text-slate-300 font-bold mb-1">Thời Gian Khóa Ví Ký Quỹ Saga (Tối Đa Phút)</label>
                <input
                  type="number"
                  value={limits.maxSagaHoldingMinutes}
                  onChange={e => setLimits({ ...limits, maxSagaHoldingMinutes: Number(e.target.value) })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-white font-mono"
                />
              </div>
            </div>
          </Card>
        </div>
      )}

      {/* TAB 2: FEES */}
      {activeTab === 'fees' && (
        <Card className="p-0 overflow-hidden">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="bg-slate-950/80 text-slate-400 uppercase tracking-wider text-[10px] font-bold border-b border-slate-800 font-mono">
              <tr>
                <th className="py-3.5 px-4">Mã Biểu Phí</th>
                <th className="py-3.5 px-4">Tên Nghiệp Vụ</th>
                <th className="py-3.5 px-4">Mức Phí Cơ Sở</th>
                <th className="py-3.5 px-4">Phân Hạng Áp Dụng</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono">
              {fees.map(f => (
                <tr key={f.type} className="hover:bg-slate-800/30 transition">
                  <td className="py-3.5 px-4 font-bold text-amber-400">{f.type}</td>
                  <td className="py-3.5 px-4 font-sans font-bold text-white">{f.name}</td>
                  <td className="py-3.5 px-4 text-emerald-400 font-bold">{f.rate}</td>
                  <td className="py-3.5 px-4 font-sans">
                    <Badge variant="primary">{f.tier}</Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}

      {/* TAB 3: INTEREST ENGINE */}
      {activeTab === 'interest' && (
        <Card className="space-y-4">
          <h2 className="text-sm font-bold text-white flex items-center gap-2 pb-3 border-b border-slate-800">
            <TrendingUp className="w-4 h-4 text-emerald-400" />
            <span>Động Cơ Tính Lãi Suất Bậc Thang Heo Đất (Savings Interest Engine)</span>
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
            <div className="p-4 bg-slate-950/60 rounded-2xl border border-slate-800 space-y-2">
              <span className="text-slate-400 block text-[10px]">Lãi Suất Cơ Sở / Năm</span>
              <div className="text-2xl font-black text-emerald-400 font-mono">{interestEngine.baseAnnualRate}% / năm</div>
              <p className="text-[11px] text-slate-400">Áp dụng cho mọi số dư trong Ví Tiết Kiệm (Saving Wallet).</p>
            </div>

            <div className="p-4 bg-slate-950/60 rounded-2xl border border-slate-800 space-y-2">
              <span className="text-slate-400 block text-[10px]">Bậc 1: &gt; 10 Triệu</span>
              <div className="text-2xl font-black text-amber-400 font-mono">+{interestEngine.tier1BonusRate}% Thưởng</div>
              <p className="text-[11px] text-slate-400">Cộng thêm vào lãi suất cơ sở khi số dư vượt mốc 10.000.000 đ.</p>
            </div>

            <div className="p-4 bg-slate-950/60 rounded-2xl border border-slate-800 space-y-2">
              <span className="text-slate-400 block text-[10px]">Bậc 2: &gt; 50 Triệu</span>
              <div className="text-2xl font-black text-indigo-400 font-mono">+{interestEngine.tier2BonusRate}% Thưởng</div>
              <p className="text-[11px] text-slate-400">Cộng thêm tối đa cho khách hàng tích lũy tài sản lớn.</p>
            </div>
          </div>
        </Card>
      )}

      {/* TAB 4: CALENDAR & CUTOFF */}
      {activeTab === 'calendar' && (
        <Card className="space-y-4">
          <h2 className="text-sm font-bold text-white flex items-center gap-2 pb-3 border-b border-slate-800">
            <CalendarDays className="w-4 h-4 text-indigo-400" />
            <span>Lịch Làm Việc Hệ Thống & Giờ Chốt Sổ (Cut-Off Times)</span>
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
            <div className="p-4 bg-slate-950 rounded-2xl border border-slate-800 space-y-3">
              <div className="font-bold text-white">Giờ Cut-Off Chốt Sổ Hàng Ngày (Hạch Toán Kép)</div>
              <input
                type="text"
                value={cutoff.dailyCutoffTime}
                onChange={e => setCutoff({ ...cutoff, dailyCutoffTime: e.target.value })}
                className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-amber-400 font-mono font-bold"
              />
              <p className="text-[11px] text-slate-400">Mọi giao dịch sau giờ Cut-off sẽ được tính vào giá trị sổ cái ngày làm việc tiếp theo.</p>
            </div>

            <div className="p-4 bg-slate-950 rounded-2xl border border-slate-800 space-y-3">
              <div className="font-bold text-white">Khung Cửa Sổ Đối Soát (Phút)</div>
              <input
                type="number"
                value={cutoff.reconciliationWindowMinutes}
                onChange={e => setCutoff({ ...cutoff, reconciliationWindowMinutes: Number(e.target.value) })}
                className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-emerald-400 font-mono font-bold"
              />
              <p className="text-[11px] text-slate-400">Khoảng thời gian Worker CDC khóa sổ tạm thời để tổng hợp Trial Balance.</p>
            </div>
          </div>
        </Card>
      )}
    </div>
  );
};
