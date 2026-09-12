import React, { useState } from 'react';
import { 
  UserCheck, 
  ScanFace, 
  Award, 
  Wallet, 
  RadioReceiver, 
  CheckCircle, 
  Clock, 
  AlertCircle,
  Search,
  ArrowRight,
  ShieldCheck,
  ChevronRight
} from 'lucide-react';

interface CustomerOnboardingItem {
  id: number;
  userId: number;
  username: string;
  fullName: string;
  phone: string;
  email: string;
  idCardNumber: string;
  idCardType: string;
  // Gate 1
  gate1Status: 'NOT_STARTED' | 'PENDING' | 'APPROVED' | 'REJECTED';
  gate1Notes?: string;
  // Gate 2
  gate2Status: 'NOT_STARTED' | 'PENDING' | 'APPROVED' | 'REJECTED';
  assignedTier: string;
  dailyLimit: number;
  // Gate 3
  gate3Status: 'NOT_STARTED' | 'PENDING' | 'PROVISIONED' | 'FAILED';
  availableAccountNo?: string;
  savingsAccountNo?: string;
  // Gate 4
  gate4Status: 'NOT_STARTED' | 'PENDING' | 'BOUND' | 'REJECTED';
  boundDeviceSerial?: string;
  boundDeviceModel?: string;
  // Overall
  overallStatus: 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED';
  createdAt: string;
}

export const CustomerOnboardingPage: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCust, setSelectedCust] = useState<CustomerOnboardingItem | null>(null);

  const [customers, setCustomers] = useState<CustomerOnboardingItem[]>([
    {
      id: 1,
      userId: 5,
      username: 'fintech_user01',
      fullName: 'Nguyễn Văn An',
      phone: '0901234567',
      email: 'user01@fintech.dev',
      idCardNumber: '079200001234',
      idCardType: 'CCCD',
      gate1Status: 'APPROVED',
      gate1Notes: 'Khuôn mặt so khớp CCCD 99.4% qua AI Liveness',
      gate2Status: 'APPROVED',
      assignedTier: 'TIER_2',
      dailyLimit: 500000000.0,
      gate3Status: 'PROVISIONED',
      availableAccountNo: 'ACC_USR_5_AVAIL',
      savingsAccountNo: 'ACC_USR_5_ESCROW',
      gate4Status: 'BOUND',
      boundDeviceSerial: 'PIGGY-VN-8801',
      boundDeviceModel: 'LIOCHIO-PIGGY-V1',
      overallStatus: 'COMPLETED',
      createdAt: '2026-09-08 09:00:00'
    },
    {
      id: 2,
      userId: 8,
      username: 'tran_minh_tam',
      fullName: 'Trần Minh Tâm',
      phone: '0988776655',
      email: 'tam.tran@gmail.com',
      idCardNumber: '079099881122',
      idCardType: 'CCCD',
      gate1Status: 'PENDING',
      gate2Status: 'NOT_STARTED',
      assignedTier: 'TIER_1',
      dailyLimit: 5000000.0,
      gate3Status: 'NOT_STARTED',
      gate4Status: 'NOT_STARTED',
      overallStatus: 'IN_PROGRESS',
      createdAt: '2026-09-10 11:20:00'
    },
    {
      id: 3,
      userId: 9,
      username: 'le_thi_hoa',
      fullName: 'Lê Thị Hoa',
      phone: '0912334455',
      email: 'hoa.le@outlook.com',
      idCardNumber: '079088776655',
      idCardType: 'CCCD',
      gate1Status: 'APPROVED',
      gate2Status: 'PENDING',
      assignedTier: 'TIER_1',
      dailyLimit: 5000000.0,
      gate3Status: 'NOT_STARTED',
      gate4Status: 'NOT_STARTED',
      overallStatus: 'IN_PROGRESS',
      createdAt: '2026-09-10 13:45:00'
    }
  ]);

  const handleApproveGate1 = (cust: CustomerOnboardingItem) => {
    setCustomers(prev => prev.map(c => c.id === cust.id ? {
      ...c,
      gate1Status: 'APPROVED',
      gate2Status: 'PENDING',
      gate1Notes: 'Thẩm định viên phê chuẩn hồ sơ CCCD hợp lệ.'
    } : c));
    alert(`✅ Đã phê chuẩn Gate 1 (eKYC) cho khách hàng ${cust.fullName}!`);
    if (selectedCust?.id === cust.id) {
      setSelectedCust(prev => prev ? { ...prev, gate1Status: 'APPROVED', gate2Status: 'PENDING' } : null);
    }
  };

  const handleApproveGate2 = (cust: CustomerOnboardingItem, tier: string, limit: number) => {
    setCustomers(prev => prev.map(c => c.id === cust.id ? {
      ...c,
      gate2Status: 'APPROVED',
      assignedTier: tier,
      dailyLimit: limit,
      gate3Status: 'PENDING'
    } : c));
    alert(`✅ Đã cấp hạng ${tier} & hạn mức ${(limit).toLocaleString()} VND cho ${cust.fullName}!`);
    if (selectedCust?.id === cust.id) {
      setSelectedCust(prev => prev ? { ...prev, gate2Status: 'APPROVED', assignedTier: tier, dailyLimit: limit, gate3Status: 'PENDING' } : null);
    }
  };

  const handleApproveGate3 = (cust: CustomerOnboardingItem) => {
    const availAcc = `ACC_USR_${cust.userId}_AVAIL`;
    const savingsAcc = `ACC_USR_${cust.userId}_SAVINGS`;
    setCustomers(prev => prev.map(c => c.id === cust.id ? {
      ...c,
      gate3Status: 'PROVISIONED',
      availableAccountNo: availAcc,
      savingsAccountNo: savingsAcc,
      gate4Status: 'PENDING'
    } : c));
    alert(`🎉 Đã mở cặp tài khoản Sổ cái kép (${availAcc} & ${savingsAcc}) cho ${cust.fullName}!`);
    if (selectedCust?.id === cust.id) {
      setSelectedCust(prev => prev ? { ...prev, gate3Status: 'PROVISIONED', availableAccountNo: availAcc, savingsAccountNo: savingsAcc, gate4Status: 'PENDING' } : null);
    }
  };

  const handleApproveGate4 = (cust: CustomerOnboardingItem, serial: string) => {
    setCustomers(prev => prev.map(c => c.id === cust.id ? {
      ...c,
      gate4Status: 'BOUND',
      boundDeviceSerial: serial,
      boundDeviceModel: 'LIOCHIO-PIGGY-V1',
      overallStatus: 'COMPLETED'
    } : c));
    alert(`🚀 Đã hoàn tất Gate 4 & ghép đôi Heo Đất IoT (#${serial}) thành công! Toàn bộ 4 Cửa ải đã hoàn tất.`);
    if (selectedCust?.id === cust.id) {
      setSelectedCust(prev => prev ? { ...prev, gate4Status: 'BOUND', boundDeviceSerial: serial, overallStatus: 'COMPLETED' } : null);
    }
  };

  const filteredList = customers.filter(c => 
    c.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    c.phone.includes(searchTerm) ||
    c.idCardNumber.includes(searchTerm)
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-black tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-400">
              QUY TRÌNH 4 CỬA ẢI THẨM ĐỊNH KHÁCH HÀNG
            </h1>
            <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-cyan-500/10 text-cyan-300 border border-cyan-500/30 font-mono">
              MENU: CORP_CUSTOMER_MGMT
            </span>
          </div>
          <p className="text-slate-400 text-xs mt-1">
            Gate 1: Duyệt eKYC $\rightarrow$ Gate 2: Cấp Tier & Hạn mức $\rightarrow$ Gate 3: Mở Ví Sổ cái kép $\rightarrow$ Gate 4: Ghép đôi Heo Đất IoT
          </p>
        </div>

        <div className="relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Tìm tên KH, SĐT, Số CCCD..."
            value={searchTerm}
            onChange={e => setSearchTerm(e.target.value)}
            className="bg-slate-900 border border-slate-800 rounded-xl pl-10 pr-4 py-2 text-xs text-slate-200 outline-none focus:border-cyan-500 w-64 shadow"
          />
        </div>
      </div>

      {/* 4 Gates Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 flex items-center gap-3 shadow-lg">
          <div className="w-10 h-10 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center shrink-0">
            <ScanFace className="w-5 h-5 text-blue-400" />
          </div>
          <div>
            <div className="text-[10px] font-mono text-slate-400 uppercase font-bold">CỬA ẢI 1</div>
            <div className="text-xs font-bold text-slate-100">Duyệt eKYC CCCD</div>
            <div className="text-[11px] text-blue-400 font-semibold mt-0.5">AI OCR & Liveness</div>
          </div>
        </div>

        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 flex items-center gap-3 shadow-lg">
          <div className="w-10 h-10 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center shrink-0">
            <Award className="w-5 h-5 text-purple-400" />
          </div>
          <div>
            <div className="text-[10px] font-mono text-slate-400 uppercase font-bold">CỬA ẢI 2</div>
            <div className="text-xs font-bold text-slate-100">Cấp Tier & Hạn Mức</div>
            <div className="text-[11px] text-purple-400 font-semibold mt-0.5">Tier 1/2/3 Limits</div>
          </div>
        </div>

        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 flex items-center gap-3 shadow-lg">
          <div className="w-10 h-10 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center shrink-0">
            <Wallet className="w-5 h-5 text-amber-400" />
          </div>
          <div>
            <div className="text-[10px] font-mono text-slate-400 uppercase font-bold">CỬA ẢI 3</div>
            <div className="text-xs font-bold text-slate-100">Mở Ví Sổ Cái Kép</div>
            <div className="text-[11px] text-amber-400 font-semibold mt-0.5">Dual-Ledger Accounts</div>
          </div>
        </div>

        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 flex items-center gap-3 shadow-lg">
          <div className="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center shrink-0">
            <RadioReceiver className="w-5 h-5 text-emerald-400" />
          </div>
          <div>
            <div className="text-[10px] font-mono text-slate-400 uppercase font-bold">CỬA ẢI 4</div>
            <div className="text-xs font-bold text-slate-100">Ghép Đôi Heo Đất IoT</div>
            <div className="text-[11px] text-emerald-400 font-semibold mt-0.5">Serial HMAC Pairing</div>
          </div>
        </div>
      </div>

      {/* Customer List & Detailed 4-Gate Steps */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Customer List */}
        <div className="lg:col-span-1 bg-slate-900/90 border border-slate-800 rounded-2xl p-4 space-y-3 shadow-xl">
          <h2 className="text-xs font-mono uppercase text-slate-400 font-bold px-2">Danh Sách Khách Hàng ({filteredList.length})</h2>
          <div className="space-y-2">
            {filteredList.map(cust => (
              <div
                key={cust.id}
                onClick={() => setSelectedCust(cust)}
                className={`p-4 rounded-xl border transition cursor-pointer flex flex-col justify-between ${
                  selectedCust?.id === cust.id
                    ? 'bg-gradient-to-r from-cyan-500/10 to-blue-600/10 border-cyan-500/40 text-cyan-300 shadow'
                    : 'bg-slate-950/60 border-slate-800/80 hover:border-slate-700 text-slate-300'
                }`}
              >
                <div className="flex items-center justify-between mb-2">
                  <div className="font-bold text-sm text-slate-100">{cust.fullName}</div>
                  <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                    cust.overallStatus === 'COMPLETED'
                      ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                      : 'bg-amber-500/10 text-amber-300 border border-amber-500/30'
                  }`}>
                    {cust.overallStatus}
                  </span>
                </div>
                <div className="text-[11px] text-slate-400 font-mono space-y-0.5">
                  <div>CCCD: {cust.idCardNumber}</div>
                  <div>SĐT: {cust.phone}</div>
                </div>

                {/* Mini Gate Progress Dots */}
                <div className="flex items-center gap-1.5 mt-3 pt-2 border-t border-slate-800/60">
                  <span className={`w-2.5 h-2.5 rounded-full ${cust.gate1Status === 'APPROVED' ? 'bg-blue-400' : 'bg-slate-700'}`} title="Gate 1" />
                  <span className={`w-2.5 h-2.5 rounded-full ${cust.gate2Status === 'APPROVED' ? 'bg-purple-400' : 'bg-slate-700'}`} title="Gate 2" />
                  <span className={`w-2.5 h-2.5 rounded-full ${cust.gate3Status === 'PROVISIONED' ? 'bg-amber-400' : 'bg-slate-700'}`} title="Gate 3" />
                  <span className={`w-2.5 h-2.5 rounded-full ${cust.gate4Status === 'BOUND' ? 'bg-emerald-400' : 'bg-slate-700'}`} title="Gate 4" />
                  <span className="text-[10px] text-slate-500 ml-auto font-mono">4-Gate Progress</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Right Column: Active Gate Workflow Panel */}
        <div className="lg:col-span-2 space-y-4">
          {selectedCust ? (
            <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-2xl space-y-6">
              {/* Customer Profile Header */}
              <div className="flex items-center justify-between pb-4 border-b border-slate-800">
                <div>
                  <h2 className="text-lg font-bold text-slate-100 flex items-center gap-2">
                    <UserCheck className="w-5 h-5 text-cyan-400" />
                    {selectedCust.fullName} ({selectedCust.username})
                  </h2>
                  <div className="text-xs text-slate-400 mt-0.5">
                    CCCD: <span className="text-slate-200 font-mono font-semibold">{selectedCust.idCardNumber}</span> &bull; SĐT: <span className="text-slate-200 font-mono">{selectedCust.phone}</span> &bull; Email: {selectedCust.email}
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-[10px] font-mono text-slate-400 uppercase block">Cấp Độ Hiện Tại</span>
                  <span className="text-xs font-bold text-purple-400 bg-purple-500/10 px-2.5 py-1 rounded-lg border border-purple-500/20">
                    {selectedCust.assignedTier} &bull; {(selectedCust.dailyLimit / 1000000).toFixed(0)}Tr/ngày
                  </span>
                </div>
              </div>

              {/* Step 1: Gate 1 eKYC */}
              <div className={`p-4 rounded-xl border transition ${
                selectedCust.gate1Status === 'APPROVED' ? 'bg-slate-950/40 border-blue-500/30' : 'bg-slate-950/80 border-slate-800'
              }`}>
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2 font-bold text-xs text-slate-200">
                    <ScanFace className="w-4 h-4 text-blue-400" />
                    CỬA ẢI 1: THẨM ĐỊNH HỒ SƠ eKYC (OCR & LIVENESS)
                  </div>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-mono font-bold ${
                    selectedCust.gate1Status === 'APPROVED' ? 'bg-blue-500/10 text-blue-400 border border-blue-500/30' : 'bg-amber-500/10 text-amber-300'
                  }`}>
                    {selectedCust.gate1Status}
                  </span>
                </div>
                <p className="text-xs text-slate-400 mb-3">
                  {selectedCust.gate1Notes || 'Hồ sơ hình ảnh CCCD 2 mặt và video sinh trắc học đang chờ kiểm tra tính hợp lệ.'}
                </p>
                {selectedCust.gate1Status === 'PENDING' && (
                  <button
                    onClick={() => handleApproveGate1(selectedCust)}
                    className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs shadow transition cursor-pointer"
                  >
                    Phê Chuẩn Gate 1 (Duyệt eKYC)
                  </button>
                )}
              </div>

              {/* Step 2: Gate 2 Role & Tier */}
              <div className={`p-4 rounded-xl border transition ${
                selectedCust.gate2Status === 'APPROVED' ? 'bg-slate-950/40 border-purple-500/30' : 'bg-slate-950/80 border-slate-800'
              }`}>
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2 font-bold text-xs text-slate-200">
                    <Award className="w-4 h-4 text-purple-400" />
                    CỬA ẢI 2: CẤP PHÁT VAI TRÒ & XẾP HẠNG TIER GIAO DỊCH
                  </div>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-mono font-bold ${
                    selectedCust.gate2Status === 'APPROVED' ? 'bg-purple-500/10 text-purple-400 border border-purple-500/30' : 'bg-slate-700 text-slate-400'
                  }`}>
                    {selectedCust.gate2Status}
                  </span>
                </div>
                <div className="text-xs text-slate-400 mb-3">
                  Tier: <span className="font-bold text-slate-200">{selectedCust.assignedTier}</span> &bull; Hạn mức ngày: <span className="font-bold text-emerald-400 font-mono">{(selectedCust.dailyLimit).toLocaleString()} VND</span>
                </div>
                {selectedCust.gate2Status === 'PENDING' && (
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleApproveGate2(selectedCust, 'TIER_2', 50000000.0)}
                      className="px-4 py-2 rounded-xl bg-purple-600 hover:bg-purple-500 text-white font-bold text-xs shadow transition cursor-pointer"
                    >
                      Cấp Hạng Tier 2 (50Tr/ngày)
                    </button>
                    <button
                      onClick={() => handleApproveGate2(selectedCust, 'TIER_3', 500000000.0)}
                      className="px-4 py-2 rounded-xl bg-purple-800 hover:bg-purple-700 text-white font-bold text-xs shadow transition cursor-pointer"
                    >
                      Cấp Hạng Tier 3 VIP (500Tr/ngày)
                    </button>
                  </div>
                )}
              </div>

              {/* Step 3: Gate 3 Dual Wallets Provisioning */}
              <div className={`p-4 rounded-xl border transition ${
                selectedCust.gate3Status === 'PROVISIONED' ? 'bg-slate-950/40 border-amber-500/30' : 'bg-slate-950/80 border-slate-800'
              }`}>
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2 font-bold text-xs text-slate-200">
                    <Wallet className="w-4 h-4 text-amber-400" />
                    CỬA ẢI 3: KÍCH HOẠT CẶP TÀI KHOẢN SỔ CÁI CORE-BANKING
                  </div>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-mono font-bold ${
                    selectedCust.gate3Status === 'PROVISIONED' ? 'bg-amber-500/10 text-amber-300 border border-amber-500/30' : 'bg-slate-700 text-slate-400'
                  }`}>
                    {selectedCust.gate3Status}
                  </span>
                </div>
                {selectedCust.gate3Status === 'PROVISIONED' ? (
                  <div className="bg-slate-950 rounded-xl p-3 border border-slate-800 font-mono text-xs text-amber-300 space-y-1">
                    <div>Ví Khả Dụng: <span className="text-slate-100">{selectedCust.availableAccountNo}</span></div>
                    <div>Ví Heo Đất Tiết Kiệm: <span className="text-slate-100">{selectedCust.savingsAccountNo}</span></div>
                  </div>
                ) : selectedCust.gate3Status === 'PENDING' ? (
                  <button
                    onClick={() => handleApproveGate3(selectedCust)}
                    className="px-4 py-2 rounded-xl bg-amber-600 hover:bg-amber-500 text-white font-bold text-xs shadow transition cursor-pointer"
                  >
                    Mở Cặp Tài Khoản Sổ Cái Kép (Core-Banking)
                  </button>
                ) : (
                  <p className="text-xs text-slate-500">Chờ hoàn tất Gate 2 trước khi mở tài khoản sổ cái.</p>
                )}
              </div>

              {/* Step 4: Gate 4 IoT Piggy Pairing */}
              <div className={`p-4 rounded-xl border transition ${
                selectedCust.gate4Status === 'BOUND' ? 'bg-slate-950/40 border-emerald-500/30' : 'bg-slate-950/80 border-slate-800'
              }`}>
                <div className="flex items-center justify-between mb-3">
                  <div className="flex items-center gap-2 font-bold text-xs text-slate-200">
                    <RadioReceiver className="w-4 h-4 text-emerald-400" />
                    CỬA ẢI 4: PHÊ CHUẨN GHÉP ĐÔI HEO ĐẤT IOT & THIẾT BỊ TIN CẬY
                  </div>
                  <span className={`px-2 py-0.5 rounded text-[10px] font-mono font-bold ${
                    selectedCust.gate4Status === 'BOUND' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30' : 'bg-slate-700 text-slate-400'
                  }`}>
                    {selectedCust.gate4Status}
                  </span>
                </div>
                {selectedCust.gate4Status === 'BOUND' ? (
                  <div className="bg-slate-950 rounded-xl p-3 border border-slate-800 font-mono text-xs text-emerald-300 space-y-1">
                    <div>Mã Serial Heo Đất: <span className="text-slate-100 font-bold">{selectedCust.boundDeviceSerial}</span></div>
                    <div>Dòng Phần Cứng: <span className="text-slate-100">{selectedCust.boundDeviceModel}</span></div>
                  </div>
                ) : selectedCust.gate4Status === 'PENDING' ? (
                  <button
                    onClick={() => handleApproveGate4(selectedCust, 'PIGGY-VN-8809')}
                    className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs shadow transition cursor-pointer"
                  >
                    Ghép Đôi Heo Đất Serial #PIGGY-VN-8809 (Gate 4)
                  </button>
                ) : (
                  <p className="text-xs text-slate-500">Chờ hoàn tất Gate 3 trước khi ghép đôi phần cứng.</p>
                )}
              </div>
            </div>
          ) : (
            <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-16 text-center text-slate-500">
              <UserCheck className="w-12 h-12 mx-auto mb-3 text-cyan-500/40" />
              <p className="text-sm font-semibold text-slate-400">Chọn một khách hàng từ danh sách bên trái</p>
              <p className="text-xs text-slate-600 mt-1">để tiến hành thẩm định và phê chuẩn theo quy trình 4 Cửa ải.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
