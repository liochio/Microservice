export type WalletType = 'AVAILABLE' | 'SAVINGS' | 'ESCROW';
export type WalletStatus = 'ACTIVE' | 'FROZEN' | 'LOCKED';

export interface Wallet {
  id: string;
  userId: string;
  type: WalletType;
  name: string;
  balance: number;
  currency: string;
  status: WalletStatus;
  accountNumber: string;
  description: string;
  updatedAt: string;
}

export interface ClosedLoopTransferPayload {
  fromWalletType: WalletType;
  toWalletType: WalletType;
  toUserId?: string;
  amount: number;
  note?: string;
}

export interface TransferResult {
  transactionId: string;
  fromWallet: WalletType;
  toWallet: WalletType;
  amount: number;
  status: 'SUCCESS' | 'BLOCKED_BY_CLOSED_LOOP' | 'FAILED';
  message: string;
  timestamp: string;
}
