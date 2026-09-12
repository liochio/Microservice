export type SagaWithdrawalStatus = 'IDLE' | 'HOLDING' | 'SETTLED' | 'TIMED_OUT' | 'JAMMED';

export interface PiggyState {
  deviceId: string;
  deviceToken: string;
  deviceName: string;
  isOnline: boolean;
  totalSaved: number;
  xp: number;
  level: number;
  levelTitle: string;
  xpToNextLevel: number;
  parentMatchingBonusRate: number; // e.g. 0.5 for +50%
  solenoidLocked: boolean;
  vibrationAlert: boolean;
  isFrozen: boolean;
  lastHeartbeat: string;
}

export interface CoinDropPayload {
  denomination: number; // 10000, 20000, 50000, 100000, 200000, 500000
  note?: string;
}

export interface CoinDropResult {
  success: boolean;
  amount: number;
  bonusAmount: number;
  totalCredited: number;
  xpEarned: number;
  newTotalSaved: number;
  newLevel: number;
  txId: string;
  message: string;
}

export interface SagaWithdrawalSession {
  sagaId: string;
  amount: number;
  status: SagaWithdrawalStatus;
  startedAt: number; // timestamp ms
  expiresAt: number; // startedAt + 60,000ms
  remainingSeconds: number;
  solenoidState: 'NC_OPEN' | 'NC_CLOSED';
  failureReason?: string;
}

export interface SavingsGoal {
  id: string;
  title: string;
  targetAmount: number;
  currentAmount: number;
  icon: string;
  completed: boolean;
}
