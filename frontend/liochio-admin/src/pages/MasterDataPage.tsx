import React, { useState } from 'react';
import { 
  Database, 
  MapPin, 
  GitBranch, 
  Landmark, 
  Coins, 
  Search, 
  Plus, 
  CheckCircle2, 
  Percent,
  Layers,
  ArrowRight
} from 'lucide-react';
import { Card, Badge } from '../components/common/CardAndBadge';
import { Button } from '../components/common/Button';

export const MasterDataPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'geography' | 'branches' | 'banks' | 'currencies'>('geography');
  const [searchTerm, setSearchTerm] = useState('');

  const [provinces, setProvinces] = useState([
    { code: 'VN-HN', name: 'Thành phố Hà Nội', region: 'Bắc Bộ', branchCount: 42, active: true },
    { code: 'VN-SG', name: 'Thành phố Hồ Chí Minh', region: 'Nam Bộ', branchCount: 58, active: true },
    { code: 'VN-DN', name: 'Thành phố Đà Nẵng', region: 'Trung Bộ', branchCount: 16, active: true },
    { code: 'VN-HP', name: 'Thành phố Hải Phòng', region: 'Bắc Bộ', branchCount: 14, active: true },
    { code: 'VN-CT', name: 'Thành phố Cần Thơ', region: 'Nam Bộ', branchCount: 12, active: true },
    { code: 'VN-BD', name: 'Tỉnh Bình Dương', region: 'Nam Bộ', branchCount: 18, active: true },
    { code: 'VN-DNA', name: 'Tỉnh Đồng Nai', region: 'Nam Bộ', branchCount: 15, active: true },
    { code: 'VN-KH', name: 'Tỉnh Khánh Hòa', region: 'Trung Bộ', branchCount: 9, active: true },
  ]);

  const [banks] = useState([
    { code: 'VPB', name: 'Ngân hàng TMCP Việt Nam Thịnh Vượng (VPBank)', swift: 'VPBKVNVX', napasCode: '970432', active: true },
    { code: 'TCB', name: 'Ngân hàng TMCP Kỹ Thương Việt Nam (Techcombank)', swift: 'VTCBVNVX', napasCode: '970407', active: true },
    { code: 'MBB', name: 'Ngân hàng TMCP Quân Đội (MB Bank)', swift: 'MSCBVNVX', napasCode: '970422', active: true },
    { code: 'VCB', name: 'Ngân hàng TMCP Ngoại Thương Việt Nam (Vietcombank)', swift: 'BFTVVNVX', napasCode: '970436', active: true },
    { code: 'BIDV', name: 'Ngân hàng TMCP Đầu Tư & Phát Triển Việt Nam', swift: 'BIDVVNVX', napasCode: '970418', active: true },
  ]);

  const [currencies] = useState([
    { code: 'VND', name: 'Việt Nam Đồng', symbol: '₫', fxRate: 1.0, isBase: true, vatRate: '8% / 10%' },
    { code: 'USD', name: 'US Dollar', symbol: '$', fxRate: 25450.0, isBase: false, vatRate: '0%' },
    { code: 'EUR', name: 'Euro', symbol: '€', fxRate: 27800.0, isBase: false, vatRate: '0%' },
    { code: 'SGD', name: 'Singapore Dollar', symbol: 'S$', fxRate: 19200.0, isBase: false, vatRate: '0%' },
  ]);

  const [branches] = useState([
    { code: 'BR-HN-001', name: 'Hội Sở Chính Liochio Platform', province: 'Hà Nội', address: 'Tòa nhà Landmark 72, Nam Từ Liêm', status: 'ACTIVE' },
    { code: 'BR-SG-001', name: 'Trung Tâm Công Nghệ & Vận Hành Miền Nam', province: 'TP. Hồ Chí Minh', address: 'Quận 1, TP. Hồ Chí Minh', status: 'ACTIVE' },
    { code: 'BR-DN-001', name: 'Chi Nhánh Đối Soát Miền Trung', province: 'Đà Nẵng', address: 'Hải Châu, TP. Đà Nẵng', status: 'ACTIVE' },
  ]);

  return (
    <div className="space-y-6 animate-fade-in max-w-7xl">
      {/* Header */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-slate-900/80 border border-slate-800 p-6 rounded-3xl backdrop-blur-xl">
        <div>
          <div className="flex items-center gap-2 font-mono text-xs text-amber-400 font-bold mb-1">
            <Database className="w-4 h-4" />
            <span>SA_MASTER_DATA &bull; GLOBAL MASTER DATA REPOSITORY</span>
          </div>
          <h1 className="text-2xl font-black text-white tracking-tight">
            Quản Trị Danh Mục Tham Số Gốc Toàn Sàn
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Cơ sở dữ liệu danh mục dùng chung: 63 Tỉnh/Thành phố, Mạng lưới chi nhánh, Mã SWIFT/BIC NAPAS và Bảng tỷ giá tiền tệ.
          </p>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-800/80 gap-2 pb-2 text-xs font-bold">
        <button
          onClick={() => setActiveTab('geography')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'geography'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <MapPin className="w-4 h-4" />
          <span>SA_MD_GEOGRAPHY (63 Tỉnh/Thành)</span>
        </button>

        <button
          onClick={() => setActiveTab('branches')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'branches'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <GitBranch className="w-4 h-4" />
          <span>SA_MD_BRANCH (Mạng Lưới Chi Nhánh)</span>
        </button>

        <button
          onClick={() => setActiveTab('banks')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'banks'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <Landmark className="w-4 h-4" />
          <span>SA_MD_BANK_NETWORK (Ngân Hàng & SWIFT)</span>
        </button>

        <button
          onClick={() => setActiveTab('currencies')}
          className={`flex items-center gap-2 px-4 py-2.5 rounded-xl transition cursor-pointer ${
            activeTab === 'currencies'
              ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
              : 'text-slate-400 hover:text-white'
          }`}
        >
          <Coins className="w-4 h-4" />
          <span>SA_MD_CURRENCY_TAX (Tiền Tệ, Tỷ Giá & VAT)</span>
        </button>
      </div>

      {/* SEARCH */}
      <div className="flex items-center gap-3 bg-slate-900/60 p-3 rounded-2xl border border-slate-800">
        <Search className="w-4 h-4 text-slate-500 ml-2" />
        <input
          type="text"
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)}
          placeholder="Tìm kiếm danh mục theo mã, tên..."
          className="w-full bg-transparent text-xs text-white placeholder-slate-500 focus:outline-none font-mono"
        />
      </div>

      {/* TAB 1: GEOGRAPHY */}
      {activeTab === 'geography' && (
        <Card className="p-0 overflow-hidden">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="bg-slate-950/80 text-slate-400 uppercase tracking-wider text-[10px] font-bold border-b border-slate-800 font-mono">
              <tr>
                <th className="py-3 px-4">Mã Tỉnh</th>
                <th className="py-3 px-4">Tỉnh / Thành Phố</th>
                <th className="py-3 px-4">Khu Vực Địa Lý</th>
                <th className="py-3 px-4">Số Chi Nhánh</th>
                <th className="py-3 px-4">Trạng Thái</th>
                <th className="py-3 px-4 text-right">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {provinces.filter(p => p.name.toLowerCase().includes(searchTerm.toLowerCase()) || p.code.toLowerCase().includes(searchTerm.toLowerCase())).map(p => (
                <tr key={p.code} className="hover:bg-slate-800/30 transition">
                  <td className="py-3.5 px-4 font-mono font-bold text-amber-400">{p.code}</td>
                  <td className="py-3.5 px-4 font-bold text-white">{p.name}</td>
                  <td className="py-3.5 px-4">
                    <Badge variant={p.region === 'Bắc Bộ' ? 'info' : p.region === 'Nam Bộ' ? 'success' : 'warning'}>
                      {p.region}
                    </Badge>
                  </td>
                  <td className="py-3.5 px-4 font-mono">{p.branchCount} Điểm</td>
                  <td className="py-3.5 px-4">
                    <Badge variant={p.active ? 'success' : 'neutral'}>
                      {p.active ? 'ACTIVE' : 'DISABLED'}
                    </Badge>
                  </td>
                  <td className="py-3.5 px-4 text-right">
                    <button
                      onClick={() => setProvinces(provinces.map(item => item.code === p.code ? { ...item, active: !item.active } : item))}
                      className="px-2.5 py-1 rounded-lg text-[11px] font-bold bg-slate-800 hover:bg-slate-700 text-slate-300 border border-slate-700 transition cursor-pointer"
                    >
                      {p.active ? 'Tắt' : 'Bật'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}

      {/* TAB 2: BRANCH NETWORK */}
      {activeTab === 'branches' && (
        <Card className="p-0 overflow-hidden">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="bg-slate-950/80 text-slate-400 uppercase tracking-wider text-[10px] font-bold border-b border-slate-800 font-mono">
              <tr>
                <th className="py-3 px-4">Mã Chi Nhánh</th>
                <th className="py-3 px-4">Tên Điểm Giao Dịch</th>
                <th className="py-3 px-4">Tỉnh / Thành Phố</th>
                <th className="py-3 px-4">Địa Chỉ Vật Lý</th>
                <th className="py-3 px-4">Trạng Thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {branches.map(b => (
                <tr key={b.code} className="hover:bg-slate-800/30 transition">
                  <td className="py-3.5 px-4 font-mono font-bold text-amber-400">{b.code}</td>
                  <td className="py-3.5 px-4 font-bold text-white">{b.name}</td>
                  <td className="py-3.5 px-4 text-slate-300">{b.province}</td>
                  <td className="py-3.5 px-4 text-slate-400">{b.address}</td>
                  <td className="py-3.5 px-4">
                    <Badge variant="success">{b.status}</Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}

      {/* TAB 3: BANKS & SWIFT */}
      {activeTab === 'banks' && (
        <Card className="p-0 overflow-hidden">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="bg-slate-950/80 text-slate-400 uppercase tracking-wider text-[10px] font-bold border-b border-slate-800 font-mono">
              <tr>
                <th className="py-3 px-4">Mã Ngân Hàng</th>
                <th className="py-3 px-4">Tên Tổ Chức Tín Dụng</th>
                <th className="py-3 px-4">Mã SWIFT/BIC</th>
                <th className="py-3 px-4">Mã NAPAS BIN</th>
                <th className="py-3 px-4">Trạng Thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono">
              {banks.map(b => (
                <tr key={b.code} className="hover:bg-slate-800/30 transition">
                  <td className="py-3.5 px-4 font-bold text-amber-400">{b.code}</td>
                  <td className="py-3.5 px-4 font-sans font-bold text-white">{b.name}</td>
                  <td className="py-3.5 px-4 text-indigo-400">{b.swift}</td>
                  <td className="py-3.5 px-4 text-slate-300">{b.napasCode}</td>
                  <td className="py-3.5 px-4 font-sans">
                    <Badge variant="success">ACTIVE</Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}

      {/* TAB 4: CURRENCIES & TAX */}
      {activeTab === 'currencies' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {currencies.map(c => (
            <Card key={c.code} className="space-y-3">
              <div className="flex justify-between items-center">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-xl bg-amber-500/10 border border-amber-500/30 flex items-center justify-center font-bold text-amber-400 font-mono text-base">
                    {c.symbol}
                  </div>
                  <div>
                    <h3 className="font-bold text-white text-sm">{c.name} ({c.code})</h3>
                    <p className="text-[11px] text-slate-400 font-mono">ISO 4217 Currency Code</p>
                  </div>
                </div>
                {c.isBase && <Badge variant="primary">Tiền Tệ Gốc</Badge>}
              </div>

              <div className="grid grid-cols-2 gap-2 text-xs pt-2 border-t border-slate-800">
                <div className="p-2.5 bg-slate-950/60 rounded-xl border border-slate-800">
                  <span className="text-slate-400 block text-[10px]">Tỷ Giá Quy Đổi (FX Rate)</span>
                  <span className="font-mono font-bold text-emerald-400 mt-0.5 block">{c.fxRate.toLocaleString('vi-VN')} VND</span>
                </div>
                <div className="p-2.5 bg-slate-950/60 rounded-xl border border-slate-800">
                  <span className="text-slate-400 block text-[10px]">Thuế Suất VAT Áp Dụng</span>
                  <span className="font-mono font-bold text-indigo-400 mt-0.5 block">{c.vatRate}</span>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};
