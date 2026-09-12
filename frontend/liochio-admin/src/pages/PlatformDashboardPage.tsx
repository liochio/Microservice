import React, { useState } from 'react';
import { 
  Activity, 
  BarChart3, 
  HeartPulse, 
  DollarSign, 
  Server, 
  Cpu, 
  Database, 
  ArrowUpRight, 
  ArrowDownRight, 
  CheckCircle2, 
  AlertTriangle,
  Zap,
  Globe2,
  Layers,
  ShieldCheck,
  Building2
} from 'lucide-react';
import { Card, Badge } from '../components/common/CardAndBadge';

export const PlatformDashboardPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'metrics' | 'health' | 'revenue'>('metrics');

  const microservices = [
    { name: 'service-registry (Eureka)', port: 8761, status: 'UP', rps: 420, latency: '2.1ms', cpu: '12%', mem: '340MB' },
    { name: 'api-gateway (Netty Reactive)', port: 8080, status: 'UP', rps: 3450, latency: '4.8ms', cpu: '28%', mem: '512MB' },
    { name: 'auth-service (IAM & RBAC)', port: 8081, status: 'UP', rps: 890, latency: '5.2ms', cpu: '18%', mem: '480MB' },
    { name: 'otp-service (SMS Engine)', port: 8094, status: 'UP', rps: 120, latency: '8.4ms', cpu: '8%', mem: '290MB' },
    { name: 'worker-service (Async CDC)', port: 8095, status: 'UP', rps: 640, latency: '3.1ms', cpu: '14%', mem: '380MB' },
    { name: 'python-fintech-core (FastAPI)', port: 8089, status: 'UP', rps: 2100, latency: '6.5ms', cpu: '32%', mem: '420MB' },
    { name: 'entity-service', port: 8082, status: 'UP', rps: 350, latency: '4.2ms', cpu: '10%', mem: '310MB' },
    { name: 'media-service (S3/MinIO)', port: 8083, status: 'UP', rps: 180, latency: '12.0ms', cpu: '15%', mem: '360MB' },
    { name: 'notification-service (Mail/WS)', port: 8084, status: 'UP', rps: 520, latency: '3.9ms', cpu: '11%', mem: '320MB' },
    { name: 'payment-service (VNPay/NAPAS)', port: 8085, status: 'UP', rps: 940, latency: '9.1ms', cpu: '22%', mem: '440MB' },
  ];

  return (
    <div className="space-y-6 animate-fade-in max-w-7xl">
      {/* Top Banner */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-slate-900/80 border border-slate-800 p-6 rounded-3xl backdrop-blur-xl">
        <div>
          <div className="flex items-center gap-2 font-mono text-xs text-amber-400 font-bold mb-1">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
            <span>SA_DASHBOARD &bull; PLATFORM COMMAND CENTER</span>
          </div>
          <h1 className="text-2xl font-black text-white tracking-tight">
            Bảng Điều Khiển Tổng Quan Nền Tảng Sàn
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Giám sát thời gian thực lưu lượng mạng, sức khỏe 17 vi dịch vụ, cơ sở dữ liệu và tốc độ tăng trưởng B2B Tenants.
          </p>
        </div>

        {/* Tab Switcher */}
        <div className="flex items-center bg-slate-950 p-1 rounded-2xl border border-slate-800 text-xs font-bold shrink-0">
          <button
            onClick={() => setActiveTab('metrics')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl transition cursor-pointer ${
              activeTab === 'metrics'
                ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30 shadow-md'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <BarChart3 className="w-3.5 h-3.5" />
            <span>Lưu Lượng & Hiệu Năng</span>
          </button>

          <button
            onClick={() => setActiveTab('health')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl transition cursor-pointer ${
              activeTab === 'health'
                ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 shadow-md'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <HeartPulse className="w-3.5 h-3.5" />
            <span>17 Microservices & DB</span>
          </button>

          <button
            onClick={() => setActiveTab('revenue')}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl transition cursor-pointer ${
              activeTab === 'revenue'
                ? 'bg-indigo-500/20 text-indigo-300 border border-indigo-500/30 shadow-md'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <DollarSign className="w-3.5 h-3.5" />
            <span>Doanh Thu & Tenant B2B</span>
          </button>
        </div>
      </div>

      {/* Overview Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="bg-slate-900/60">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold">
            <span>Tổng Lưu Lượng (RPS)</span>
            <div className="p-2 rounded-xl bg-amber-500/10 text-amber-400">
              <Activity className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-black text-white mt-2">8,730 RPS</div>
          <div className="text-[11px] text-emerald-400 font-medium flex items-center gap-1 mt-1">
            <ArrowUpRight className="w-3.5 h-3.5" />
            <span>+14.2% so với giờ trước</span>
          </div>
        </Card>

        <Card className="bg-slate-900/60">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold">
            <span>Độ Trễ P99 API Gateway</span>
            <div className="p-2 rounded-xl bg-emerald-500/10 text-emerald-400">
              <Zap className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-black text-white mt-2">5.4 ms</div>
          <div className="text-[11px] text-emerald-400 font-medium flex items-center gap-1 mt-1">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>SLA 99.99% Hoàn Hảo</span>
          </div>
        </Card>

        <Card className="bg-slate-900/60">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold">
            <span>Doanh Nghiệp (Tenants)</span>
            <div className="p-2 rounded-xl bg-indigo-500/10 text-indigo-400">
              <Building2 className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-black text-white mt-2">14 Đối Tác</div>
          <div className="text-[11px] text-indigo-400 font-medium flex items-center gap-1 mt-1">
            <span>12 Hoạt Động &bull; 2 Chờ Duyệt</span>
          </div>
        </Card>

        <Card className="bg-slate-900/60">
          <div className="flex items-center justify-between text-slate-400 text-xs font-semibold">
            <span>Doanh Thu Tháng Này</span>
            <div className="p-2 rounded-xl bg-rose-500/10 text-rose-400">
              <DollarSign className="w-4 h-4" />
            </div>
          </div>
          <div className="text-2xl font-black text-white mt-2">4.850.000.000 đ</div>
          <div className="text-[11px] text-emerald-400 font-medium flex items-center gap-1 mt-1">
            <ArrowUpRight className="w-3.5 h-3.5" />
            <span>+28.5% Tăng trưởng MRR</span>
          </div>
        </Card>
      </div>

      {/* TAB 1: METRICS & TRAFFIC */}
      {activeTab === 'metrics' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <Card className="lg:col-span-2 space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <h2 className="text-sm font-bold text-white flex items-center gap-2">
                <BarChart3 className="w-4 h-4 text-amber-400" />
                <span>Phân Bổ Lưu Lượng Request Theo Giây (RPS Time Series)</span>
              </h2>
              <span className="text-[10px] font-mono text-slate-400">Cập nhật mỗi 1s</span>
            </div>

            <div className="h-60 flex items-end justify-between gap-1.5 pt-4 px-2 bg-slate-950/60 rounded-2xl border border-slate-800/80">
              {[42, 65, 80, 55, 90, 110, 95, 120, 135, 105, 140, 160, 150, 180, 200, 175, 190, 220, 210, 240].map((val, idx) => (
                <div key={idx} className="flex-1 flex flex-col items-center gap-1 group">
                  <div
                    className="w-full bg-gradient-to-t from-amber-500 to-rose-500 rounded-t-lg transition-all group-hover:brightness-125"
                    style={{ height: `${(val / 240) * 100}%` }}
                  />
                  <span className="text-[8px] font-mono text-slate-500 hidden sm:block">{idx}s</span>
                </div>
              ))}
            </div>

            <div className="grid grid-cols-3 gap-3 pt-2 text-xs">
              <div className="p-3 bg-slate-950/50 rounded-xl border border-slate-800">
                <span className="text-slate-400 block text-[10px]">P50 Latency</span>
                <span className="font-mono font-bold text-slate-200 mt-0.5 block">2.3 ms</span>
              </div>
              <div className="p-3 bg-slate-950/50 rounded-xl border border-slate-800">
                <span className="text-slate-400 block text-[10px]">P95 Latency</span>
                <span className="font-mono font-bold text-amber-300 mt-0.5 block">4.8 ms</span>
              </div>
              <div className="p-3 bg-slate-950/50 rounded-xl border border-slate-800">
                <span className="text-slate-400 block text-[10px]">P99 Latency</span>
                <span className="font-mono font-bold text-emerald-400 mt-0.5 block">6.2 ms</span>
              </div>
            </div>
          </Card>

          <Card className="space-y-4">
            <h2 className="text-sm font-bold text-white flex items-center gap-2 pb-3 border-b border-slate-800">
              <Server className="w-4 h-4 text-indigo-400" />
              <span>Hạ Tầng Cluster Resource</span>
            </h2>

            <div className="space-y-3 text-xs">
              <div>
                <div className="flex justify-between text-slate-300 mb-1">
                  <span>CPU Node 1 (Control Plane)</span>
                  <span className="font-mono font-bold text-amber-400">38%</span>
                </div>
                <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden border border-slate-800">
                  <div className="bg-amber-500 h-full rounded-full" style={{ width: '38%' }} />
                </div>
              </div>

              <div>
                <div className="flex justify-between text-slate-300 mb-1">
                  <span>RAM Memory Cluster</span>
                  <span className="font-mono font-bold text-emerald-400">42% (6.7/16 GB)</span>
                </div>
                <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden border border-slate-800">
                  <div className="bg-emerald-500 h-full rounded-full" style={{ width: '42%' }} />
                </div>
              </div>

              <div>
                <div className="flex justify-between text-slate-300 mb-1">
                  <span>Redis Cluster RAM (O(1) Cache)</span>
                  <span className="font-mono font-bold text-cyan-400">18% (1.4/8 GB)</span>
                </div>
                <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden border border-slate-800">
                  <div className="bg-cyan-500 h-full rounded-full" style={{ width: '18%' }} />
                </div>
              </div>

              <div>
                <div className="flex justify-between text-slate-300 mb-1">
                  <span>MySQL IOPS & Storage Pool</span>
                  <span className="font-mono font-bold text-indigo-400">24% (120/500 GB)</span>
                </div>
                <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden border border-slate-800">
                  <div className="bg-indigo-500 h-full rounded-full" style={{ width: '24%' }} />
                </div>
              </div>
            </div>
          </Card>
        </div>
      )}

      {/* TAB 2: HEALTH 17 MICROSERVICES */}
      {activeTab === 'health' && (
        <Card className="space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-800">
            <div>
              <h2 className="text-sm font-bold text-white flex items-center gap-2">
                <HeartPulse className="w-4 h-4 text-emerald-400" />
                <span>Trạng Thái Hoạt Động Cụm Vi Dịch Vụ Microservices</span>
              </h2>
              <p className="text-xs text-slate-400 mt-0.5 font-mono">
                Mã định danh: SA_DASH_HEALTH &bull; Đăng ký qua Eureka :8761
              </p>
            </div>
            <Badge variant="success">10/10 Core Nodes Healthy</Badge>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-300">
              <thead className="bg-slate-950/80 text-slate-400 uppercase tracking-wider text-[10px] font-bold border-b border-slate-800 font-mono">
                <tr>
                  <th className="py-3 px-4">Service Name</th>
                  <th className="py-3 px-4">Internal Port</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Throughput (RPS)</th>
                  <th className="py-3 px-4">Avg Latency</th>
                  <th className="py-3 px-4">CPU</th>
                  <th className="py-3 px-4">Memory</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono">
                {microservices.map(s => (
                  <tr key={s.name} className="hover:bg-slate-800/30 transition">
                    <td className="py-3.5 px-4 font-bold text-white font-sans flex items-center gap-2">
                      <span className="w-2 h-2 rounded-full bg-emerald-400" />
                      <span>{s.name}</span>
                    </td>
                    <td className="py-3.5 px-4 text-amber-400">:{s.port}</td>
                    <td className="py-3.5 px-4">
                      <span className="px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 text-[10px] font-bold">
                        {s.status}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-slate-200">{s.rps} rps</td>
                    <td className="py-3.5 px-4 text-cyan-400">{s.latency}</td>
                    <td className="py-3.5 px-4 text-slate-300">{s.cpu}</td>
                    <td className="py-3.5 px-4 text-slate-300">{s.mem}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      {/* TAB 3: REVENUE & TENANTS */}
      {activeTab === 'revenue' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <Card className="lg:col-span-2 space-y-4">
            <h2 className="text-sm font-bold text-white flex items-center gap-2 pb-3 border-b border-slate-800">
              <DollarSign className="w-4 h-4 text-emerald-400" />
              <span>Biểu Đồ Phân Tích Doanh Thu Toàn Sàn Theo Tháng (VNĐ)</span>
            </h2>
            <div className="space-y-3">
              {[
                { label: 'Doanh Thu Gói Cước B2B SaaS (Subscription MRR)', amount: '2.450.000.000 đ', percent: 50.5, color: 'bg-indigo-500' },
                { label: 'Phí Giao Dịch & Hoa Hồng Luân Chuyển Dòng Tiền', amount: '1.680.000.000 đ', percent: 34.6, color: 'bg-emerald-500' },
                { label: 'Phí Bảo Trì Hạ Tầng & Bản Quyền Phần Cứng IoT', amount: '520.000.000 đ', percent: 10.7, color: 'bg-amber-500' },
                { label: 'Phí Dịch Vụ API eKYC & OCR Theo Lượng Gọi', amount: '200.000.000 đ', percent: 4.2, color: 'bg-rose-500' },
              ].map(item => (
                <div key={item.label} className="p-3 bg-slate-950/60 rounded-xl border border-slate-800/80 space-y-2">
                  <div className="flex justify-between text-xs font-semibold">
                    <span className="text-slate-300">{item.label}</span>
                    <span className="text-white font-mono font-bold">{item.amount} ({item.percent}%)</span>
                  </div>
                  <div className="w-full bg-slate-900 h-2 rounded-full overflow-hidden">
                    <div className={`${item.color} h-full rounded-full`} style={{ width: `${item.percent}%` }} />
                  </div>
                </div>
              ))}
            </div>
          </Card>

          <Card className="space-y-4">
            <h2 className="text-sm font-bold text-white flex items-center gap-2 pb-3 border-b border-slate-800">
              <Globe2 className="w-4 h-4 text-cyan-400" />
              <span>Top 3 Đối Tác Doanh Nghiệp Lớn Nhất</span>
            </h2>
            <div className="space-y-3 text-xs">
              <div className="p-3.5 bg-slate-950 rounded-xl border border-slate-800">
                <div className="font-bold text-white flex items-center justify-between">
                  <span>VPBank Piggy Digital</span>
                  <span className="text-amber-400 font-mono">VPB-FINTECH</span>
                </div>
                <div className="text-[11px] text-slate-400 mt-1">18,450 End-Users &bull; 820 IoT Devices</div>
                <div className="text-emerald-400 font-bold font-mono mt-1">1.820.000.000 đ / tháng</div>
              </div>

              <div className="p-3.5 bg-slate-950 rounded-xl border border-slate-800">
                <div className="font-bold text-white flex items-center justify-between">
                  <span>Techcombank Junior</span>
                  <span className="text-amber-400 font-mono">TCB-KIDS</span>
                </div>
                <div className="text-[11px] text-slate-400 mt-1">24,200 End-Users &bull; 650 IoT Devices</div>
                <div className="text-emerald-400 font-bold font-mono mt-1">2.140.000.000 đ / tháng</div>
              </div>

              <div className="p-3.5 bg-slate-950 rounded-xl border border-slate-800">
                <div className="font-bold text-white flex items-center justify-between">
                  <span>Viettel Digital Family</span>
                  <span className="text-amber-400 font-mono">VIETTEL-PAY</span>
                </div>
                <div className="text-[11px] text-slate-400 mt-1">5,850 End-Users &bull; 72 IoT Devices</div>
                <div className="text-emerald-400 font-bold font-mono mt-1">450.000.000 đ / tháng</div>
              </div>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
};
