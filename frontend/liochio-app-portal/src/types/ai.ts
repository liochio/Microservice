export interface ReceiptItem {
  name: string;
  price: number;
  qty: number;
}

export interface ReceiptOcrResult {
  merchantName: string;
  invoiceDate: string;
  totalAmount: number;
  category: 'THIET_YEU' | 'LINH_HOAT' | 'TIET_KIEM';
  items: ReceiptItem[];
  confidenceScore: number;
  rawText?: string;
}

export interface Budget503020Rule {
  income: number;
  needsTarget: number;    // 50%
  wantsTarget: number;    // 30%
  savingsTarget: number;  // 20%
  needsActual: number;
  wantsActual: number;
  savingsActual: number;
  healthScore: number;
  recommendations: string[];
}

export interface GaussianKdePoint {
  intervalDays: number;
  probabilityDensity: number;
}

export interface GaussianKdeModel {
  distribution: GaussianKdePoint[];
  medianIntervalDays: number;
  p90LateThresholdDays: number;
  predictedNextDepositDays: number;
  recommendedPrompt: string;
  isLate: boolean;
}

export interface ZScoreAnomaly {
  txId: string;
  amount: number;
  mean: number;
  stdDev: number;
  zScore: number;
  severity: 'NORMAL' | 'SUSPICIOUS' | 'CRITICAL_FRAUD';
  flaggedReason: string;
  timestamp: string;
}
