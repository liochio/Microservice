import React, { useEffect, useState } from 'react';
import { LedgerEntry } from '../../types/ledger';
import { ledgerApi } from '../../api/ledgerApi';
import { BookOpenCheck, Check, Send } from 'lucide-react';
import { Badge } from '../common/Badge';

export const JournalTable: React.FC = () => {
  const [entries, setEntries] = useState<LedgerEntry[]>([]);

  useEffect(() => {
    ledgerApi.getLedgerEntries().then(setEntries);
  }, []);

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-bold text-base text-slate-100 flex items-center gap-2">
          <BookOpenCheck className="w-5 h-5 text-cyan-400" />
          Nhật Ký Hạch Toán Sổ Cái Kép (Double-Entry General Ledger)
        </h3>
        <span className="text-xs text-slate-400">Tự động khóa sổ theo thời gian thực</span>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs text-slate-300 border-collapse">
          <thead>
            <tr className="border-b border-slate-800 bg-slate-900/70 text-slate-400">
              <th className="p-3">MÃ GIAO DỊCH / TRACE</th>
              <th className="p-3">MÔ TẢ NGHIỆP VỤ</th>
              <th className="p-3 text-right">NỢ (DEBIT)</th>
              <th className="p-3 text-right">CÓ (CREDIT)</th>
              <th className="p-3 text-center">OUTBOX</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800/60">
            {entries.map((entry) => (
              <tr key={entry.id} className="hover:bg-slate-900/40 transition-colors">
                <td className="p-3 font-mono">
                  <div className="font-bold text-slate-200">{entry.id}</div>
                  <div className="text-[10px] text-slate-500">{entry.traceId}</div>
                </td>
                <td className="p-3">
                  <p className="font-semibold text-slate-200">{entry.description}</p>
                  <div className="text-[10px] text-slate-400 mt-1 space-y-0.5 font-mono">
                    {entry.postings.map((p, i) => (
                      <div key={i}>
                        • {p.accountNumber}: {p.debit > 0 ? `Nợ +${p.debit.toLocaleString('vi-VN')} đ` : `Có -${p.credit.toLocaleString('vi-VN')} đ`}
                      </div>
                    ))}
                  </div>
                </td>
                <td className="p-3 text-right font-mono font-bold text-emerald-400">
                  {entry.totalDebit.toLocaleString('vi-VN')} đ
                </td>
                <td className="p-3 text-right font-mono font-bold text-cyan-400">
                  {entry.totalCredit.toLocaleString('vi-VN')} đ
                </td>
                <td className="p-3 text-center">
                  <Badge variant="emerald" size="sm">
                    {entry.outboxStatus}
                  </Badge>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
