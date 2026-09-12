import React, { useState } from 'react';
import {
  Shield, Key, Cpu, Ban, BellRing, ZapOff, AlertTriangle,
  CheckCircle2, Lock, RefreshCw, Plus, Trash2, Power, Sliders, Radio,
} from 'lucide-react';

interface BlacklistEntry {
  id: string;
  type: 'IP' | 'CCCD' | 'PHONE' | 'DEVICE_MAC' | 'BANK_ACCOUNT';
  value: string;
  reason: string;
  addedBy: string;
  addedAt: string;
  severity: 'HIGH' | 'CRITICAL' | 'MEDIUM';
}

const INITIAL_BLACKLIST: BlacklistEntry[] = [
  { id: 'BL-001', type: 'IP', value: '185.220.101.45', reason: 'Tor Exit Node brute-force gateway login', addedBy: 'Auto-AML-Engine', addedAt: '2026-09-10 14:15:00', severity: 'CRITICAL' },
  { id: 'BL-002', type: 'CCCD', value: '079090001234', reason: 'Danh sách đối tượng lừa đảo tín dụng liên ngân hàng', addedBy: 'superadmin_sec', addedAt: '2026-09-08 10:20:00', severity: 'CRITICAL' },
  { id: 'BL-003', type: 'PHONE', value: '0901999888', reason: 'Spam SMS OTP flood & fake KYC fraud', addedBy: 'superadmin_sec', addedAt: '2026-09-09 18:40:00', severity: 'HIGH' },
  { id: 'BL-004', type: 'DEVICE_MAC', value: '48:2C:6A:1E:9B:40', reason: 'Thiết bị root bypass SafetyNet & MITM proxy', addedBy: 'Auto-AML-Engine', addedAt: '2026-09-10 09:12:00', severity: 'HIGH' },
  { id: 'BL-005', type: 'BANK_ACCOUNT', value: '9988221144 - MBBank', reason: 'Tài khoản nhận tiền lừa đảo chiếm đoạt', addedBy: 'compliance_officer', addedAt: '2026-09-07 11:00:00', severity: 'CRITICAL' },
];

export const PlatformSecurityPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'POLICY' | 'TOKEN' | 'BLACKLIST' | 'ALERTS' | 'CIRCUIT_BREAKER'>('CIRCUIT_BREAKER');
  const [blacklist, setBlacklist] = useState<BlacklistEntry[]>(INITIAL_BLACKLIST);
  const [newEntryType, setNewEntryType] = useState<'IP' | 'CCCD' | 'PHONE' | 'DEVICE_MAC' | 'BANK_ACCOUNT'>('IP');
  const [newEntryValue, setNewEntryValue] = useState('');
  const [newEntryReason, setNewEntryReason] = useState('');
  const [killSwitches, setKillSwitches] = useState({
    globalWithdrawalHalt: false,
    globalDepositHalt: false,
    interbankTransferPause: false,
    iotTelemetryDrop: false,
    maintenanceMode: false,
  });
  const [passPolicy, setPassPolicy] = useState({
    minLength: 12, expiryDays: 90, maxFailedAttempts: 5, enforceMfaSuperAdmin: true, enforceMfaCorpAdmin: true,
  });
  const [jwtState, setJwtState] = useState({
    algorithm: 'RS256', accessTokenTtlMin: 15, refreshTokenTtlDays: 7,
    keyFingerprint: 'SHA256:d8:a4:2f:99:bb:10:ee:44:aa:77:88:99:cc:dd:ee:ff',
    lastRotated: '2026-09-01 00:00:00', redisBlacklistActiveTokens: 142,
  });

  const handleToggleSwitch = (key: keyof typeof killSwitches) => {
    const nextVal = !killSwitches[key];
    if (window.confirm(nextVal ? 'CẢNH BÁO: Bạn có chắc chắn muốn KÍCH HOẠT ngắt mạch khẩn cấp [' + key + ']?' : 'Khôi phục lại bình thường?')) {
      setKillSwitches(prev => ({ ...prev, [key]: nextVal }));
    }
  };

  const handleAddBlacklist = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newEntryValue) return;
    const newEntry: BlacklistEntry = {
      id: 'BL-' + String(blacklist.length + 1).padStart(3, '0'),
      type: newEntryType,
      value: newEntryValue,
      reason: newEntryReason || 'Chặn thủ công bởi SuperAdmin',
      addedBy: 'superadmin_root',
      addedAt: new Date().toISOString().replace('T', ' ').substring(0, 19),
      severity: 'HIGH',
    };
    setBlacklist([newEntry, ...blacklist]);
    setNewEntryValue('');
    setNewEntryReason('');
    alert('Đã thêm ' + newEntryValue + ' vào Sổ Đen Toàn Sàn!');
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-900/60 p-6 rounded-2xl border border-slate-800 backdrop-blur-xl">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400">
            <Shield className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-white tracking-wide">Kiểm Soát Rủi Ro & AML</h1>
            <p className="text-xs text-slate-400">Phòng vệ an ninh mạng, chống rửa tiền tập trung và công tắc ngắt mạch khẩn cấp (Kill-Switch)</p>
          </div>
        </div>
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-semibold">
          <Radio className="w-3.5 h-3.5 animate-pulse" />
          AML Defense: ARMED
        </div>
      </div>

      <div className="flex items-center gap-2 border-b border-slate-800 pb-2 overflow-x-auto">
        {[
          { key: 'CIRCUIT_BREAKER', label: 'Ngắt Mạch Khẩn Cấp (Kill-Switch)', icon: ZapOff, code: 'SA_SEC_CIRCUIT_BREAKER' },
          { key: 'BLACKLIST', label: 'Sổ Đen Toàn Sàn (Global Blacklist)', icon: Ban, code: 'SA_SEC_GLOBAL_BLACKLIST' },
          { key: 'TOKEN', label: 'Vòng Đời Token RS256 & Key', icon: Cpu, code: 'SA_SEC_TOKEN_LIFECYCLE' },
          { key: 'POLICY', label: 'Chính Sách Mật Khẩu & 2FA', icon: Key, code: 'SA_SEC_PASS_POLICY' },
          { key: 'ALERTS', label: 'Điều Phối Cảnh Báo Sự Cố', icon: BellRing, code: 'SA_SEC_ALERT_ROUTING' },
        ].map(t => {
          const Icon = t.icon;
          const isActive = activeTab === t.key;
          return (
            <button
              key={t.key}
              onClick={() => setActiveTab(t.key as any)}
              className={'flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-semibold transition whitespace-nowrap border ' + (
                isActive ? 'bg-rose-500/10 border-rose-500/40 text-rose-400 shadow-sm' : 'bg-slate-900/40 border-slate-800/80 text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              )}
            >
              <Icon className="w-4 h-4" />
              <span>{t.label}</span>
              <span className="text-[10px] font-mono opacity-60 ml-1">[{t.code}]</span>
            </button>
          );
        })}
      </div>

      {activeTab === 'CIRCUIT_BREAKER' && (
        <div className="space-y-6">
          <div className="p-4 rounded-2xl bg-rose-950/30 border border-rose-800/50 flex items-start gap-4">
            <AlertTriangle className="w-6 h-6 text-rose-400 shrink-0 mt-0.5" />
            <div>
              <h3 className="text-sm font-bold text-rose-300">TRUNG TÂM ĐIỀU KHIỂN CÔNG TẮC KHẨN CẤP (GLOBAL KILL-SWITCH)</h3>
              <p className="text-xs text-rose-200/70 mt-1 leading-relaxed">Lệnh ngắt có hiệu lực tức thời trong vòng 50ms tới toàn bộ API Gateway và Microservices.</p>
            </div>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {[
              { key: 'globalWithdrawalHalt', title: 'Đóng Băng Rút Tiền Toàn Sàn', desc: 'Chặn tức thì mọi giao dịch rút tiền và chuyển khoản ra ngoài.' },
              { key: 'globalDepositHalt', title: 'Dừng Tiếp Nhận Nạp Tiền', desc: 'Tạm dừng đối soát Webhook nạp tiền và sinh QR VietQR.' },
              { key: 'interbankTransferPause', title: 'Tạm Dừng Cổng NAPAS 247', desc: 'Ngắt kết nối chuyển mạch liên ngân hàng ISO 8583.' },
              { key: 'iotTelemetryDrop', title: 'Hạ Tải IoT Telemetry Fallback', desc: 'Tạm ngắt nạp telemetry từ heo đất nếu Kafka bị nghẽn.' },
              { key: 'maintenanceMode', title: 'Chế Độ Bảo Trì Hệ Thống', desc: 'Bật màn hình thông báo bảo trì toàn hệ thống.' },
            ].map(sw => {
              const isHalted = killSwitches[sw.key as keyof typeof killSwitches];
              return (
                <div key={sw.key} className={'p-6 rounded-2xl border transition-all ' + (isHalted ? 'bg-rose-950/50 border-rose-600 shadow-xl' : 'bg-slate-900/60 border-slate-800')}>
                  <div className="flex items-center justify-between mb-4">
                    <div className="p-3 rounded-xl bg-rose-500/10 text-rose-400"><ZapOff className="w-6 h-6" /></div>
                    <button
                      onClick={() => handleToggleSwitch(sw.key as any)}
                      className={'px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 ' + (isHalted ? 'bg-rose-600 hover:bg-rose-500 text-white animate-pulse' : 'bg-slate-800 hover:bg-rose-900/40 text-slate-300 border border-slate-700')}
                    >
                      <Power className="w-3.5 h-3.5" />
                      {isHalted ? 'ĐANG NGẮT' : 'BÌNH THƯỜNG'}
                    </button>
                  </div>
                  <h4 className="text-sm font-bold text-white mb-1">{sw.title}</h4>
                  <p className="text-xs text-slate-400">{sw.desc}</p>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {activeTab === 'BLACKLIST' && (
        <div className="space-y-6">
          <form onSubmit={handleAddBlacklist} className="bg-slate-900/60 p-5 rounded-2xl border border-slate-800 space-y-4">
            <h3 className="text-sm font-bold text-white flex items-center gap-2"><Plus className="w-4 h-4 text-rose-400" /> Thêm Đối Tượng Vào Sổ Đen</h3>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div>
                <label className="text-xs text-slate-400 mb-1 block">Loại đối tượng</label>
                <select value={newEntryType} onChange={e => setNewEntryType(e.target.value as any)} className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-xs text-white">
                  <option value="IP">Địa Chỉ IP</option>
                  <option value="CCCD">Số CCCD / Hộ Chiếu</option>
                  <option value="PHONE">Số Điện Thoại</option>
                  <option value="DEVICE_MAC">Địa Chỉ MAC / Device ID</option>
                  <option value="BANK_ACCOUNT">Tài Khoản Ngân Hàng Gian Lận</option>
                </select>
              </div>
              <div className="md:col-span-2">
                <label className="text-xs text-slate-400 mb-1 block">Giá trị chặn</label>
                <input type="text" value={newEntryValue} onChange={e => setNewEntryValue(e.target.value)} placeholder="VD: 192.168.1.1 hoặc 079090001234" className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-xs text-white" />
              </div>
              <div>
                <label className="text-xs text-slate-400 mb-1 block">Lý do</label>
                <input type="text" value={newEntryReason} onChange={e => setNewEntryReason(e.target.value)} placeholder="Lý do vi phạm..." className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-xs text-white" />
              </div>
            </div>
            <div className="flex justify-end">
              <button type="submit" className="px-5 py-2 bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold rounded-xl transition shadow-lg flex items-center gap-2"><Ban className="w-4 h-4" /> Chặn Ngay</button>
            </div>
          </form>
          <div className="bg-slate-900/60 rounded-2xl border border-slate-800 overflow-hidden">
            <table className="w-full text-left text-xs text-slate-300">
              <thead className="bg-slate-950/80 text-slate-400 uppercase text-[10px] tracking-wider border-b border-slate-800">
                <tr><th className="py-3 px-4">Mã</th><th className="py-3 px-4">Loại</th><th className="py-3 px-4">Đối Tượng</th><th className="py-3 px-4">Lý Do</th><th className="py-3 px-4">Mức Độ</th><th className="py-3 px-4 text-right">Xóa</th></tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {blacklist.map(b => (
                  <tr key={b.id} className="hover:bg-slate-800/30 transition">
                    <td className="py-3 px-4 font-mono font-bold text-slate-400">{b.id}</td>
                    <td className="py-3 px-4"><span className="px-2 py-0.5 rounded text-[10px] font-mono bg-slate-800 border border-slate-700">{b.type}</span></td>
                    <td className="py-3 px-4 font-mono font-bold text-white">{b.value}</td>
                    <td className="py-3 px-4 text-slate-300">{b.reason}</td>
                    <td className="py-3 px-4"><span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-rose-500/10 text-rose-400 border border-rose-500/20">{b.severity}</span></td>
                    <td className="py-3 px-4 text-right"><button onClick={() => setBlacklist(blacklist.filter(x => x.id !== b.id))} className="p-1 text-slate-400 hover:text-rose-400"><Trash2 className="w-4 h-4" /></button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeTab === 'TOKEN' && (
        <div className="bg-slate-900/60 p-6 rounded-2xl border border-slate-800 space-y-4">
          <h3 className="text-sm font-bold text-white flex items-center gap-2"><Cpu className="w-4 h-4 text-rose-400" /> Cấu Hình JWT RS256 & Thu Hồi Redis</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
            <div className="p-4 bg-slate-950 rounded-xl border border-slate-800 space-y-2">
              <div className="flex justify-between"><span className="text-slate-400">Thuật toán</span><span className="font-mono text-emerald-400 font-bold">{jwtState.algorithm}</span></div>
              <div className="flex justify-between"><span className="text-slate-400">Access Token TTL</span><span className="font-mono text-white">{jwtState.accessTokenTtlMin} Phút</span></div>
              <div className="flex justify-between"><span className="text-slate-400">Refresh Token TTL</span><span className="font-mono text-white">{jwtState.refreshTokenTtlDays} Ngày</span></div>
            </div>
            <div className="p-4 bg-slate-950 rounded-xl border border-slate-800 space-y-2">
              <div className="flex justify-between"><span className="text-slate-400">Redis Blacklist Active</span><span className="font-mono text-rose-400 font-bold">{jwtState.redisBlacklistActiveTokens} Tokens</span></div>
              <div className="flex justify-between"><span className="text-slate-400">Lần xoay khóa gần nhất</span><span className="font-mono text-slate-300">{jwtState.lastRotated}</span></div>
            </div>
          </div>
          <div className="pt-2 flex justify-end gap-3">
            <button onClick={() => alert('Đã kích hoạt xoay khóa RSA 4096-bit!')} className="px-4 py-2 bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 font-bold rounded-xl border border-amber-500/20 text-xs transition"><RefreshCw className="w-3.5 h-3.5 inline mr-1" /> Xoay Khóa Ký RS256</button>
            <button onClick={() => alert('Đã gửi tín hiệu Global Revoke!')} className="px-4 py-2 bg-rose-600 hover:bg-rose-500 text-white font-bold rounded-xl text-xs transition shadow-lg">Thu Hồi Khẩn Cấp Toàn Bộ Token</button>
          </div>
        </div>
      )}

      {activeTab === 'POLICY' && (
        <div className="bg-slate-900/60 p-6 rounded-2xl border border-slate-800 space-y-4">
          <h3 className="text-sm font-bold text-white flex items-center gap-2"><Lock className="w-4 h-4 text-rose-400" /> Chính Sách Mật Khẩu & 2FA Toàn Sàn</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
            <div><label className="text-slate-400 block mb-1">Độ dài tối thiểu</label><input type="number" value={passPolicy.minLength} onChange={e => setPassPolicy({...passPolicy, minLength: Number(e.target.value)})} className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-white" /></div>
            <div><label className="text-slate-400 block mb-1">Thời hạn mật khẩu (Ngày)</label><input type="number" value={passPolicy.expiryDays} onChange={e => setPassPolicy({...passPolicy, expiryDays: Number(e.target.value)})} className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-white" /></div>
            <div><label className="text-slate-400 block mb-1">Số lần thử sai tối đa</label><input type="number" value={passPolicy.maxFailedAttempts} onChange={e => setPassPolicy({...passPolicy, maxFailedAttempts: Number(e.target.value)})} className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-white" /></div>
          </div>
          <div className="flex justify-end"><button onClick={() => alert('Đã lưu chính sách!')} className="px-5 py-2 bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold rounded-xl transition shadow-lg">Lưu Chính Sách</button></div>
        </div>
      )}

      {activeTab === 'ALERTS' && (
        <div className="bg-slate-900/60 p-6 rounded-2xl border border-slate-800 space-y-4">
          <h3 className="text-sm font-bold text-white flex items-center gap-2"><BellRing className="w-4 h-4 text-purple-400" /> Quy Tắc Điều Phối Cảnh Báo Sự Cố</h3>
          <div className="space-y-3">
            {[
              { level: 'P1 - CRITICAL', ch: 'Slack #secops-critical + SMS Hotline SuperAdmin + PagerDuty', cond: 'Lỗ hổng rút tiền, Spike lỗi 5xx > 15%' },
              { level: 'P2 - HIGH', ch: 'Slack #secops-alerts + Email Lead SRE', cond: 'Độ trễ NAPAS > 2000ms, Kafka Consumer Lag > 5000 msg' },
              { level: 'P3 - MEDIUM', ch: 'Slack #fintech-monitoring', cond: 'Nhập sai mật khẩu vượt ngưỡng 5 lần, eKYC Face match < 60%' },
            ].map((r, i) => (
              <div key={i} className="p-4 rounded-xl bg-slate-950 border border-slate-800 flex justify-between items-center text-xs">
                <div><span className="font-mono font-bold text-rose-400">[{r.level}]</span> <strong className="text-slate-200 ml-2">{r.cond}</strong><div className="text-[11px] text-slate-400 mt-1">Chuyển tiếp: {r.ch}</div></div>
                <button onClick={() => alert('Đã gửi test alert!')} className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg transition">Test</button>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
export default PlatformSecurityPage;