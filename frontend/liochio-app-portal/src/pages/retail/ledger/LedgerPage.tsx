import React from 'react';
import { TrialBalanceGauge } from '../../../components/ledger/TrialBalanceGauge';
import { JournalTable } from '../../../components/ledger/JournalTable';
import { CsvExporter } from '../../../components/ledger/CsvExporter';

export const LedgerPage: React.FC = () => {
  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-white">Lõi Sổ Cái Kép (Core Double-Entry Ledger)</h2>
          <p className="text-xs text-slate-400">
            Hạch toán đối soát Nợ/Có thời gian thực, đảm bảo phương trình cân bằng tài chính Delta = 0.00 đ.
          </p>
        </div>
        <CsvExporter />
      </div>

      <TrialBalanceGauge />
      <JournalTable />
    </div>
  );
};
