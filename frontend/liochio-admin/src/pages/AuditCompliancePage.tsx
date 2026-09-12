import React, { useState } from 'react';
import {
  FileCheck, FileText, CheckCircle2, History, Archive, Shield,
  Clock, Search, Download, Filter, Eye, AlertCircle, Hash, Database,
} from 'lucide-react';

interface LegalTerm {
  id: string;
  code: string;
  title: string;
  targetUser: 'RETAIL' | 'CORPORATE' | 'ALL';
  version: string;
  effectiveDate: string;
  status: 'PUBLISHED' | 'DRAFT' | 'ARCHIVED';
  mandatoryReConsent: boolean;
}

interface ConsentRecord {
  id: string;
  userId: string;
  userName: string;
  userType: 'RETAIL' | 'CORP';
  termVersion: string;
  acceptedAt: string;
  ipAddress: string;
  deviceFingerprint: string;
  signatureSha256: string;
}

interface ImmutableWormLog {
  blockIndex: number;
  timestamp: string;
  actor: string;
  action: string;
  targetResource: string;
  details: string;
  blockHash: string;
  prevBlockHash: string;
  wormVerified: boolean;
}

export const AuditCompliancePage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'TERMS' | 'CONSENT' | 'WORM' | 'RETENTION'>('WORM');

  const [terms] = useState<LegalTerm[]>([
    { id: 'TERM-01', code: 'TERMS_OF_SERVICE_RETAIL', title: 'Điều Khoản Dịch Vụ Ví Heo Đất Cá Nhân', targetUser: 'RETAIL', version: 'v3.2.0', effectiveDate: '2026-09-01', status: 'PUBLISHED', mandatoryReConsent: true },
    { id: 'TERM-02', code: 'TERMS_OF_SERVICE_CORP', title: 'Thỏa Thuận Sử Dụng Nền Tảng Fintech Doanh Nghiệp', targetUser: 'CORPORATE', version: 'v2.1.0', effectiveDate: '2026-08-15', status: 'PUBLISHED', mandatoryReConsent: false },
    { id: 'TERM-03', code: 'PRIVACY_POLICY_AML', title: 'Chính Sách Bảo Mật Dữ Liệu & Phòng Chống Rửa Tiền', targetUser: 'ALL', version: 'v4.0.0', effectiveDate: '2026-09-01', status: 'PUBLISHED', mandatoryReConsent: true },
  ]);

  const [consents] = useState<ConsentRecord[]>([
    { id: 'CST-9001', userId: 'USR-8821', userName: 'Nguyen Van An', userType: 'RETAIL', termVersion: 'TERMS_RETAIL_v3.2.0', acceptedAt: '2026-09-10 16:42:10', ipAddress: '14.162.180.25', deviceFingerprint: 'FP_iOS_17_4_iPhone15Pro', signatureSha256: '9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08' },
    { id: 'CST-9002', userId: 'CORP-ADM-01', userName: 'Vingroup Digital Corp', userType: 'CORP', termVersion: 'TERMS_CORP_v2.1.0', acceptedAt: '2026-09-10 15:18:22', ipAddress: '113.190.23.88', deviceFingerprint: 'FP_MacOS_Chrome_128', signatureSha256: '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8' },
    { id: 'CST-9003', userId: 'USR-8845', userName: 'Tran Thi Bich', userType: 'RETAIL', termVersion: 'TERMS_RETAIL_v3.2.0', acceptedAt: '2026-09-10 14:05:30', ipAddress: '27.72.100.12', deviceFingerprint: 'FP_Android_14_SamsungS24', signatureSha256: '4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a' },
  ]);

  const [wormLogs] = useState<ImmutableWormLog[]>([
    { blockIndex: 10452, timestamp: '2026-09-10 16:55:00', actor: 'superadmin_root', action: 'MODIFY_GLOBAL_FIN_LIMIT', targetResource: 'SA_FIN_LIMITS', details: 'Nâng trần giao dịch doanh nghiệp VIP lên 50,000,000,000 VND', blockHash: '0000a89f78bc421de091...', prevBlockHash: '00003b12ef998811ca45...', wormVerified: true },
    { blockIndex: 10451, timestamp: '2026-09-10 15:30:12', actor: 'superadmin_sec', action: 'ARM_CIRCUIT_BREAKER', targetResource: 'SA_SEC_CIRCUIT_BREAKER', details: 'Thực thi kiểm tra ngắt mạch sandbox kết nối NAPAS test', blockHash: '00003b12ef998811ca45...', prevBlockHash: '000099eeff7711223344...', wormVerified: true },
    { blockIndex: 10450, timestamp: '2026-09-10 14:10:00', actor: 'compliance_officer', action: 'ADD_GLOBAL_BLACKLIST', targetResource: 'SA_SEC_GLOBAL_BLACKLIST', details: 'Khóa vĩnh viễn IP 185.220.101.45 (Tor Exit Node brute-force)', blockHash: '000099eeff7711223344...', prevBlockHash: '00001122334455667788...', wormVerified: true },
  ]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-900/60 p-6 rounded-2xl border border-slate-800 backdrop-blur-xl">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400">
            <FileCheck className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-white tracking-wide">Pháp Lý, Kiểm Toán & Tuân Thủ (Audit & Compliance)</h1>
            <p className="text-xs text-slate-400">Lưu vết bằng chứng pháp lý bất biến (WORM), quản trị phiên bản điều khoản và chính sách lưu trữ Cold Storage</p>
          </div>
        </div>
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-semibold">
          <Shield className="w-3.5 h-3.5" />
          WORM Integrity: 100% VERIFIED
        </div>
      </div>

      <div className="flex items-center gap-2 border-b border-slate-800 pb-2 overflow-x-auto">
        {[
          { key: 'WORM', label: 'Nhật Ký Kiểm Toán Bất Biến (WORM)', icon: History, code: 'SA_AUDIT_IMMUTABLE_LOGS' },
          { key: 'CONSENT', label: 'Bằng Chứng Pháp Lý Khách Hàng', icon: CheckCircle2, code: 'SA_AUDIT_CONSENT_TRAIL' },
          { key: 'TERMS', label: 'Văn Bản Pháp Lý & Điều Khoản', icon: FileText, code: 'SA_AUDIT_TERMS' },
          { key: 'RETENTION', label: 'Chính Sách Lưu Trữ Cold Storage', icon: Archive, code: 'SA_AUDIT_RETENTION_ARCHIVE' },
        ].map(t => {
          const Icon = t.icon;
          const isActive = activeTab === t.key;
          return (
            <button
              key={t.key}
              onClick={() => setActiveTab(t.key as any)}
              className={'flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-semibold transition whitespace-nowrap border ' + (
                isActive ? 'bg-emerald-500/10 border-emerald-500/40 text-emerald-400 shadow-sm' : 'bg-slate-900/40 border-slate-800/80 text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              )}
            >
              <Icon className="w-4 h-4" />
              <span>{t.label}</span>
              <span className="text-[10px] font-mono opacity-60 ml-1">[{t.code}]</span>
            </button>
          );
        })}
      </div>

      {/* TAB 1: IMMUTABLE WORM LOGS */}
      {activeTab === 'WORM' && (
        <div className="space-y-4">
          <div className="p-4 rounded-xl bg-slate-950 border border-slate-800 flex items-center justify-between text-xs">
            <div className="flex items-center gap-3">
              <Hash className="w-5 h-5 text-emerald-400 shrink-0" />
              <div>
                <span className="font-bold text-white">Cơ Chế Khóa Bất Biến WORM (Write-Once-Read-Many):</span>
                <p className="text-slate-400 text-[11px] mt-0.5">Mỗi hành động của SuperAdmin tạo thành 1 block mã hóa SHA-256 nối tiếp chuỗi trước đó. Không một tài khoản nào có thể xóa hoặc sửa nhật ký.</p>
              </div>
            </div>
            <button onClick={() => alert('Xác thực toàn bộ 10,452 blocks WORM... KẾT QUẢ: 100% HỢP LỆ') } className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white font-bold rounded-lg transition shrink-0">Kiểm Tra Chuỗi Hash</button>
          </div>

          <div className="bg-slate-900/60 rounded-2xl border border-slate-800 overflow-hidden">
            <table className="w-full text-left text-xs text-slate-300">
              <thead className="bg-slate-950/80 text-slate-400 uppercase text-[10px] tracking-wider border-b border-slate-800">
                <tr>
                  <th className="py-3.5 px-4">Block #</th>
                  <th className="py-3.5 px-4">Thời Gian</th>
                  <th className="py-3.5 px-4">Người Thực Hiện</th>
                  <th className="py-3.5 px-4">Hành Động & Module</th>
                  <th className="py-3.5 px-4">Chi Tiết Thay Đổi</th>
                  <th className="py-3.5 px-4">Block Hash (SHA-256)</th>
                  <th className="py-3.5 px-4">Trạng Thái WORM</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono">
                {wormLogs.map(l => (
                  <tr key={l.blockIndex} className="hover:bg-slate-800/30 transition">
                    <td className="py-3 px-4 font-bold text-emerald-400">#{l.blockIndex}</td>
                    <td className="py-3 px-4 text-slate-400 text-[11px]">{l.timestamp}</td>
                    <td className="py-3 px-4 font-bold text-white font-sans">{l.actor}</td>
                    <td className="py-3 px-4"><span className="px-2 py-0.5 rounded text-[10px] bg-slate-800 text-amber-300 border border-slate-700">{l.action}</span></td>
                    <td className="py-3 px-4 text-slate-300 font-sans text-xs">{l.details}</td>
                    <td className="py-3 px-4 text-[10px] text-slate-400">{l.blockHash}</td>
                    <td className="py-3 px-4"><span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-sans">VERIFIED</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 2: CUSTOMER CONSENT TRAIL */}
      {activeTab === 'CONSENT' && (
        <div className="space-y-4">
          <div className="flex justify-between items-center bg-slate-900/60 p-4 rounded-xl border border-slate-800">
            <span className="text-xs text-slate-400">Lưu vết 100% bằng chứng người dùng đồng thuận điều khoản, phục vụ thanh tra NHNN và kiểm toán độc lập.</span>
            <button onClick={() => alert('Xuất danh sách chữ ký bằng chứng pháp lý sang CSV')} className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs rounded-lg transition flex items-center gap-1.5"><Download className="w-3.5 h-3.5" /> Xuất Báo Cáo Kiểm Toán</button>
          </div>

          <div className="bg-slate-900/60 rounded-2xl border border-slate-800 overflow-hidden">
            <table className="w-full text-left text-xs text-slate-300">
              <thead className="bg-slate-950/80 text-slate-400 uppercase text-[10px] tracking-wider border-b border-slate-800">
                <tr>
                  <th className="py-3.5 px-4">Mã Consent</th>
                  <th className="py-3.5 px-4">Khách Hàng</th>
                  <th className="py-3.5 px-4">Phiên Bản Điều Khoản</th>
                  <th className="py-3.5 px-4">Thời Gian Chấp Thuận</th>
                  <th className="py-3.5 px-4">Địa Chỉ IP</th>
                  <th className="py-3.5 px-4">Device Fingerprint</th>
                  <th className="py-3.5 px-4">Chữ Ký Toàn Vẹn</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono">
                {consents.map(c => (
                  <tr key={c.id} className="hover:bg-slate-800/30 transition">
                    <td className="py-3 px-4 font-bold text-slate-400">{c.id}</td>
                    <td className="py-3 px-4 font-sans font-semibold text-white">{c.userName} <span className="text-[10px] font-mono text-slate-500">({c.userId})</span></td>
                    <td className="py-3 px-4"><span className="px-2 py-0.5 rounded text-[10px] bg-slate-800 text-emerald-400 border border-slate-700">{c.termVersion}</span></td>
                    <td className="py-3 px-4 text-slate-400">{c.acceptedAt}</td>
                    <td className="py-3 px-4 text-slate-300">{c.ipAddress}</td>
                    <td className="py-3 px-4 text-[11px] text-slate-400 truncate max-w-[150px]" title={c.deviceFingerprint}>{c.deviceFingerprint}</td>
                    <td className="py-3 px-4 text-[10px] text-amber-300 truncate max-w-[150px]" title={c.signatureSha256}>{c.signatureSha256}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 3: LEGAL TERMS & POLICY VERSIONS */}
      {activeTab === 'TERMS' && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          {terms.map(t => (
            <div key={t.id} className="bg-slate-900/60 rounded-2xl border border-slate-800 p-5 space-y-3">
              <div className="flex justify-between items-start">
                <span className="text-[10px] font-mono text-emerald-400 bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">{t.targetUser}</span>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">{t.status}</span>
              </div>
              <h3 className="text-sm font-bold text-white">{t.title}</h3>
              <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 text-xs space-y-1.5 font-mono">
                <div className="flex justify-between text-slate-400"><span>Mã tệp:</span><span className="text-slate-200">{t.code}</span></div>
                <div className="flex justify-between text-slate-400"><span>Phiên bản:</span><span className="text-amber-400 font-bold">{t.version}</span></div>
                <div className="flex justify-between text-slate-400"><span>Hiệu lực từ:</span><span className="text-slate-200">{t.effectiveDate}</span></div>
                <div className="flex justify-between text-slate-400"><span>Bắt buộc ký lại:</span><span className={t.mandatoryReConsent ? 'text-rose-400 font-bold' : 'text-slate-500'}>{t.mandatoryReConsent ? 'YES (BẮT BUỘC)' : 'NO'}</span></div>
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button onClick={() => alert('Mở trình soạn thảo Markdown cho ' + t.title)} className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold rounded-lg transition">Chỉnh Sửa Bản Soạn Thảo</button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* TAB 4: DATA RETENTION */}
      {activeTab === 'RETENTION' && (
        <div className="bg-slate-900/60 p-6 rounded-2xl border border-slate-800 space-y-6">
          <h3 className="text-sm font-bold text-white flex items-center gap-2"><Archive className="w-4 h-4 text-emerald-400" /> Chính Sách Lưu Trữ Dữ Liệu & Cold Storage Archiving</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
            {[
              { title: 'Nhật Ký Giao Dịch Tài Chính (Ledger)', period: '10 Năm (Theo Luật Kế Toán VN)', storage: 'PostgreSQL Hot DB (2 năm) -> S3 Glacier Deep Archive (8 năm)' },
              { title: 'Dữ Liệu KYC & Hồ Sơ Khách Hàng', period: 'Trọn Đời Tài Khoản + 5 Năm Sau Đóng', storage: 'MinIO Encrypted Bucket AES-256' },
              { title: 'Nhật Ký Truy Cập & Audit Logs (WORM)', period: '5 Năm', storage: 'Elasticsearch Cold Node + Immutable S3 WORM' },
              { title: 'Gói Tin Viễn Trắc IoT Telemetry Raw', period: '90 Ngày (Sau đó nén Roll-up theo giờ)', storage: 'TimescaleDB / InfluxDB Auto-Retention Dropper' },
            ].map((p, idx) => (
              <div key={idx} className="p-4 rounded-xl bg-slate-950 border border-slate-800 space-y-2">
                <h4 className="font-bold text-white text-xs">{p.title}</h4>
                <div className="flex justify-between text-slate-400"><span>Thời gian lưu trữ:</span><span className="text-emerald-400 font-bold font-mono">{p.period}</span></div>
                <div className="text-slate-500 text-[11px]">Hạ tầng: {p.storage}</div>
              </div>
            ))}
          </div>
          <div className="flex justify-end">
            <button onClick={() => alert('Đã kích hoạt chạy cron lưu trữ Cold Storage thủ công!')} className="px-5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold rounded-xl transition shadow-lg">Chạy Cron Nén Cold Storage Ngay</button>
          </div>
        </div>
      )}
    </div>
  );
};
export default AuditCompliancePage;