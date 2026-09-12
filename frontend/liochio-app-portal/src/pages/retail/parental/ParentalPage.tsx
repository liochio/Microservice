import React from 'react';
import { ParentalMatchingConfig } from '../../../components/parental/ParentalMatchingConfig';
import { GoalsQuests } from '../../../components/piggy/GoalsQuests';

export const ParentalPage: React.FC = () => {
  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h2 className="text-xl font-bold text-white">Cổng Kiểm Soát Dành Riêng Cho Phụ Huynh</h2>
        <p className="text-xs text-slate-400">
          Thiết lập tỷ lệ thưởng tiết kiệm khuyến khích con, mã PIN phê duyệt và theo dõi các mục tiêu tài chính nhí.
        </p>
      </div>

      <ParentalMatchingConfig />
      <GoalsQuests />
    </div>
  );
};
