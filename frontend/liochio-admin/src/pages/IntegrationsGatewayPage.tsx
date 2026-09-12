import React, { useState } from 'react';
import {
  Network,
  Fingerprint,
  MessageSquare,
  Mail,
  ScanLine,
  FileSpreadsheet,
  CheckCircle2,
  RefreshCw,
  Plus,
  Key,
  Globe,
  Radio,
  Send,
  Sliders,
  ShieldCheck,
} from 'lucide-react';

interface IntegrationProvider {
  id: string;
  name: string;
  category: 'SSO' | 'SMS' | 'SMTP' | 'EKYC' | 'EINVOICE';
  status: 'ACTIVE' | 'TESTING' | 'INACTIVE' | 'ERROR';
  endpoint: string;
  apiKeyMasked: string;
  quotaUsed: number;
  quotaLimit: number;
  latencyMs: number;
  lastTested: string;
}

const INITIAL_PROVIDERS: IntegrationProvider[] = [
  { id: 'INT-01', name: 'Google Identity OAuth2', category: 'SSO', status: 'ACTIVE', endpoint: 'https://accounts.google.com/o/oauth2/v2/auth', apiKeyMasked: 'AIzaSy********************4Jz8', quotaUsed: 14520, quotaLimit: 100000, latencyMs: 42, lastTested: '2026-09-10 16:45:00' },
  { id: 'INT-02', name: 'Apple Sign-In (Private Relay)', category: 'SSO', status: 'ACTIVE', endpoint: 'https://appleid.apple.com/auth/authorize', apiKeyMasked: 'team_id:APPLE_SEC_***_KEY', quotaUsed: 8900, quotaLimit: 50000, latencyMs: 65, lastTested: '2026-09-10 16:40:00' },
  { id: 'INT-03', name: 'Enterprise Keycloak Identity', category: 'SSO', status: 'ACTIVE', endpoint: 'https://sso.liochio.vn/auth/realms/liochio-fintech', apiKeyMasked: 'kc-client-sec-********************9a8f', quotaUsed: 42100, quotaLimit: 200000, latencyMs: 18, lastTested: '2026-09-10 17:00:00' },
  
  { id: 'INT-04', name: 'Viettel Telecom SMS Brandname', category: 'SMS', status: 'ACTIVE', endpoint: 'https://api.viettel.vn/sms/v2/brandname-send', apiKeyMasked: 'VTEL_ACC_********************812a', quotaUsed: 48900, quotaLimit: 100000, latencyMs: 120, lastTested: '2026-09-10 16:55:00' },
  { id: 'INT-05', name: 'VNPT SMS OTP Gateway', category: 'SMS', status: 'ACTIVE', endpoint: 'https://smsapi.vnpt.vn/otp/broadcast', apiKeyMasked: 'VNPT_OTP_********************bb31', quotaUsed: 31200, quotaLimit: 80000, latencyMs: 95, lastTested: '2026-09-10 16:50:00' },
  { id: 'INT-06', name: 'SpeedSMS Fallback Router', category: 'SMS', status: 'TESTING', endpoint: 'https://api.speedsms.vn/index.php/sms/send', apiKeyMasked: 'SPEED_KEY_******************200x', quotaUsed: 1200, quotaLimit: 20000, latencyMs: 150, lastTested: '2026-09-10 15:30:00' },
  
  { id: 'INT-07', name: 'AWS SES Production Mailer', category: 'SMTP', status: 'ACTIVE', endpoint: 'email-smtp.ap-southeast-1.amazonaws.com:587', apiKeyMasked: 'AKIA********************88KL', quotaUsed: 89200, quotaLimit: 500000, latencyMs: 38, lastTested: '2026-09-10 17:05:00' },
  { id: 'INT-08', name: 'SendGrid Transactional Relay', category: 'SMTP', status: 'ACTIVE', endpoint: 'smtp.sendgrid.net:587', apiKeyMasked: 'SG.************************************12', quotaUsed: 23100, quotaLimit: 100000, latencyMs: 55, lastTested: '2026-09-10 16:10:00' },
  
  { id: 'INT-09', name: 'BCA CCCD Chip eKYC Engine', category: 'EKYC', status: 'ACTIVE', endpoint: 'https://ekyc.bca.gov.vn/api/v3/verify-chip-cccd', apiKeyMasked: 'BCA_EKYC_********************9900', quotaUsed: 12450, quotaLimit: 50000, latencyMs: 310, lastTested: '2026-09-10 16:30:00' },
  { id: 'INT-10', name: 'VNPT eKYC Face Matching & OCR', category: 'EKYC', status: 'ACTIVE', endpoint: 'https://api.vnpt.vn/ai/v1/ocr-id-card', apiKeyMasked: 'VNPT_AI_********************4567', quotaUsed: 19800, quotaLimit: 50000, latencyMs: 240, lastTested: '2026-09-10 16:25:00' },
  { id: 'INT-11', name: 'Liochio OCR Receipt Invoice Scanner', category: 'EKYC', status: 'ACTIVE', endpoint: 'http://ai-service:8089/api/v1/ocr/receipt', apiKeyMasked: 'LIOCHIO_INTERNAL_TOKEN_OCR', quotaUsed: 45000, quotaLimit: 100000, latencyMs: 85, lastTested: '2026-09-10 17:02:00' },
  
  { id: 'INT-12', name: 'VNPT E-Invoice Tax Gateway', category: 'EINVOICE', status: 'ACTIVE', endpoint: 'https://api-sinvoice.vnpt.vn/v1/tax-invoice', apiKeyMasked: 'VNPT_INV_********************7788', quotaUsed: 7800, quotaLimit: 30000, latencyMs: 180, lastTested: '2026-09-10 16:00:00' },
  { id: 'INT-13', name: 'MISA meInvoice Enterprise Connector', category: 'EINVOICE', status: 'ACTIVE', endpoint: 'https://www.meinvoice.vn/api/v1/publish', apiKeyMasked: 'MISA_SEC_********************1122', quotaUsed: 3200, quotaLimit: 20000, latencyMs: 195, lastTested: '2026-09-10 15:50:00' },
];

export const IntegrationsGatewayPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'ALL' | 'SSO' | 'SMS' | 'SMTP' | 'EKYC' | 'EINVOICE'>('ALL');
  const [providers, setProviders] = useState<IntegrationProvider[]>(INITIAL_PROVIDERS);
  const [testResult, setTestResult] = useState<{ id: string; msg: string; status: 'ok' | 'error' } | null>(null);
  const [testPhoneNumber, setTestPhoneNumber] = useState('0987654321');
  const [testEmail, setTestEmail] = useState('superadmin@liochio.vn');

  const filtered = activeTab === 'ALL' ? providers : providers.filter(p => p.category === activeTab);

  const handleTestConnection = (id: string, name: string) => {
    setTestResult({ id, msg: `Đang gửi lệnh kiểm thử kết nối tới ${name}...`, status: 'ok' });
    setTimeout(() => {
      setTestResult({
        id,
        msg: `Kiểm thử ${name} thành công! HTTP 200 OK - Roundtrip latency: ${(Math.random() * 50 + 20).toFixed(0)}ms`,
        status: 'ok',
      });
      setProviders(prev =>
        prev.map(p => (p.id === id ? { ...p, lastTested: new Date().toISOString().replace('T', ' ').substring(0, 19) } : p))
      );
    }, 800);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-900/60 p-6 rounded-2xl border border-slate-800 backdrop-blur-xl">
        <div>
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2.5 rounded-xl bg-purple-500/10 border border-purple-500/20 text-purple-400">
              <Network className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-white tracking-wide">Cổng Kết Nối Ngoại Vi & Đối Tác</h1>
              <p className="text-xs text-slate-400">Quản trị toàn bộ Gateway SSO OAuth2, SMS Brandname, SMTP Server, eKYC và Hóa Đơn Điện Tử</p>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setTestResult({ id: 'all', msg: 'Đang kiểm tra tình trạng kết nối toàn bộ 13 Gateway ngoại vi...', status: 'ok' })}
            className="flex items-center gap-2 px-3.5 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl border border-slate-700 transition shadow-sm"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            Ping Toàn Bộ Gateways
          </button>
          <button
            onClick={() => alert('Mở form cấu hình Gateway ngoại vi mới')}
            className="flex items-center gap-2 px-3.5 py-2 bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold rounded-xl transition shadow-lg shadow-purple-900/30"
          >
            <Plus className="w-3.5 h-3.5" />
            Thêm Gateway Mới
          </button>
        </div>
      </div>

      {/* Sub Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-800 pb-2 overflow-x-auto">
        {[
          { key: 'ALL', label: 'Tất Cả Cổng (13)', icon: Network, code: 'SA_INTEGRATIONS' },
          { key: 'SSO', label: 'SSO & OAuth2 (3)', icon: Fingerprint, code: 'SA_INT_SSO_OAUTH' },
          { key: 'SMS', label: 'SMS Brandname & OTP (3)', icon: MessageSquare, code: 'SA_INT_TELCO_SMS' },
          { key: 'SMTP', label: 'SMTP Gateway (2)', icon: Mail, code: 'SA_INT_SMTP_GATEWAY' },
          { key: 'EKYC', label: 'eKYC & OCR Chip (3)', icon: ScanLine, code: 'SA_INT_EKYC_OCR' },
          { key: 'EINVOICE', label: 'Hóa Đơn Điện Tử (2)', icon: FileSpreadsheet, code: 'SA_INT_EINVOICE' },
        ].map(t => {
          const Icon = t.icon;
          const isActive = activeTab === t.key;
          return (
            <button
              key={t.key}
              onClick={() => setActiveTab(t.key as any)}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-semibold transition whitespace-nowrap border ${
                isActive
                  ? 'bg-purple-500/10 border-purple-500/40 text-purple-400 shadow-sm'
                  : 'bg-slate-900/40 border-slate-800/80 text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <Icon className="w-4 h-4" />
              <span>{t.label}</span>
              <span className="text-[10px] font-mono opacity-60 ml-1">[{t.code}]</span>
            </button>
          );
        })}
      </div>

      {/* Active Ping / Test feedback banner */}
      {testResult && (
        <div className={`p-4 rounded-xl border flex items-center justify-between text-xs animate-in fade-in duration-300 ${
          testResult.status === 'ok' ? 'bg-emerald-950/30 border-emerald-800/50 text-emerald-300' : 'bg-rose-950/30 border-rose-800/50 text-rose-300'
        }`}>
          <div className="flex items-center gap-3">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>{testResult.msg}</span>
          </div>
          <button onClick={() => setTestResult(null)} className="text-slate-400 hover:text-white text-xs">Đóng</button>
        </div>
      )}

      {/* Main Grid: Provider Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {filtered.map(p => {
          const quotaPercent = Math.min(100, Math.round((p.quotaUsed / p.quotaLimit) * 100));
          return (
            <div
              key={p.id}
              className="bg-slate-900/60 rounded-2xl border border-slate-800 p-5 flex flex-col justify-between hover:border-slate-700 transition relative overflow-hidden group shadow-lg"
            >
              {/* Category indicator line */}
              <div className={`absolute top-0 left-0 right-0 h-1 ${
                p.category === 'SSO' ? 'bg-blue-500' :
                p.category === 'SMS' ? 'bg-emerald-500' :
                p.category === 'SMTP' ? 'bg-amber-500' :
                p.category === 'EKYC' ? 'bg-purple-500' : 'bg-rose-500'
              }`} />

              <div>
                <div className="flex items-start justify-between gap-2 mb-3">
                  <div>
                    <span className="text-[10px] font-mono text-purple-400 bg-purple-500/10 px-2 py-0.5 rounded border border-purple-500/20">
                      {p.id} • {p.category}
                    </span>
                    <h3 className="text-sm font-bold text-slate-100 mt-1.5">{p.name}</h3>
                  </div>
                  <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold border ${
                    p.status === 'ACTIVE' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                    p.status === 'TESTING' ? 'bg-amber-500/10 text-amber-400 border-amber-500/20' : 'bg-rose-500/10 text-rose-400 border-rose-500/20'
                  }`}>
                    {p.status}
                  </span>
                </div>

                {/* Endpoint & Secret */}
                <div className="space-y-2 bg-slate-950/60 p-3 rounded-xl border border-slate-800/80 mb-4 text-xs font-mono">
                  <div className="flex items-center gap-1.5 text-slate-400 overflow-hidden">
                    <Globe className="w-3.5 h-3.5 shrink-0 text-slate-500" />
                    <span className="truncate text-[11px] text-slate-300">{p.endpoint}</span>
                  </div>
                  <div className="flex items-center gap-1.5 text-slate-400">
                    <Key className="w-3.5 h-3.5 shrink-0 text-amber-400/80" />
                    <span className="text-[11px] text-amber-300/80">{p.apiKeyMasked}</span>
                  </div>
                </div>

                {/* Usage Quota */}
                <div className="mb-4">
                  <div className="flex justify-between text-[11px] mb-1">
                    <span className="text-slate-400">Hạn mức tháng</span>
                    <span className="text-slate-200 font-mono font-semibold">{p.quotaUsed.toLocaleString()} / {p.quotaLimit.toLocaleString()} ({quotaPercent}%)</span>
                  </div>
                  <div className="w-full bg-slate-800 rounded-full h-1.5 overflow-hidden">
                    <div
                      className={`h-full rounded-full ${
                        quotaPercent > 85 ? 'bg-rose-500' : quotaPercent > 60 ? 'bg-amber-500' : 'bg-purple-500'
                      }`}
                      style={{ width: `${quotaPercent}%` }}
                    />
                  </div>
                </div>

                {/* Telemetry info */}
                <div className="flex items-center justify-between text-[10px] text-slate-400 border-t border-slate-800/60 pt-2.5">
                  <div className="flex items-center gap-1">
                    <Radio className="w-3 h-3 text-emerald-400" />
                    <span>Độ trễ: <strong className="text-emerald-400">{p.latencyMs} ms</strong></span>
                  </div>
                  <span>Ping: {p.lastTested.substring(11)}</span>
                </div>
              </div>

              {/* Action */}
              <div className="mt-4 pt-3 border-t border-slate-800 flex items-center justify-between gap-2">
                <button
                  onClick={() => handleTestConnection(p.id, p.name)}
                  className="flex-1 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-lg border border-slate-700 transition text-center"
                >
                  Kiểm Thử Kết Nối
                </button>
                <button
                  onClick={() => alert(`Mở cấu hình ${p.name}`)}
                  className="px-3 py-1.5 bg-purple-500/10 hover:bg-purple-500/20 text-purple-400 text-xs font-semibold rounded-lg border border-purple-500/20 transition"
                >
                  Sửa
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* Sandbox Test Console Panel */}
      <div className="bg-slate-900/60 rounded-2xl border border-slate-800 p-6 backdrop-blur-xl">
        <h3 className="text-sm font-bold text-slate-200 mb-4 flex items-center gap-2">
          <Send className="w-4 h-4 text-purple-400" />
          Bàn Thử Nghiệm Sandbox Gửi Tin Nhắn / Email / eKYC Trực Tiếp
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-3 bg-slate-950/40 p-4 rounded-xl border border-slate-800/80">
            <h4 className="text-xs font-bold text-slate-300">Test Gửi SMS Brandname OTP</h4>
            <div className="flex gap-2">
              <input
                type="text"
                value={testPhoneNumber}
                onChange={e => setTestPhoneNumber(e.target.value)}
                placeholder="Nhập SĐT nhận OTP (VD: 0987654321)"
                className="flex-1 px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-xs text-white"
              />
              <button
                onClick={() => alert(`Đã gửi OTP kiểm thử mẫu [692810] qua Viettel SMS Brandname tới SĐT: ${testPhoneNumber}`)}
                className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold rounded-lg transition"
              >
                Gửi Thử OTP
              </button>
            </div>
            <p className="text-[11px] text-slate-500">Mẫu tin nhắn: [LIOCHIO] Ma OTP xac thuc SuperAdmin la 692810. Hieu luc trong 5 phut.</p>
          </div>

          <div className="space-y-3 bg-slate-950/40 p-4 rounded-xl border border-slate-800/80">
            <h4 className="text-xs font-bold text-slate-300">Test Gửi Email SMTP Thông Báo</h4>
            <div className="flex gap-2">
              <input
                type="email"
                value={testEmail}
                onChange={e => setTestEmail(e.target.value)}
                placeholder="Nhập email nhận (VD: superadmin@liochio.vn)"
                className="flex-1 px-3 py-2 bg-slate-900 border border-slate-700 rounded-lg text-xs text-white"
              />
              <button
                onClick={() => alert(`Đã gửi Email kiểm thử HTML qua AWS SES tới: ${testEmail}`)}
                className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white text-xs font-bold rounded-lg transition"
              >
                Gửi Thử Email
              </button>
            </div>
            <p className="text-[11px] text-slate-500">Tiêu đề: [TEST-GATEWAY] Xác nhận trạng thái hạ tầng mailer sàn Liochio Fintech Core.</p>
          </div>
        </div>
      </div>
    </div>
  );
};
export default IntegrationsGatewayPage;
