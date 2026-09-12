import React from 'react';
import { Button } from '../common/Button';
import { Download } from 'lucide-react';
import { useToast } from '../common/Toast';

export const CsvExporter: React.FC = () => {
  const { success } = useToast();

  const handleExport = () => {
    const csvContent = 'data:text/csv;charset=utf-8,ID,TraceId,Type,Debit,Credit,Balanced,Timestamp\n' +
      'LEDGER-001,TRC-9821-4401,PIGGY_COIN_DROP,75000,75000,TRUE,2026-09-09T12:00:00Z\n' +
      'LEDGER-002,TRC-9821-4402,SAGA_PHASE1_HOLD,100000,100000,TRUE,2026-09-09T12:30:00Z\n' +
      'LEDGER-003,TRC-9821-4403,SAGA_PHASE2_SETTLE,100000,100000,TRUE,2026-09-09T13:00:00Z\n';

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `liochio_general_ledger_${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    success('Xuất CSV Thành Công!', 'Tệp sao kê sổ cái kép đã được tải về máy tính.');
  };

  return (
    <Button variant="outline" size="sm" onClick={handleExport} leftIcon={<Download className="w-4 h-4" />}>
      Xuất Báo Cáo Sổ Cái (CSV)
    </Button>
  );
};
