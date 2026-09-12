export type AccountType = 'ASSET' | 'LIABILITY' | 'EQUITY' | 'REVENUE' | 'EXPENSE';

export interface LedgerPosting {
  accountNumber: string;
  accountName: string;
  debit: number;
  credit: number;
}

export interface LedgerEntry {
  id: string;
  traceId: string;
  idempotencyKey: string;
  transactionType: string;
  description: string;
  postings: LedgerPosting[];
  totalDebit: number;
  totalCredit: number;
  isBalanced: boolean;
  outboxStatus: 'SENT' | 'PENDING' | 'FAILED';
  createdAt: string;
}

export interface TrialBalance {
  totalDebit: number;
  totalCredit: number;
  difference: number;
  isBalanced: boolean;
  lastAuditTime: string;
}
