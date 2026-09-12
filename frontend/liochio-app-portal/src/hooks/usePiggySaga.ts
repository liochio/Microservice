import { useState, useEffect, useCallback } from 'react';
import { SagaWithdrawalSession } from '../types/piggy';
import { piggyApi } from '../api/piggyApi';
import { useAudio } from './useAudio';

export const usePiggySaga = () => {
  const [session, setSession] = useState<SagaWithdrawalSession | null>(null);
  const [isProcessing, setIsProcessing] = useState<boolean>(false);
  const { playSolenoidClick, playAlarmSiren } = useAudio();

  // 60s Countdown timer effect
  useEffect(() => {
    if (!session || session.status !== 'HOLDING') return;

    const timer = setInterval(() => {
      const now = Date.now();
      const remainingMs = Math.max(0, session.expiresAt - now);
      const remainingSec = Math.ceil(remainingMs / 1000);

      if (remainingSec <= 0) {
        clearInterval(timer);
        handleTimeout();
      } else {
        setSession((prev) => prev ? { ...prev, remainingSeconds: remainingSec } : null);
      }
    }, 1000);

    return () => clearInterval(timer);
  }, [session?.status, session?.expiresAt]);

  const startWithdrawal = async (amount: number, parentPin?: string) => {
    setIsProcessing(true);
    try {
      const newSession = await piggyApi.requestWithdrawalSaga(amount, parentPin);
      setSession(newSession);
      playSolenoidClick();
    } finally {
      setIsProcessing(false);
    }
  };

  const confirmSettle = async () => {
    if (!session) return;
    setIsProcessing(true);
    try {
      await piggyApi.settleWithdrawalSaga(session.sagaId);
      setSession((prev) => prev ? { ...prev, status: 'SETTLED', solenoidState: 'NC_CLOSED' } : null);
      playSolenoidClick();
    } finally {
      setIsProcessing(false);
    }
  };

  const handleTimeout = useCallback(async () => {
    if (!session) return;
    setIsProcessing(true);
    try {
      await piggyApi.timeoutWithdrawalSaga(session.sagaId);
      setSession((prev) => prev ? { ...prev, status: 'TIMED_OUT', solenoidState: 'NC_CLOSED' } : null);
    } finally {
      setIsProcessing(false);
    }
  }, [session]);

  const simulateJam = () => {
    if (!session) return;
    setSession((prev) => prev ? { ...prev, status: 'JAMMED', failureReason: 'Cảm biến hồng ngoại phát hiện kẹt cơ khí khe nhả tiền!' } : null);
    playAlarmSiren();
  };

  const resetSaga = () => {
    setSession(null);
  };

  return {
    session,
    isProcessing,
    startWithdrawal,
    confirmSettle,
    handleTimeout,
    simulateJam,
    resetSaga,
  };
};
