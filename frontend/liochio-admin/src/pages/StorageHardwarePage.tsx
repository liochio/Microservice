import React, { useState } from 'react';
import {
  HardDrive, Layers, FileCode, Radio, Plus, CheckCircle2, AlertCircle,
  RefreshCw, Upload, Download, Sliders, ShieldCheck, Database, Cpu, Wifi,
} from 'lucide-react';

interface S3Bucket {
  name: string;
  provider: 'MinIO Private Cluster' | 'AWS S3 ap-southeast-1';
  purpose: string;
  objectCount: number;
  totalSizeGb: number;
  preSignedTtlMin: number;
  corsEnabled: boolean;
}

interface FirmwareRelease {
  version: string;
  hardwareTarget: string;
  releaseDate: string;
  sha256: string;
  fileSizeMb: number;
  rolloutPercent: number;
  status: 'STABLE' | 'CANARY' | 'DEPRECATED';
}

export const StorageHardwarePage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'BLOB' | 'FIRMWARE' | 'TELEMETRY'>('FIRMWARE');

  const [buckets] = useState<S3Bucket[]>([
    { name: 'liochio-kyc-documents', provider: 'MinIO Private Cluster', purpose: 'Lưu ảnh CCCD Chip, chân dung selfie mã hóa AES-256', objectCount: 45200, totalSizeGb: 124.5, preSignedTtlMin: 15, corsEnabled: true },
    { name: 'liochio-firmware-binaries', provider: 'AWS S3 ap-southeast-1', purpose: 'Lưu tệp nhị phân .bin OTA cho heo đất IoT', objectCount: 28, totalSizeGb: 1.2, preSignedTtlMin: 60, corsEnabled: true },
    { name: 'liochio-e-invoices-xml', provider: 'MinIO Private Cluster', purpose: 'Lưu trữ tệp XML ký số hóa đơn điện tử', objectCount: 18900, totalSizeGb: 45.8, preSignedTtlMin: 30, corsEnabled: false },
    { name: 'liochio-system-backups', provider: 'AWS S3 ap-southeast-1', purpose: 'Cold storage snapshots PostgreSQL & MongoDB', objectCount: 365, totalSizeGb: 890.0, preSignedTtlMin: 120, corsEnabled: false },
  ]);

  const [firmwares, setFirmwares] = useState<FirmwareRelease[]>([
    { version: 'v2.4.1-rc3', hardwareTarget: 'ESP32-S3-PIGGY-V2', releaseDate: '2026-09-08', sha256: '9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08', fileSizeMb: 2.45, rolloutPercent: 20, status: 'CANARY' },
    { version: 'v2.4.0', hardwareTarget: 'ESP32-S3-PIGGY-V2', releaseDate: '2026-08-15', sha256: '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', fileSizeMb: 2.42, rolloutPercent: 100, status: 'STABLE' },
    { version: 'v2.3.5', hardwareTarget: 'ESP32-WROOM-32D-V1', releaseDate: '2026-06-01', sha256: '4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a', fileSizeMb: 1.88, rolloutPercent: 100, status: 'STABLE' },
    { version: 'v2.2.0', hardwareTarget: 'ESP32-WROOM-32D-V1', releaseDate: '2026-02-10', sha256: 'ef2d127de37b942baad06145e54b0c619a1f22327b2ebbcfbec78f5564afe39d', fileSizeMb: 1.75, rolloutPercent: 0, status: 'DEPRECATED' },
  ]);

  const [telemetryConfig, setTelemetryConfig] = useState({
    heartbeatIntervalSec: 30,
    impactThresholdG: 6.0,
    tempAlertCelsius: 65,
    lowBatteryPercent: 15,
    offlineTimeoutSec: 120,
    mqttKeepAliveSec: 60,
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-900/60 p-6 rounded-2xl border border-slate-800 backdrop-blur-xl">
        <div className="flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-cyan-500/10 border border-cyan-500/20 text-cyan-400">
            <HardDrive className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-white tracking-wide">Kho Phần Cứng & Firmware OTA</h1>
            <p className="text-xs text-slate-400">Quản lý kho nhị phân S3/MinIO, tệp firmware .bin ESP32 và tham số viễn trắc thiết bị IoT Heo Đất</p>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <button onClick={() => alert('Mở bảng tải lên Firmware .bin mới')} className="flex items-center gap-2 px-3.5 py-2 bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-semibold rounded-xl transition shadow-lg shadow-cyan-900/30">
            <Upload className="w-3.5 h-3.5" /> Upload .BIN Firmware
          </button>
        </div>
      </div>

      <div className="flex items-center gap-2 border-b border-slate-800 pb-2 overflow-x-auto">
        {[
          { key: 'FIRMWARE', label: 'Quản Lý Firmware OTA (.bin)', icon: FileCode, code: 'SA_STG_FIRMWARE_OTA' },
          { key: 'BLOB', label: 'Kho Lưu Trữ Tệp S3 / MinIO', icon: Layers, code: 'SA_STG_BINARY_BLOB' },
          { key: 'TELEMETRY', label: 'Cấu Hình Viễn Trắc IoT (MPU6050)', icon: Radio, code: 'SA_STG_IOT_TELEMETRY' },
        ].map(t => {
          const Icon = t.icon;
          const isActive = activeTab === t.key;
          return (
            <button
              key={t.key}
              onClick={() => setActiveTab(t.key as any)}
              className={'flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-semibold transition whitespace-nowrap border ' + (
                isActive ? 'bg-cyan-500/10 border-cyan-500/40 text-cyan-400 shadow-sm' : 'bg-slate-900/40 border-slate-800/80 text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              )}
            >
              <Icon className="w-4 h-4" />
              <span>{t.label}</span>
              <span className="text-[10px] font-mono opacity-60 ml-1">[{t.code}]</span>
            </button>
          );
        })}
      </div>

      {/* TAB 1: FIRMWARE OTA */}
      {activeTab === 'FIRMWARE' && (
        <div className="space-y-5">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800"><div className="text-slate-400 text-xs">Tổng Phiên Bản OTA</div><div className="text-xl font-bold text-white font-mono mt-1">{firmwares.length} Bản</div></div>
            <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800"><div className="text-slate-400 text-xs">Bản Đang Phổ Cập 100%</div><div className="text-xl font-bold text-emerald-400 font-mono mt-1">v2.4.0</div></div>
            <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800"><div className="text-slate-400 text-xs">Canary Staging</div><div className="text-xl font-bold text-amber-400 font-mono mt-1">v2.4.1-rc3 (20%)</div></div>
            <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800"><div className="text-slate-400 text-xs">ESP32 Đang Online</div><div className="text-xl font-bold text-cyan-400 font-mono mt-1">1,842 Thiết bị</div></div>
          </div>

          <div className="bg-slate-900/60 rounded-2xl border border-slate-800 overflow-hidden">
            <table className="w-full text-left text-xs text-slate-300">
              <thead className="bg-slate-950/80 text-slate-400 uppercase text-[10px] tracking-wider border-b border-slate-800">
                <tr>
                  <th className="py-3.5 px-4">Phiên Bản</th>
                  <th className="py-3.5 px-4">Phần Cứng Mục Tiêu</th>
                  <th className="py-3.5 px-4">SHA-256 Checksum Toàn Vẹn</th>
                  <th className="py-3.5 px-4">Kích Thước</th>
                  <th className="py-3.5 px-4">Tiến Độ Rollout</th>
                  <th className="py-3.5 px-4">Trạng Thái</th>
                  <th className="py-3.5 px-4 text-right">Thao Tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono">
                {firmwares.map(f => (
                  <tr key={f.version} className="hover:bg-slate-800/30 transition">
                    <td className="py-3.5 px-4 font-bold text-white">{f.version}</td>
                    <td className="py-3.5 px-4 text-slate-400">{f.hardwareTarget}</td>
                    <td className="py-3.5 px-4 text-[10px] text-amber-300/80 truncate max-w-[200px]" title={f.sha256}>{f.sha256}</td>
                    <td className="py-3.5 px-4 text-slate-300">{f.fileSizeMb} MB</td>
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-2">
                        <div className="w-16 bg-slate-800 h-1.5 rounded-full overflow-hidden">
                          <div className="bg-cyan-500 h-full" style={{ width: f.rolloutPercent + '%' }} />
                        </div>
                        <span className="text-[10px]">{f.rolloutPercent}%</span>
                      </div>
                    </td>
                    <td className="py-3.5 px-4">
                      <span className={'px-2 py-0.5 rounded-full text-[10px] font-bold border ' + (
                        f.status === 'STABLE' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                        f.status === 'CANARY' ? 'bg-amber-500/10 text-amber-400 border-amber-500/20' : 'bg-slate-800 text-slate-500 border-slate-700'
                      )}>
                        {f.status}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-right font-sans">
                      <button onClick={() => alert('Điều chỉnh tỷ lệ Rollout OTA cho ' + f.version)} className="px-2.5 py-1 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs rounded-lg transition mr-2">Rollout</button>
                      <button onClick={() => alert('Tải tệp .bin: ' + f.version)} className="p-1 text-slate-400 hover:text-cyan-400"><Download className="w-3.5 h-3.5 inline" /></button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 2: BINARY BLOB S3/MINIO */}
      {activeTab === 'BLOB' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {buckets.map(b => (
            <div key={b.name} className="bg-slate-900/60 rounded-2xl border border-slate-800 p-5 space-y-3">
              <div className="flex items-start justify-between">
                <div>
                  <span className="text-[10px] font-mono text-cyan-400 bg-cyan-500/10 px-2 py-0.5 rounded border border-cyan-500/20">{b.provider}</span>
                  <h3 className="text-sm font-bold text-white font-mono mt-1.5">{b.name}</h3>
                </div>
                <span className="text-xs font-bold text-cyan-400">{b.totalSizeGb} GB</span>
              </div>
              <p className="text-xs text-slate-400">{b.purpose}</p>
              <div className="p-3 bg-slate-950 rounded-xl border border-slate-800 text-xs space-y-1.5 font-mono">
                <div className="flex justify-between text-slate-400"><span>Số lượng đối tượng:</span><span className="text-white font-bold">{b.objectCount.toLocaleString()} files</span></div>
                <div className="flex justify-between text-slate-400"><span>Pre-Signed URL Expiry:</span><span className="text-amber-400">{b.preSignedTtlMin} Phút</span></div>
                <div className="flex justify-between text-slate-400"><span>CORS Access Header:</span><span className={b.corsEnabled ? 'text-emerald-400' : 'text-slate-500'}>{b.corsEnabled ? 'ENABLED (*)' : 'STRICT INTERNAL'}</span></div>
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button onClick={() => alert('Kiểm tra tính toàn vẹn Bucket ' + b.name)} className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs rounded-lg transition">Kiểm Thử S3</button>
              </div>
            </div>
          ))} 
        </div>
      )}

      {/* TAB 3: TELEMETRY BASELINE CONFIG */}
      {activeTab === 'TELEMETRY' && (
        <div className="bg-slate-900/60 p-6 rounded-2xl border border-slate-800 space-y-6">
          <h3 className="text-sm font-bold text-white flex items-center gap-2"><Radio className="w-4 h-4 text-cyan-400" /> Cấu Hình Ngưỡng Viễn Trắc Cảm Biến Heo Đất IoT (MPU6050 & ESP32)</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-xs">
            <div>
              <label className="text-slate-400 block mb-1 font-semibold">Nhịp Tim Heartbeat Định Kỳ (Giây)</label>
              <input type="number" value={telemetryConfig.heartbeatIntervalSec} onChange={e => setTelemetryConfig({...telemetryConfig, heartbeatIntervalSec: Number(e.target.value)})} className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-white font-mono" />
              <p className="text-[11px] text-slate-500 mt-1">Chu kỳ ESP32 gửi gói tin MQTT nhịp tim về Broker.</p>
            </div>
            <div>
              <label className="text-slate-400 block mb-1 font-semibold">Ngưỡng Va Đập Ngắt Mạch MPU6050 (G-Force)</label>
              <input type="number" step="0.1" value={telemetryConfig.impactThresholdG} onChange={e => setTelemetryConfig({...telemetryConfig, impactThresholdG: Number(e.target.value)})} className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-rose-400 font-mono font-bold" />
              <p className="text-[11px] text-slate-500 mt-1">Khi gia tốc &gt; 6.0G, kích hoạt tự động khóa ví chống đập phá heo.</p>
            </div>
            <div>
              <label className="text-slate-400 block mb-1 font-semibold">Ngưỡng Quá Nhiệt (°C)</label>
              <input type="number" value={telemetryConfig.tempAlertCelsius} onChange={e => setTelemetryConfig({...telemetryConfig, tempAlertCelsius: Number(e.target.value)})} className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-xl text-amber-400 font-mono font-bold" />
              <p className="text-[11px] text-slate-500 mt-1">Cảnh báo pin phồng hoặc môi trường nhiệt độ nguy hiểm.</p>
            </div>
          </div>
          <div className="flex justify-end pt-2">
            <button onClick={() => alert('Đã cập nhật bộ tham số IoT Telemetry tới toàn bộ thiết bị!')} className="px-6 py-2 bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-bold rounded-xl transition shadow-lg shadow-cyan-900/30">Lưu Tham Số IoT Sàn</button>
          </div>
        </div>
      )}
    </div>
  );
};
export default StorageHardwarePage;