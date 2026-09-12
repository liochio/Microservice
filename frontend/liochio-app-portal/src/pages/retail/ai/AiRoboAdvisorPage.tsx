import React, { useEffect, useState } from 'react';
import { GaussianKdeChart } from '../../../components/ai/GaussianKdeChart';
import { ReceiptOcrScanner } from '../../../components/ai/ReceiptOcrScanner';
import { Budget503020 } from '../../../components/ai/Budget503020';
import { FraudAlerts } from '../../../components/ai/FraudAlerts';
import { aiApi } from '../../../api/aiApi';
import { GaussianKdeModel } from '../../../types/ai';

export const AiRoboAdvisorPage: React.FC = () => {
  const [kdeModel, setKdeModel] = useState<GaussianKdeModel | null>(null);

  useEffect(() => {
    aiApi.getGaussianKdeDistribution().then(setKdeModel);
  }, []);

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h2 className="text-xl font-bold text-white">AI Robo-Advisor & Phân Tích Thông Minh</h2>
        <p className="text-xs text-slate-400">
          Mô hình toán học Gaussian KDE học thói quen nạp tiền, quét hóa đơn Vision OCR và quét dị thường Z-Score.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {kdeModel && <GaussianKdeChart model={kdeModel} />}
        <Budget503020 />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <ReceiptOcrScanner />
        <FraudAlerts />
      </div>
    </div>
  );
};
