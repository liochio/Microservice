import React, { useState } from 'react';
import { 
  CheckSquare, 
  Clock, 
  CheckCircle2, 
  XCircle, 
  PlusCircle, 
  History, 
  ShieldAlert, 
  AlertCircle,
  Search,
  Filter
} from 'lucide-react';

interface ApprovalItem {
  id: number;
  requestCode: string;
  requestType: string;
  entityType: string;
  title: string;
  makerUsername: string;
  makerNote: string;
  payloadBefore?: string;
  payloadAfter: string;
  checkerUsername?: string;
  checkerNote?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  rejectionReason?: string;
  createdAt: string;
  reviewedAt?: string;
}

export const ApprovalsCenterPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'PENDING' | 'HISTORY' | 'CREATE'>('PENDING');
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedItem, setSelectedItem] = useState<ApprovalItem | null>(null);
  const [rejectionModalOpen, setRejectionModalOpen] = useState(false);
  const [rejectionReason, setRejectionReason] = useState('');
  const [checkerNote, setCheckerNote] = useState('');

  // Sample data
  const [approvals, setApprovals] = useState<ApprovalItem[]>([
    {
      id: 101,
      requestCode: 'REQ_178902001_A1B2',
      requestType: 'LIMIT_OVERRIDE',
      entityType: 'USER',
      title: 'Nâng trần hạn mức ngày User fintech_user01 lên 100,000,000 VND',
      makerUsername: 'maker_staff01',
      makerNote: 'Khách hàng VIP đã xác minh thu nhập bổ sung tại quầy giao dịch',
      payloadBefore: '{"dailyLimit": 50000000.00, "tier": "TIER_2"}',
      payloadAfter: '{"dailyLimit": 100000000.00, "tier": "TIER_3"}',
      status: 'PENDING',
      createdAt: '2026-09-10 14:30:00'
    },
    {
      id: 102,
      requestCode: 'REQ_178902002_C3D4',
      requestType: 'FEE_CHANGE',
      entityType: 'FEE',
      title: 'Miễn phí nạp tiền VietQR cho chương trình Ngày Quốc Tế Thiếu Nhi',
      makerUsername: 'maker_staff02',
      makerNote: 'Chương trình khuyến mãi giảm phí nạp ví tích lũy từ 1.1% về 0%',
      payloadBefore: '{"topupFeePercent": 1.10}',
      payloadAfter: '{"topupFeePercent": 0.00}',
      status: 'PENDING',
      createdAt: '2026-09-10 15:10:00'
    },
    {
      id: 103,
      requestCode: 'REQ_178901990_E5F6',
      requestType: 'REFUND_DISPUTE',
      entityType: 'WALLET',
      title: 'Hoàn trả tra soát kẹt xu heo đất Serial #PIGGY-VN-8802',
      makerUsername: 'maker_staff01',
      makerNote: 'Cảm biến kẹt xu đã được nhân viên kỹ thuật xác nhận tại nhà',
      payloadBefore: '{"holdAmount": 50000.00}',
      payloadAfter: '{"refundAmount": 50000.00, "status": "SETTLED"}',
      checkerUsername: 'checker_supervisor',
      checkerNote: 'Đã đối chiếu log gia tốc kế và video hỗ trợ. Phê chuẩn hoàn tiền.',
      status: 'APPROVED',
      createdAt: '2026-09-09 10:15:00',
      reviewedAt: '2026-09-09 11:00:00'
    }
  ]);

  const storedUserJson = localStorage.getItem('app_user') || localStorage.getItem('corp_user') || '{}';
  let currentUser: any = {};
  try {
    currentUser = JSON.parse(storedUserJson);
  } catch (e) {
    currentUser = { username: 'corp_maker', roles: ['ROLE_MAKER'] };
  }
  const currentUsername = currentUser.username || 'corp_maker';
  const isChecker = (currentUser.roles || []).includes('ROLE_CHECKER') || (currentUser.roles || []).includes('ROLE_CORP_ADMIN');
  const isMaker = (currentUser.roles || []).includes('ROLE_MAKER') || (currentUser.roles || []).includes('ROLE_CORP_ADMIN');

  // Form submit state
  const [newRequest, setNewRequest] = useState({
    requestType: 'LIMIT_OVERRIDE',
    entityType: 'USER',
    title: '',
    makerNote: '',
    payloadAfter: '{\n  "dailyLimit": 20000000\n}'
  });

  const handleApprove = (item: ApprovalItem) => {
    if (!isChecker) {
      alert('⛔ TRUY CẬP BỊ TỪ CHỐI: Chỉ tài khoản Kiểm Soát Viên (ROLE_CHECKER / ROLE_CORP_ADMIN) mới có quyền duyệt yêu cầu!');
      return;
    }

    if (item.makerUsername === currentUsername) {
      alert('⚠️ Vi phạm nguyên tắc SoD (Separation of Duties): Bạn không được tự duyệt yêu cầu do chính mình tạo!');
      return;
    }

    setApprovals(prev => prev.map(a => a.id === item.id ? {
      ...a,
      status: 'APPROVED',
      checkerUsername: currentUsername,
      checkerNote: checkerNote || 'Phê chuẩn hợp lệ theo quy chế kiểm soát rủi ro.',
      reviewedAt: new Date().toLocaleString()
    } : a));
    alert(`✅ Đã phê duyệt yêu cầu ${item.requestCode} thành công!`);
    setSelectedItem(null);
  };

  const handleReject = () => {
    if (!isChecker) {
      alert('⛔ TRUY CẬP BỊ TỪ CHỐI: Chỉ tài khoản Kiểm Soát Viên (ROLE_CHECKER / ROLE_CORP_ADMIN) mới có quyền từ chối yêu cầu!');
      return;
    }

    if (!selectedItem || !rejectionReason.trim()) {
      alert('Vui lòng nhập lý do từ chối cụ thể!');
      return;
    }

    setApprovals(prev => prev.map(a => a.id === selectedItem.id ? {
      ...a,
      status: 'REJECTED',
      checkerUsername: currentUsername,
      rejectionReason: rejectionReason,
      reviewedAt: new Date().toLocaleString()
    } : a));

    setRejectionModalOpen(false);
    setRejectionReason('');
    setSelectedItem(null);
    alert(`❌ Đã từ chối yêu cầu ${selectedItem.requestCode}!`);
  };

  const handleCreateSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const item: ApprovalItem = {
      id: Date.now(),
      requestCode: `REQ_${Date.now()}_${Math.random().toString(36).substring(2, 6).toUpperCase()}`,
      requestType: newRequest.requestType,
      entityType: newRequest.entityType,
      title: newRequest.title,
      makerUsername: currentUsername,
      makerNote: newRequest.makerNote,
      payloadAfter: newRequest.payloadAfter,
      status: 'PENDING',
      createdAt: new Date().toLocaleString()
    };
    setApprovals([item, ...approvals]);
    alert(`🎉 Yêu cầu đã được tạo và gửi vào Hàng đợi Checker phê duyệt!`);
    setActiveTab('PENDING');
    setNewRequest({
      requestType: 'LIMIT_OVERRIDE',
      entityType: 'USER',
      title: '',
      makerNote: '',
      payloadAfter: '{\n  "dailyLimit": 20000000\n}'
    });
  };

  const pendingList = approvals.filter(a => a.status === 'PENDING');
  const historyList = approvals.filter(a => a.status !== 'PENDING');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-black tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-400">
              TRUNG TÂM PHÊ DUYỆT MAKER - CHECKER
            </h1>
            <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-cyan-500/10 text-cyan-300 border border-cyan-500/30 font-mono">
              MENU: CORP_APPROVAL_CENTER
            </span>
          </div>
          <p className="text-slate-400 text-xs mt-1">
            Quy trình Phân tách Trách nhiệm (Separation of Duties - SoD) &bull; Maker tạo lệnh $\ne$ Checker duyệt lệnh
          </p>
        </div>

        {/* Tab Controls */}
        <div className="flex bg-slate-900 border border-slate-800 p-1 rounded-xl gap-1">
          <button
            onClick={() => setActiveTab('PENDING')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-semibold transition cursor-pointer ${
              activeTab === 'PENDING'
                ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/30 shadow'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Clock className="w-3.5 h-3.5" />
            Hàng Đợi Chờ Duyệt ({pendingList.length})
          </button>
          <button
            onClick={() => setActiveTab('HISTORY')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-semibold transition cursor-pointer ${
              activeTab === 'HISTORY'
                ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/30 shadow'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <History className="w-3.5 h-3.5" />
            Nhật Ký Đã Xử Lý ({historyList.length})
          </button>
          <button
            onClick={() => setActiveTab('CREATE')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-semibold transition cursor-pointer ${
              activeTab === 'CREATE'
                ? 'bg-blue-600 text-white shadow-lg shadow-blue-500/30'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <PlusCircle className="w-3.5 h-3.5" />
            Tạo Lệnh Mới (Maker)
          </button>
        </div>
      </div>

      {/* Warning Alert: SoD Notice */}
      <div className="bg-amber-950/40 border border-amber-500/30 rounded-2xl p-4 flex items-start gap-3">
        <ShieldAlert className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
        <div className="text-xs text-slate-300 leading-relaxed">
          <span className="font-bold text-amber-300">Ràng buộc Kiểm soát Rủi ro SoD:</span> Hệ thống tự động ngăn chặn hành vi tự phê duyệt. Người dùng thuộc vai trò <code>MAKER</code> chỉ được đề xuất thay đổi; chỉ người có vai trò <code>CHECKER</code> khác tài khoản mới có quyền cấp phép.
        </div>
      </div>

      {/* Main Tab Content */}
      {activeTab === 'PENDING' && (
        <div className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {pendingList.map((item) => (
              <div 
                key={item.id} 
                className="bg-slate-900/90 border border-slate-800 hover:border-cyan-500/50 rounded-2xl p-5 transition flex flex-col justify-between shadow-xl"
              >
                <div>
                  <div className="flex items-center justify-between gap-2 mb-3">
                    <span className="px-2.5 py-1 rounded-lg text-[10px] font-mono font-bold bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
                      {item.requestType}
                    </span>
                    <span className="text-[11px] text-slate-400 flex items-center gap-1 font-mono">
                      <Clock className="w-3 h-3 text-amber-400" />
                      {item.createdAt}
                    </span>
                  </div>

                  <h3 className="font-bold text-sm text-slate-100 mb-2 leading-snug">
                    {item.title}
                  </h3>

                  <div className="bg-slate-950/60 rounded-xl p-3 border border-slate-800/80 mb-3 space-y-1.5 text-xs">
                    <div className="flex justify-between text-slate-400">
                      <span>Mã yêu cầu:</span>
                      <span className="font-mono text-cyan-300">{item.requestCode}</span>
                    </div>
                    <div className="flex justify-between text-slate-400">
                      <span>Người tạo (Maker):</span>
                      <span className="font-semibold text-slate-200">{item.makerUsername}</span>
                    </div>
                    <div className="text-slate-300 pt-1 border-t border-slate-800/60">
                      <span className="text-slate-500">Ghi chú:</span> {item.makerNote}
                    </div>
                  </div>

                  <div className="bg-slate-950 rounded-xl p-3 border border-slate-800 font-mono text-[11px] text-emerald-400 overflow-x-auto mb-4">
                    <div className="text-[10px] text-slate-500 uppercase font-bold mb-1">Payload Áp Dụng:</div>
                    <pre>{item.payloadAfter}</pre>
                  </div>
                </div>

                <div className="flex items-center gap-2 pt-3 border-t border-slate-800">
                  <button
                    onClick={() => handleApprove(item)}
                    className="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs shadow-lg shadow-emerald-600/20 transition cursor-pointer"
                  >
                    <CheckCircle2 className="w-4 h-4" />
                    Phê Duyệt (Checker)
                  </button>
                  <button
                    onClick={() => {
                      setSelectedItem(item);
                      setRejectionModalOpen(true);
                    }}
                    className="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl bg-rose-600/20 hover:bg-rose-600/30 text-rose-300 border border-rose-500/30 font-bold text-xs transition cursor-pointer"
                  >
                    <XCircle className="w-4 h-4" />
                    Từ Chối
                  </button>
                </div>
              </div>
            ))}
            {pendingList.length === 0 && (
              <div className="col-span-full py-16 text-center text-slate-500 bg-slate-900/40 rounded-2xl border border-slate-800">
                <CheckCircle2 className="w-12 h-12 mx-auto mb-3 text-emerald-500/50" />
                <p className="text-sm font-semibold">Tất cả yêu cầu đã được xử lý xong!</p>
                <p className="text-xs text-slate-600 mt-1">Hàng đợi Checker hiện không có yêu cầu nào đang chờ.</p>
              </div>
            )}
          </div>
        </div>
      )}

      {/* History Tab */}
      {activeTab === 'HISTORY' && (
        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-300">
              <thead className="bg-slate-950/80 text-slate-400 uppercase font-mono text-[10px] border-b border-slate-800">
                <tr>
                  <th className="p-4">Mã Lệnh</th>
                  <th className="p-4">Loại Yêu Cầu</th>
                  <th className="p-4">Tiêu Đề</th>
                  <th className="p-4">Maker</th>
                  <th className="p-4">Checker</th>
                  <th className="p-4">Trạng Thái</th>
                  <th className="p-4">Thời Gian</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono">
                {historyList.map(item => (
                  <tr key={item.id} className="hover:bg-slate-800/40 transition">
                    <td className="p-4 font-bold text-cyan-300">{item.requestCode}</td>
                    <td className="p-4 text-slate-300">{item.requestType}</td>
                    <td className="p-4 font-sans text-slate-100 max-w-xs truncate">{item.title}</td>
                    <td className="p-4 text-slate-300">{item.makerUsername}</td>
                    <td className="p-4 text-slate-300">{item.checkerUsername || '—'}</td>
                    <td className="p-4">
                      {item.status === 'APPROVED' ? (
                        <span className="px-2 py-1 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                          APPROVED
                        </span>
                      ) : (
                        <span className="px-2 py-1 rounded-full text-[10px] font-bold bg-rose-500/10 text-rose-400 border border-rose-500/30">
                          REJECTED
                        </span>
                      )}
                    </td>
                    <td className="p-4 text-slate-500">{item.reviewedAt || item.createdAt}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Create Maker Request Tab */}
      {activeTab === 'CREATE' && (
        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 max-w-2xl mx-auto shadow-2xl">
          <h2 className="text-lg font-bold text-slate-100 mb-1 flex items-center gap-2">
            <PlusCircle className="w-5 h-5 text-cyan-400" />
            Khởi Tạo Lệnh Yêu Cầu Phê Duyệt Mới (Maker Submission)
          </h2>
          <p className="text-xs text-slate-400 mb-6">
            Lệnh sau khi gửi sẽ nằm trong hàng đợi chờ Checker phê chuẩn trước khi có hiệu lực thực tế trên Sổ cái & Cấu hình.
          </p>

          <form onSubmit={handleCreateSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">Loại Yêu Cầu</label>
                <select
                  value={newRequest.requestType}
                  onChange={e => setNewRequest({ ...newRequest, requestType: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-slate-200 focus:border-cyan-500 outline-none"
                >
                  <option value="LIMIT_OVERRIDE">Ghi đè Hạn mức Giao dịch (LIMIT_OVERRIDE)</option>
                  <option value="FEE_CHANGE">Điều chỉnh Biểu phí Thuê/Nạp (FEE_CHANGE)</option>
                  <option value="REFUND_DISPUTE">Hoàn trả Tra soát Khiếu nại (REFUND_DISPUTE)</option>
                  <option value="ROLE_GRANT">Cấp quyền Tài khoản Nhân sự (ROLE_GRANT)</option>
                  <option value="CONFIG_CHANGE">Thay đổi Tham số Vận hành (CONFIG_CHANGE)</option>
                </select>
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">Đối Tượng Thực Thể</label>
                <select
                  value={newRequest.entityType}
                  onChange={e => setNewRequest({ ...newRequest, entityType: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-slate-200 focus:border-cyan-500 outline-none"
                >
                  <option value="USER">Người dùng Cá nhân (USER)</option>
                  <option value="WALLET">Tài khoản Sổ cái Ví (WALLET)</option>
                  <option value="CONFIG">Tham số Cấu hình (CONFIG)</option>
                  <option value="DEVICE">Thiết bị Heo Đất IoT (DEVICE)</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Tiêu Đề Tóm Tắt</label>
              <input
                type="text"
                required
                placeholder="VD: Nâng hạn mức chuyển tiền ngày lên 50,000,000 VND cho KH 079200001234"
                value={newRequest.title}
                onChange={e => setNewRequest({ ...newRequest, title: e.target.value })}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-slate-200 focus:border-cyan-500 outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Ghi Chú & Căn Cứ Đề Xuất (Maker Note)</label>
              <textarea
                rows={3}
                required
                placeholder="Nêu rõ lý do, biên bản đính kèm hoặc kết quả thẩm định thực tế..."
                value={newRequest.makerNote}
                onChange={e => setNewRequest({ ...newRequest, makerNote: e.target.value })}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs text-slate-200 focus:border-cyan-500 outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Dữ Liệu Cấu Hình Mới (JSON Payload)</label>
              <textarea
                rows={4}
                required
                value={newRequest.payloadAfter}
                onChange={e => setNewRequest({ ...newRequest, payloadAfter: e.target.value })}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 font-mono text-xs text-emerald-400 focus:border-cyan-500 outline-none"
              />
            </div>

            <div className="pt-3">
              <button
                type="submit"
                className="w-full py-3 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-bold text-xs shadow-lg shadow-cyan-500/20 transition cursor-pointer"
              >
                Gửi Lệnh Yêu Cầu Phê Duyệt Cho Checker
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Rejection Modal */}
      {rejectionModalOpen && selectedItem && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 max-w-md w-full shadow-2xl space-y-4">
            <h3 className="text-sm font-bold text-rose-400 flex items-center gap-2">
              <AlertCircle className="w-4 h-4" />
              Từ Chối Yêu Cầu Phê Duyệt
            </h3>
            <p className="text-xs text-slate-300">
              Yêu cầu <span className="font-mono text-cyan-300 font-bold">{selectedItem.requestCode}</span> sẽ bị hủy và lưu vào nhật ký kiểm toán.
            </p>
            <textarea
              rows={3}
              placeholder="Nhập lý do từ chối (bắt buộc)..."
              value={rejectionReason}
              onChange={e => setRejectionReason(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-xs text-slate-200 outline-none focus:border-rose-500"
            />
            <div className="flex gap-2">
              <button
                onClick={() => setRejectionModalOpen(false)}
                className="flex-1 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-300 transition"
              >
                Hủy
              </button>
              <button
                onClick={handleReject}
                className="flex-1 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-500 text-xs font-bold text-white shadow transition"
              >
                Xác Nhận Từ Chối
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
