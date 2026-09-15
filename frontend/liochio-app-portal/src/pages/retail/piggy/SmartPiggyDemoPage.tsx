import React, { useState, useEffect, useContext, useRef, useCallback } from 'react';
import {
  Cpu,
  Wifi,
  WifiOff,
  Shield,
  ShieldAlert,
  ShieldCheck,
  AlertTriangle,
  CheckCircle,
  RefreshCw,
  DollarSign,
  Wallet,
  PiggyBank,
  Heart,
  Flame,
  Bell,
  Sparkles,
  Terminal,
  Lock,
  Unlock,
  Hammer,
  Play,
  Zap,
  Trash2,
  ArrowRight,
  ArrowDownUp,
  Radio,
  Clock,
  Layers,
  Check,
  UserPlus,
  Key,
  Smartphone,
  CheckSquare,
  XCircle,
  Database,
  Power,
  Activity,
  TrendingUp,
  Send,
  X,
  Info,
  Mail,
  Unlink,
  Volume2,
  Coins
} from 'lucide-react';
import { WebSocketContext } from '../../../context/WebSocketContext';
import { piggyApi, PiggyDevice, BucketGoal, HealthScoreResult } from '../../../api/piggyApi';
import { apiClient } from '../../../api/client';

interface AuditLogEntry {
  id: string;
  time: string;
  action: string;
  detail: string;
  status: 'SUCCESS' | 'PENDING' | 'REJECTED' | 'ALARM';
}

interface DiscoveredPiggy {
  id: string;
  name: string;
  mac: string;
  signal: number;
  firmware: string;
  discoveredAt: string;
}

interface PopupModalData {
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
}

// =========================================================================
// CINEMATIC WEB AUDIO API SYNTHESIZER (Pure Browser Synthesis - No 404s)
// =========================================================================
const playAudioEffect = (type: 'coin' | 'smash' | 'success' | 'beep' | 'radar' | 'siren') => {
  try {
    const AudioContextClass = window.AudioContext || (window as any).webkitAudioContext;
    if (!AudioContextClass) return;
    const ctx = new AudioContextClass();

    if (type === 'coin') {
      // 1. Paper feeding swish (noise burst)
      const bufferSize = ctx.sampleRate * 0.08;
      const buffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate);
      const data = buffer.getChannelData(0);
      for (let i = 0; i < bufferSize; i++) {
        data[i] = (Math.random() * 2 - 1) * Math.exp(-i / (ctx.sampleRate * 0.03));
      }
      const noise = ctx.createBufferSource();
      noise.buffer = buffer;
      const noiseFilter = ctx.createBiquadFilter();
      noiseFilter.type = 'bandpass';
      noiseFilter.frequency.value = 3500;
      const noiseGain = ctx.createGain();
      noiseGain.gain.setValueAtTime(0.2, ctx.currentTime);
      noiseGain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.08);
      noise.connect(noiseFilter);
      noiseFilter.connect(noiseGain);
      noiseGain.connect(ctx.destination);
      noise.start(ctx.currentTime);

      // 2. Dual chime: "Cha-ching!" cash bell (E6: 1318Hz, C7: 2093Hz)
      const osc1 = ctx.createOscillator();
      const osc2 = ctx.createOscillator();
      const gain1 = ctx.createGain();
      const gain2 = ctx.createGain();

      osc1.type = 'sine';
      osc1.frequency.setValueAtTime(1318.51, ctx.currentTime + 0.06);
      gain1.gain.setValueAtTime(0.35, ctx.currentTime + 0.06);
      gain1.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.45);
      osc1.connect(gain1);
      gain1.connect(ctx.destination);
      osc1.start(ctx.currentTime + 0.06);
      osc1.stop(ctx.currentTime + 0.45);

      osc2.type = 'triangle';
      osc2.frequency.setValueAtTime(2093.00, ctx.currentTime + 0.12);
      gain2.gain.setValueAtTime(0.4, ctx.currentTime + 0.12);
      gain2.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.55);
      osc2.connect(gain2);
      gain2.connect(ctx.destination);
      osc2.start(ctx.currentTime + 0.12);
      osc2.stop(ctx.currentTime + 0.55);

      // 3. Falling coins clinking in belly (B5 & D6)
      [1975.53, 2349.32].forEach((freq, idx) => {
        const coinOsc = ctx.createOscillator();
        const coinGain = ctx.createGain();
        coinOsc.type = 'sine';
        coinOsc.frequency.value = freq;
        const sTime = ctx.currentTime + 0.2 + idx * 0.09;
        coinGain.gain.setValueAtTime(0.25, sTime);
        coinGain.gain.exponentialRampToValueAtTime(0.001, sTime + 0.25);
        coinOsc.connect(coinGain);
        coinGain.connect(ctx.destination);
        coinOsc.start(sTime);
        coinOsc.stop(sTime + 0.25);
      });
    } else if (type === 'smash') {
      // 1. Heavy metallic hammer thud (Deep bass drop 140Hz -> 20Hz)
      const thud = ctx.createOscillator();
      const thudGain = ctx.createGain();
      thud.type = 'sine';
      thud.frequency.setValueAtTime(140, ctx.currentTime);
      thud.frequency.exponentialRampToValueAtTime(20, ctx.currentTime + 0.35);
      thudGain.gain.setValueAtTime(0.85, ctx.currentTime);
      thudGain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.35);
      thud.connect(thudGain);
      thudGain.connect(ctx.destination);
      thud.start(ctx.currentTime);
      thud.stop(ctx.currentTime + 0.35);

      // 2. High frequency ceramic shatter (Multi-stage filtered noise)
      const bufferSize = ctx.sampleRate * 0.6;
      const buffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate);
      const data = buffer.getChannelData(0);
      for (let i = 0; i < bufferSize; i++) {
        data[i] = (Math.random() * 2 - 1) * Math.exp(-i / (ctx.sampleRate * 0.15));
      }
      const noise = ctx.createBufferSource();
      noise.buffer = buffer;
      const filter = ctx.createBiquadFilter();
      filter.type = 'highpass';
      filter.frequency.value = 1600;
      const gain = ctx.createGain();
      gain.gain.setValueAtTime(0.75, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.6);
      noise.connect(filter);
      filter.connect(gain);
      gain.connect(ctx.destination);
      noise.start(ctx.currentTime);

      // 3. Cluster of bouncing gold coins on floor
      const coinTones = [1760, 2093, 2637, 3135, 1396, 2793];
      coinTones.forEach((freq, idx) => {
        const cOsc = ctx.createOscillator();
        const cGain = ctx.createGain();
        cOsc.type = 'sine';
        cOsc.frequency.value = freq;
        const cStart = ctx.currentTime + 0.12 + idx * 0.06;
        cGain.gain.setValueAtTime(0.3, cStart);
        cGain.gain.exponentialRampToValueAtTime(0.001, cStart + 0.28);
        cOsc.connect(cGain);
        cGain.connect(ctx.destination);
        cOsc.start(cStart);
        cOsc.stop(cStart + 0.28);
      });
    } else if (type === 'radar') {
      // High-tech sonar pulse chirp (880Hz -> 1760Hz with soft tail)
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(880, ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(1760, ctx.currentTime + 0.18);
      gain.gain.setValueAtTime(0.3, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.35);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start(ctx.currentTime);
      osc.stop(ctx.currentTime + 0.35);
    } else if (type === 'siren') {
      // Emergency siren warble (600Hz <-> 950Hz)
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'sawtooth';
      const now = ctx.currentTime;
      osc.frequency.setValueAtTime(600, now);
      osc.frequency.linearRampToValueAtTime(950, now + 0.2);
      osc.frequency.linearRampToValueAtTime(600, now + 0.4);
      osc.frequency.linearRampToValueAtTime(950, now + 0.6);
      osc.frequency.linearRampToValueAtTime(600, now + 0.8);
      gain.gain.setValueAtTime(0.25, now);
      gain.gain.exponentialRampToValueAtTime(0.01, now + 0.9);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start(now);
      osc.stop(now + 0.9);
    } else if (type === 'success') {
      // Victory fanfare arpeggio (C5 -> E5 -> G5 -> C6)
      const notes = [523.25, 659.25, 783.99, 1046.50];
      notes.forEach((freq, idx) => {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = 'triangle';
        osc.frequency.value = freq;
        const startTime = ctx.currentTime + idx * 0.08;
        gain.gain.setValueAtTime(0.25, startTime);
        gain.gain.exponentialRampToValueAtTime(0.001, startTime + 0.3);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(startTime);
        osc.stop(startTime + 0.3);
      });
    } else if (type === 'beep') {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(800, ctx.currentTime);
      gain.gain.setValueAtTime(0.18, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.16);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start(ctx.currentTime);
      osc.stop(ctx.currentTime + 0.16);
    }
  } catch (err) {
    console.warn('Audio synthesis warning:', err);
  }
};

export const SmartPiggyDemoPage: React.FC = () => {
  const { isConnected: wsConnected, logs: wsLogs, clearLogs: clearWsLogs, lastMessage } = useContext(WebSocketContext);

  // User Profile loaded dynamically from authenticated session
  const [currentLoggedUser, setCurrentLoggedUser] = useState<{
    id: string;
    username: string;
    fullName: string;
    email: string;
    role: string;
    status: string;
  }>(() => {
    try {
      const stored = localStorage.getItem('app_user') || localStorage.getItem('corp_user');
      if (stored) {
        const u = JSON.parse(stored);
        const uname = u.username || 'retail_user';
        const uid = u.userId ? String(u.userId) : (u.id ? String(u.id) : uname);
        return {
          id: uid,
          username: uname,
          fullName: u.fullName || u.username || 'Khách Hàng Cá Nhân',
          email: u.email || (uname ? (uname + '@liochio.vn') : 'retail_user@liochio.vn'),
          role: (u.roles && u.roles[0]) || 'ROLE_CUSTOMER',
          status: 'ACTIVE'
        };
      }
    } catch (e) {
      console.warn('Failed to parse user session:', e);
    }
    return {
      id: 'retail_user',
      username: 'retail_user',
      fullName: 'Khách Hàng Cá Nhân',
      email: 'retail_user@liochio.vn',
      role: 'ROLE_CUSTOMER',
      status: 'ACTIVE'
    };
  });

  // State: Wallets
  const [walletsList, setWalletsList] = useState<any[]>([]);
  const [selectedWalletId, setSelectedWalletId] = useState<string>('');
  const [activeWallet, setActiveWallet] = useState<any | null>(null);

  // State: Hardware (Left Panel)
  const [piggyPower, setPiggyPower] = useState<boolean>(true);
  const [hardwareTamperLid, setHardwareTamperLid] = useState<boolean>(false);
  const [hardwareImpactShock, setHardwareImpactShock] = useState<boolean>(false);
  const [selectedCoin, setSelectedCoin] = useState<number>(50000);
  const [isCoinDropping, setIsCoinDropping] = useState<boolean>(false);
  const [piggyExpression, setPiggyExpression] = useState<'HAPPY' | 'HUNGRY' | 'SICK' | 'SHOCKED'>('HAPPY');
  const [oledTicker, setOledTicker] = useState<string>('SẴN SÀNG TIẾT KIỆM');
  const [solenoidLocked, setSolenoidLocked] = useState<boolean>(true);

  // State: Paired Piggy Device & Buckets
  const [activeDevice, setActiveDevice] = useState<PiggyDevice | null>(null);
  const [broadcastingDevice, setBroadcastingDevice] = useState<DiscoveredPiggy | null>(null);
  const [buckets, setBuckets] = useState<BucketGoal[]>([]);
  const [healthScore, setHealthScore] = useState<number>(95);
  const [healthStatus, setHealthStatus] = useState<string>('KHỎE MẠNH');

  // Discovered Beacons
  const [discoveredDevices, setDiscoveredDevices] = useState<DiscoveredPiggy[]>([
    {
      id: 'ESP32-PIGGY-101C',
      name: 'Heo Đất ESP32 Phòng Khách',
      mac: '24:6F:28:17:10:1C',
      signal: -48,
      firmware: 'v2.4.2-PROD',
      discoveredAt: new Date().toLocaleTimeString('vi-VN')
    }
  ]);

  // Loading state per device for Pair request
  const [pairLoadingDeviceId, setPairLoadingDeviceId] = useState<string | null>(null);

  // =========================================================================
  // CINEMATIC VISUAL STAGES
  // =========================================================================
  // 1. SMASH PIGGY THEATER (Full-screen theatrical stage)
  const [smashTheaterOpen, setSmashTheaterOpen] = useState<boolean>(false);
  const [smashStep, setSmashStep] = useState<'AIMING' | 'STRIKING' | 'SHATTERED' | 'OTP_INPUT'>('AIMING');
  const [smashOtpInput, setSmashOtpInput] = useState<string>('');

  // 2. BANKNOTE FEEDING STAGE (Visual polymer bill slide-in & swallow animation)
  const [feedingStageOpen, setFeedingStageOpen] = useState<boolean>(false);
  const [feedingStep, setFeedingStep] = useState<'APPROACHING' | 'SWALLOWING' | 'DIGESTED'>('APPROACHING');
  const [feedingAmount, setFeedingAmount] = useState<number>(50000);

  // 3. RADAR WAVE EFFECT (Antenna BLE pulsing)
  const [isRadarPulsing, setIsRadarPulsing] = useState<boolean>(false);

  // Modals & OTPs
  const [pairModalOpen, setPairModalOpen] = useState<boolean>(false);
  const [selectedPairDevice, setSelectedPairDevice] = useState<DiscoveredPiggy | null>(null);
  const [pairOtpInput, setPairOtpInput] = useState<string>('');
  const [otpSentEmail, setOtpSentEmail] = useState<string>('');
  const [devBypassHint, setDevBypassHint] = useState<string>('');

  // Deposit Money Modal
  const [depositModalOpen, setDepositModalOpen] = useState<boolean>(false);
  const [depositPendingAmount, setDepositPendingAmount] = useState<number>(50000);
  const [depositOtpInput, setDepositOtpInput] = useState<string>('');

  // Unbind Device Modal
  const [unbindModalOpen, setUnbindModalOpen] = useState<boolean>(false);
  const [unbindOtpInput, setUnbindOtpInput] = useState<string>('');

  // Bucket Transfer Modal
  const [showTransferModal, setShowTransferModal] = useState<boolean>(false);
  const [fromBucket, setFromBucket] = useState<string>('');
  const [toBucket, setToBucket] = useState<string>('');
  const [transferAmount, setTransferAmount] = useState<number>(100000);

  // Policy Reject Modal
  const [policyRejectModalOpen, setPolicyRejectModalOpen] = useState<boolean>(false);

  // Dedicated Rich Popup Notification Modal
  const [popupModal, setPopupModal] = useState<PopupModalData | null>(null);

  // General Loading indicator
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [notificationMode, setNotificationMode] = useState<'MODE_Y' | 'MODE_N'>('MODE_Y');

  // Real-time Audit Logs
  const [auditLogs, setAuditLogs] = useState<AuditLogEntry[]>([
    {
      id: 'log-0',
      time: new Date().toLocaleTimeString('vi-VN'),
      action: 'SYSTEM_READY',
      detail: 'Khởi tạo thành công Bảng điều khiển Heo Đất IoT & Ngân Hàng Số.',
      status: 'SUCCESS'
    }
  ]);

  const showPopup = (type: 'success' | 'error' | 'warning' | 'info', title: string, message: string) => {
    setPopupModal({ type, title, message });
  };

  const pushAudit = (action: string, detail: string, status: 'SUCCESS' | 'PENDING' | 'REJECTED' | 'ALARM' = 'SUCCESS') => {
    const newEntry: AuditLogEntry = {
      id: 'log-' + Date.now() + '-' + Math.random().toString(36).substring(2, 6),
      time: new Date().toLocaleTimeString('vi-VN'),
      action: action,
      detail: detail,
      status: status
    };
    setAuditLogs(prev => [newEntry, ...prev.slice(0, 49)]);
  };

  // Sync state from Backend
  const refreshAllState = useCallback(async (customWalletId?: string) => {
    setIsLoading(true);
    try {
      const targetWId = customWalletId || selectedWalletId;
      const stateRes = await piggyApi.demoGetState({
        user_id: currentLoggedUser.id,
        wallet_id: targetWId
      });

      if (stateRes.data) {
        const d = stateRes.data;
        if (d.user) {
          setCurrentLoggedUser(prev => ({
            ...prev,
            id: String(d.user.id || prev.id),
            username: d.user.username || prev.username,
            fullName: d.user.full_name || prev.fullName,
            email: d.user.email || prev.email,
            status: d.user.status || prev.status
          }));
        }

        const currentWallets: any[] = d.wallets || [];
        setWalletsList(currentWallets);

        const currentSelected = (targetWId && currentWallets.find((w: any) => w.id === targetWId)) || currentWallets[0] || null;
        setActiveWallet(currentSelected);
        if (currentSelected && currentSelected.id !== selectedWalletId) {
          setSelectedWalletId(currentSelected.id);
        }

        if (d.active_device) {
          setActiveDevice(d.active_device);
          setSolenoidLocked(d.active_device.solenoid_locked ?? true);
        } else {
          const matched = (d.devices || []).find((dev: any) => dev.wallet_id === (currentSelected?.id) && (dev.status === 'ONLINE' || dev.status === 'ACTIVE'));
          if (matched) {
            setActiveDevice(matched);
            setSolenoidLocked(true);
          } else {
            setActiveDevice(null);
          }
        }

        if (d.buckets && d.buckets.length > 0) {
          setBuckets(d.buckets);
        } else {
          setBuckets([]);
        }

        if (d.health) {
          setHealthScore(d.health.score || 95);
          setHealthStatus(d.health.score < 20 ? 'BỆNH / KHÓA AN TOÀN' : 'KHỎE MẠNH');
          if (d.health.score < 20) {
            setPiggyExpression('SICK');
            setOledTicker('CẢNH BÁO: PIN YẾU / ĐÓI TIỀN');
          }
        }
      }
    } catch (err: any) {
      console.warn('Sync State error:', err);
    } finally {
      setIsLoading(false);
    }
  }, [currentLoggedUser.id, selectedWalletId]);

  useEffect(() => {
    refreshAllState();
  }, [refreshAllState]);

  // Quick Unfreeze Wallet handler
  const handleQuickUnfreeze = async (wId: string) => {
    setIsLoading(true);
    try {
      await piggyApi.demoUnfreezeWallet({ wallet_id: wId });
      pushAudit('WALLET_UNFROZEN', 'Mở khóa an toàn cho Ví ' + wId + ' -> Trạng thái chuyển sang ACTIVE', 'SUCCESS');
      await refreshAllState(wId);
      playAudioEffect('beep');
      showPopup('success', 'Mở Khóa Ví Thành Công', 'Ví tài chính đã được mở khóa an toàn và trở về trạng thái ACTIVE sẵn sàng giao dịch.');
    } catch (err: any) {
      showPopup('error', 'Mở Khóa Thất Bại', err?.message || 'Lỗi khi mở khóa ví');
    } finally {
      setIsLoading(false);
    }
  };

  // Quick Activate Wallet handler
  const handleQuickActivate = async (wId: string) => {
    setIsLoading(true);
    try {
      await piggyApi.demoActivateWallet({ wallet_id: wId, otp_code: '123456' });
      pushAudit('WALLET_ACTIVATED', 'Kích hoạt thành công Ví ' + wId + ' qua Smart OTP', 'SUCCESS');
      await refreshAllState(wId);
      playAudioEffect('beep');
      showPopup('success', 'Kích Hoạt Ví Thành Công', 'Ví tài chính đã được kích hoạt thành công (ACTIVE) và sẵn sàng liên kết với Heo Đất.');
    } catch (err: any) {
      showPopup('error', 'Kích Hoạt Thất Bại', err?.message || 'Lỗi khi kích hoạt ví');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelectWallet = (newWId: string) => {
    setSelectedWalletId(newWId);
    refreshAllState(newWId);
    pushAudit('WALLET_SELECTED', 'Khách hàng chuyển sang quản lý Ví: ' + newWId, 'SUCCESS');
  };

  // =========================================================================
  // 1. HARDWARE ACTION: Press Physical Pairing Button (BLE Radar Waves & Audio)
  // =========================================================================
  const handlePressHardwarePair = () => {
    if (!piggyPower) {
      showPopup('warning', 'Thiết Bị Chưa Bật Nguồn', 'Vui lòng bật nguồn Heo Đất trước khi bấm nút kết nối phần cứng.');
      return;
    }

    // Trigger visual radar wave and audio
    setIsRadarPulsing(true);
    playAudioEffect('radar');
    setTimeout(() => setIsRadarPulsing(false), 1600);

    const randHex = Math.random().toString(16).substring(2, 6).toUpperCase();
    const macTail = Math.random().toString(16).substring(2, 4).toUpperCase();
    const newDev: DiscoveredPiggy = {
      id: 'ESP32-PIGGY-' + randHex,
      name: 'Heo Đất ESP32 #' + randHex,
      mac: '24:6F:28:' + randHex.substring(0, 2) + ':' + randHex.substring(2, 4) + ':' + macTail,
      signal: -45 - Math.floor(Math.random() * 25),
      firmware: 'v2.4.2-PROD',
      discoveredAt: new Date().toLocaleTimeString('vi-VN')
    };

    setBroadcastingDevice(newDev);
    setDiscoveredDevices(prev => [newDev, ...prev.filter(d => d.mac !== newDev.mac && d.id !== newDev.id)].slice(0, 6));
    setPiggyExpression('HAPPY');
    setOledTicker('ĐANG PHÁT BEACON BLE: ' + newDev.id);
    pushAudit('HARDWARE_BEACON_BROADCAST', 'Bấm nút vật lý trên Heo Đất -> ESP32 phát sóng Beacon BLE/mDNS với MAC: ' + newDev.mac, 'SUCCESS');
    showPopup('info', 'Phần Cứng Đã Phát Tín Hiệu Ghép Đôi', 'Heo Đất ' + newDev.name + ' (MAC: ' + newDev.mac + ') đang phát sóng Beacon BLE qua ăng-ten! Thiết bị đã được đưa lên đầu danh sách quét bên phải.');
  };

  // =========================================================================
  // 2. FINTECH ACTION: Request OTP via Email to Pair Device
  // =========================================================================
  const handleRequestPairOtp = async (dev: DiscoveredPiggy) => {
    if (!activeWallet) {
      showPopup('warning', 'Chưa Chọn Ví', 'Vui lòng chọn một Ví tài chính để liên kết.');
      return;
    }

    if (activeDevice) {
      showPopup('warning', 'Đã Có Thiết Bị Liên Kết', 'Ví này hiện đã liên kết với Heo Đất "' + activeDevice.device_name + '". Mỗi ví chỉ được liên kết với 1 thiết bị duy nhất. Vui lòng Hủy Liên Kết hoặc Đập Heo để Liên kết thiết bị khác.');
      return;
    }

    setPairLoadingDeviceId(dev.id);

    if (activeWallet.status === 'FROZEN') {
      try {
        await piggyApi.demoUnfreezeWallet({ wallet_id: activeWallet.id });
        pushAudit('WALLET_AUTO_UNFROZEN', 'Tự động mở khóa an toàn cho Ví ' + activeWallet.name + ' để tiến hành liên kết', 'SUCCESS');
        await refreshAllState(activeWallet.id);
      } catch (e: any) {
        setPairLoadingDeviceId(null);
        showPopup('error', 'Mở Khóa Ví Thất Bại', 'Không thể tự động mở khóa ví: ' + (e?.message || 'Lỗi không xác định'));
        return;
      }
    } else if (activeWallet.status === 'PENDING_ACTIVATION') {
      try {
        await piggyApi.demoActivateWallet({ wallet_id: activeWallet.id, otp_code: '123456' });
        pushAudit('WALLET_AUTO_ACTIVATED', 'Tự động kích hoạt Ví ' + activeWallet.name + ' để liên kết', 'SUCCESS');
        await refreshAllState(activeWallet.id);
      } catch (e: any) {
        setPairLoadingDeviceId(null);
        showPopup('error', 'Kích Hoạt Ví Thất Bại', 'Không thể tự động kích hoạt ví: ' + (e?.message || 'Lỗi không xác định'));
        return;
      }
    }

    setSelectedPairDevice(dev);
    setPairOtpInput('');

    try {
      const res = await piggyApi.demoRequestPairOtp({
        wallet_id: activeWallet.id,
        mac_address: dev.mac,
        device_name: dev.name,
        user_id: currentLoggedUser.id,
        destination_email: currentLoggedUser.email
      });

      playAudioEffect('beep');
      const recipient = res.data?.recipient || currentLoggedUser.email;
      setOtpSentEmail(recipient);
      setDevBypassHint(res.data?.dev_bypass_code || '');

      pushAudit('OTP_REQUESTED', 'Yêu cầu mã Smart OTP Liên kết thiết bị ' + dev.name + ' -> Đã gửi email tới ' + recipient, 'SUCCESS');
      setPairModalOpen(true);
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || 'Lỗi khi yêu cầu mã OTP';
      pushAudit('OTP_REQUEST_FAILED', msg, 'REJECTED');
      showPopup('error', 'Gửi OTP Thất Bại', msg);
    } finally {
      setPairLoadingDeviceId(null);
    }
  };

  // =========================================================================
  // 3. FINTECH ACTION: Confirm Pair Device with OTP
  // =========================================================================
  const handleConfirmPairDevice = async () => {
    if (!selectedPairDevice || !activeWallet) return;
    if (!pairOtpInput.trim()) {
      showPopup('warning', 'Thiếu Mã OTP', 'Vui lòng nhập mã OTP gồm 6 chữ số đã gửi về email của bạn.');
      return;
    }

    setIsLoading(true);
    try {
      await piggyApi.demoPairDevice({
        wallet_id: activeWallet.id,
        mac_address: selectedPairDevice.mac,
        device_name: selectedPairDevice.name,
        otp_code: pairOtpInput.trim(),
        user_id: currentLoggedUser.id
      });

      setPairModalOpen(false);
      setBroadcastingDevice(null);
      playAudioEffect('success');
      pushAudit('DEVICE_PAIRED', 'Ghép đôi thành công Thiết bị ' + selectedPairDevice.id + ' với Ví ' + activeWallet.account_number + ' -> Đã lưu CSDL thật, tự động tạo 4 Hũ Tiết Kiệm, vô hiệu hóa các thiết bị khác trong danh sách', 'SUCCESS');
      setPiggyExpression('HAPPY');
      setOledTicker('ĐÃ GHÉP ĐÔI THÀNH CÔNG!');
      await refreshAllState(activeWallet.id);
      showPopup('success', 'Liên kết thiết bị Thành Công', 'Thiết bị ' + selectedPairDevice.name + ' đã được ghép đôi an toàn với Ví ' + activeWallet.name + ' (' + activeWallet.account_number + '). Các thiết bị khác trong danh sách đã được khóa vô hiệu hóa nhằm đảm bảo an toàn duy nhất 1 thiết bị!');
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.response?.data?.context?.message || err?.message || 'Lỗi khi Liên kết thiết bị';
      pushAudit('DEVICE_PAIR_FAILED', msg, 'REJECTED');
      showPopup('error', 'Liên Kết Thất Bại', msg);
    } finally {
      setIsLoading(false);
    }
  };

  // =========================================================================
  // 4. FINTECH ACTION: Request Unbind with OTP
  // =========================================================================
  const handleRequestUnbindDevice = () => {
    if (!activeDevice) return;
    playAudioEffect('beep');
    setUnbindOtpInput('');
    setUnbindModalOpen(true);
  };

  const handleConfirmUnbindDevice = async () => {
    if (!activeDevice) return;
    if (!unbindOtpInput.trim()) {
      showPopup('warning', 'Thiếu Mã OTP', 'Vui lòng nhập mã OTP 6 số để xác nhận hủy liên kết.');
      return;
    }

    setIsLoading(true);
    try {
      await piggyApi.demoUnbindDevice({
        device_id: activeDevice.id,
        user_id: currentLoggedUser.id
      });

      setUnbindModalOpen(false);
      playAudioEffect('beep');
      setActiveDevice(null);
      setBroadcastingDevice(null);
      setBuckets([]);
      pushAudit('DEVICE_UNBOUND', 'Hủy liên kết Heo Đất ' + activeDevice.device_name + ' khỏi Ví ' + (activeWallet?.account_number || '') + ' -> Số dư tích lũy Heo Đất trở về 0 đ, giải phóng danh sách thiết bị quét', 'SUCCESS');
      setOledTicker('ĐÃ HỦY LIÊN KẾT - SỐ DƯ: 0 đ');
      await refreshAllState(activeWallet?.id);
      showPopup('success', 'Hủy Liên Kết Thành Công', 'Đã hủy Liên kết thiết bị Heo Đất thành công qua xác thực OTP. Số dư tích lũy của Heo đã được đưa về 0 đ và tất cả các thiết bị trong danh sách quét đã được kích hoạt lại để sẵn sàng ghép đôi mới.');
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || 'Lỗi khi hủy liên kết';
      pushAudit('UNBIND_FAILED', msg, 'REJECTED');
      showPopup('error', 'Hủy Liên Kết Thất Bại', msg);
    } finally {
      setIsLoading(false);
    }
  };

  // =========================================================================
  // 5. HARDWARE & FINTECH ACTION: Drop Money with OTP & Realistic Banknote Slide-in
  // =========================================================================
  const handleOpenDepositOtpModal = (amount: number) => {
    if (!piggyPower) {
      showPopup('warning', 'Thiết Bị Chưa Bật Nguồn', 'Heo Đất đang tắt nguồn. Vui lòng bật nguồn trước khi nạp tiền.');
      return;
    }
    if (!activeDevice) {
      showPopup('warning', 'Chưa Liên kết thiết bị', 'Ví này chưa được liên kết với Heo Đất nào. Vui lòng Liên kết thiết bị trước khi nạp tiền.');
      return;
    }
    if (hardwareTamperLid || hardwareImpactShock) {
      showPopup('error', 'Cảnh Báo An Ninh Đang Bật', 'Heo Đất đang trong tình trạng cảnh báo an ninh! Vui lòng mở khóa an ninh trước khi nạp tiền.');
      return;
    }

    playAudioEffect('beep');
    setDepositPendingAmount(amount);
    setDepositOtpInput('');
    setDepositModalOpen(true);
  };

  const handleConfirmDepositMoney = async () => {
    if (!depositOtpInput.trim()) {
      showPopup('warning', 'Thiếu Mã OTP', 'Vui lòng nhập mã OTP để xác nhận nhét tiền vào heo.');
      return;
    }

    setDepositModalOpen(false);
    setFeedingAmount(depositPendingAmount);
    setFeedingStageOpen(true);
    setFeedingStep('APPROACHING');
    setIsCoinDropping(true);
    setOledTicker('CẢM BIẾN TCRT5000: TIỀN POLYMER ĐANG CHUI VÀO HEO...');
    setPiggyExpression('HAPPY');

    // Play banknote friction & chime
    playAudioEffect('coin');

    // Step 2: Slide into coin slot
    setTimeout(() => {
      setFeedingStep('SWALLOWING');
      playAudioEffect('coin');
    }, 600);

    try {
      const payload = {
        mac_address: activeDevice?.mac_address || '24:6F:28:17:10:1C',
        amount: depositPendingAmount
      };

      const res = await piggyApi.dropMoney(payload);
      const newBal = res.data?.new_balance ?? ((activeWallet?.balance || 0) + depositPendingAmount);

      setTimeout(() => {
        setFeedingStep('DIGESTED');
        setOledTicker('+' + depositPendingAmount.toLocaleString('vi-VN') + ' Đ | SỐ DƯ HEO: ' + newBal.toLocaleString('vi-VN') + ' Đ');
      }, 1200);

      pushAudit('COIN_DROP_INGESTION', 'Cảm biến quang học TCRT5000 nhận diện tờ polymer ' + depositPendingAmount.toLocaleString('vi-VN') + ' đ chui vào khe heo -> Đã cộng trực tiếp vào số dư ví CSDL thật', 'SUCCESS');
      await refreshAllState(activeWallet?.id);
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || 'Lỗi khi gọi API nạp tiền';
      pushAudit('COIN_DROP_FAILED', msg, 'REJECTED');
      showPopup('error', 'Nạp Tiền Thất Bại', msg);
    } finally {
      setTimeout(() => {
        setFeedingStageOpen(false);
        setIsCoinDropping(false);
      }, 2000);
    }
  };

  // =========================================================================
  // 6. FINTECH ACTION: Bucket Internal Transfer
  // =========================================================================
  const handleConfirmTransfer = async () => {
    if (!fromBucket || !toBucket || fromBucket === toBucket) {
      showPopup('warning', 'Hũ Không Hợp Lệ', 'Vui lòng chọn hũ nguồn và hũ đích khác nhau để điều chuyển.');
      return;
    }
    if (transferAmount <= 0) {
      showPopup('warning', 'Số Tiền Không Hợp Lệ', 'Số tiền chuyển phải lớn hơn 0.');
      return;
    }

    setIsLoading(true);
    try {
      await piggyApi.transferBuckets({
        from_bucket_id: fromBucket,
        to_bucket_id: toBucket,
        amount: transferAmount
      });
      setShowTransferModal(false);
      playAudioEffect('coin');
      pushAudit('BUCKET_TRANSFER', 'Điều chuyển nội bộ ' + transferAmount.toLocaleString('vi-VN') + ' đ giữa 2 hũ tiết kiệm', 'SUCCESS');
      await refreshAllState(activeWallet?.id);
      showPopup('success', 'Điều Chuyển Thành Công', 'Đã điều chuyển ' + transferAmount.toLocaleString('vi-VN') + ' đ giữa các hũ tiết kiệm thành công.');
    } catch (err: any) {
      setShowTransferModal(false);
      const msg = err?.response?.data?.message || err?.message || 'Lỗi khi chuyển hũ';
      pushAudit('BUCKET_TRANSFER_FAILED', msg, 'REJECTED');
      showPopup('error', 'Chuyển Hũ Thất Bại', msg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSimulateBankWithdraw = () => {
    pushAudit('BANK_WITHDRAW_BLOCKED', 'Khách hàng thử rút tiền ra ngân hàng -> Bị từ chối bởi Chính sách FinTech (Chỉ Nạp - Không Rút Lẻ)', 'REJECTED');
    setPolicyRejectModalOpen(true);
  };

  // =========================================================================
  // 7. CINEMATIC FULLSCREEN ACTION: Smash Piggy Theater
  // =========================================================================
  const handleOpenSmashTheater = () => {
    if (!activeDevice) {
      showPopup('warning', 'Chưa Liên kết thiết bị', 'Vui lòng liên kết Heo Đất trước khi thực hiện nghi thức đập heo tất toán.');
      return;
    }

    setSmashOtpInput('');
    setSmashTheaterOpen(true);
    setSmashStep('AIMING');
    setPiggyExpression('SHOCKED');
    setOledTicker('CHUẨN BỊ ĐẬP HEO TẤT TOÁN...');

    // Sequence 1: Hammer swings down after 800ms
    setTimeout(() => {
      setSmashStep('STRIKING');
    }, 700);

    // Sequence 2: Impact moment after 1100ms -> Shatter explosion & audio!
    setTimeout(() => {
      setSmashStep('SHATTERED');
      playAudioEffect('smash');
      setOledTicker('ĐÃ ĐẬP VỠ HEO! TIỀN VÀNG TUNG TÓE...');
    }, 1100);

    // Sequence 3: Reveal OTP confirmation card after 2100ms
    setTimeout(() => {
      setSmashStep('OTP_INPUT');
    }, 2100);
  };

  const handleConfirmSmashInTheater = async () => {
    if (!smashOtpInput.trim()) {
      showPopup('warning', 'Thiếu Mã Smart OTP', 'Vui lòng nhập mã Smart OTP để hoàn tất tất toán.');
      return;
    }

    setIsLoading(true);
    try {
      const devId = activeDevice?.id || 'ESP32-PIGGY-101C';
      await piggyApi.demoSmash({
        device_id: devId,
        wallet_id: activeWallet?.id,
        user_id: currentLoggedUser.id,
        otp_code: smashOtpInput.trim()
      });

      playAudioEffect('success');
      setSolenoidLocked(false);
      setActiveDevice(null);
      setBroadcastingDevice(null);
      setBuckets([]);
      pushAudit('SMASH_PIGGY_COMPLETED', 'Đập Heo Tất Toán thành công! Mở khóa Solenoid 5V, số dư chuyển về 0 đ, hủy Liên kết thiết bị để sẵn sàng chu kỳ mới, mở khóa lại toàn bộ thiết bị trong danh sách scanner', 'SUCCESS');
      setOledTicker('TẤT TOÁN THÀNH CÔNG: SỐ DƯ VỀ 0 đ');
      await refreshAllState(activeWallet?.id);

      // Close theater after celebratory moment
      setTimeout(() => {
        setSmashTheaterOpen(false);
        showPopup('success', 'Tất Toán Thành Công', 'Chúc mừng bạn đã hoàn thành chu kỳ tiết kiệm! Khóa Solenoid đã mở bung nắp, toàn bộ số dư đã được tất toán về 0 đ và tất cả các thiết bị trong danh sách quét đã được mở khóa để bắt đầu chu kỳ mới.');
      }, 800);
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || 'Lỗi khi đập heo tất toán';
      pushAudit('SMASH_FAILED', msg, 'REJECTED');
      showPopup('error', 'Đập Heo Thất Bại', msg);
    } finally {
      setIsLoading(false);
    }
  };

  // =========================================================================
  // 8. HARDWARE ALARMS WITH SOUND & VISUAL SIREN
  // =========================================================================
  const handleTriggerLidTamper = async () => {
    setHardwareTamperLid(true);
    setPiggyExpression('SHOCKED');
    setOledTicker('BÁO ĐỘNG: CẠY NẮP HEO!');
    playAudioEffect('siren');
    pushAudit('TAMPER_ALARM', 'Công tắc hành trình Limit Switch phát hiện cạy nắp heo trái phép!', 'ALARM');
    showPopup('error', 'CẢNH BÁO AN NINH: CẠY NẮP HEO!', 'Công tắc hành trình (Limit Switch) phát hiện nắp heo bị mở trái phép! Còi báo động phần cứng đã kích hoạt và hệ thống đã ghi log kiểm toán an ninh.');

    if (activeDevice) {
      try {
        await piggyApi.reportLidTamper(activeDevice.id, true);
      } catch (e: any) {
        console.warn('Lỗi cảnh báo cạy nắp:', e);
      }
    }
  };

  const handleTriggerImpactShock = async () => {
    setHardwareImpactShock(true);
    setPiggyExpression('SHOCKED');
    setOledTicker('BÁO ĐỘNG: RUNG LẮC > 6G!');
    playAudioEffect('siren');
    pushAudit('IMPACT_ALARM', 'Cảm biến gia tốc MPU6050 phát hiện va đập rung lắc vượt ngưỡng 6G!', 'ALARM');
    showPopup('warning', 'CẢNH BÁO VA ĐẬP MẠNH!', 'Cảm biến gia tốc MPU6050 phát hiện lực chấn động vượt ngưỡng 6G! Hệ thống đã tự động khóa an toàn và ghi nhận sự cố an ninh.');

    if (activeDevice) {
      try {
        await piggyApi.reportFatalCrash(activeDevice.id, 6.8);
      } catch (e: any) {
        console.warn('Lỗi cảnh báo va đập:', e);
      }
    }
  };

  const handleUnfreezeHardware = async () => {
    setHardwareTamperLid(false);
    setHardwareImpactShock(false);
    setPiggyExpression('HAPPY');
    setOledTicker('ĐÃ GIẢI TỎA AN NINH');
    playAudioEffect('beep');
    pushAudit('UNFREEZE_HARDWARE', 'Người dùng mở khóa an ninh khẩn cấp cho phần cứng Heo Đất', 'SUCCESS');
    showPopup('success', 'Giải Tỏa An Ninh Thành Công', 'Đã xóa trạng thái cảnh báo an ninh cho Heo Đất. Thiết bị trở lại trạng thái hoạt động bình thường.');

    if (activeDevice) {
      try {
        await piggyApi.unfreezeWallet(activeDevice.id, activeWallet?.id);
      } catch (e: any) {
        console.warn('Lỗi mở khóa an ninh:', e);
      }
    }
  };

  return (
    <div className="space-y-6 pb-12 text-slate-200 font-sans">
      {/* Header Bar */}
      <div className="glass-panel rounded-2xl p-6 border border-slate-800 bg-slate-900/70 shadow-xl flex flex-col md:flex-row justify-between items-start md:items-center gap-4 backdrop-blur-md">
        <div>
          <div className="flex items-center gap-2 mb-1.5 flex-wrap">
            <span className="bg-pink-500/20 text-pink-400 text-[11px] px-2.5 py-0.5 rounded-full font-bold uppercase tracking-wider border border-pink-500/30">
              Menu Demo Nghiệp Vụ Heo Đất Thông Minh
            </span>
            <span className="bg-emerald-500/20 text-emerald-400 text-[11px] px-2.5 py-0.5 rounded-full font-semibold flex items-center gap-1 border border-emerald-500/30">
              <Database className="w-3 h-3 inline" /> 100% API & CSDL MySQL Thật
            </span>
            <span className="bg-cyan-500/20 text-cyan-400 text-[11px] px-2.5 py-0.5 rounded-full font-semibold border border-cyan-500/30">
              Khách Hàng: {currentLoggedUser.username}
            </span>
          </div>
          <h1 className="text-xl md:text-2xl font-extrabold text-white flex items-center gap-2.5">
            <PiggyBank className="w-7 h-7 text-pink-500" /> Mô Phỏng Tương Tác Heo Đất ESP32 & Ngân Hàng Số
          </h1>
          <p className="text-slate-400 text-xs mt-1 leading-relaxed">
            Bên trái là Phần Cứng Heo Đất IoT (Mô phỏng ESP32) &mdash; Bên phải là Ứng Dụng Ngân Hàng Số. Chọn 1 Ví có sẵn của người dùng để ghép đôi với Heo Đất qua mã xác thực OTP gửi về Email thật.
          </p>
        </div>
        <button
          onClick={() => refreshAllState()}
          disabled={isLoading}
          className="bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs px-4 py-2.5 rounded-xl font-semibold flex items-center gap-2 transition shrink-0"
        >
          <RefreshCw className={'w-4 h-4 ' + (isLoading ? 'animate-spin text-pink-400' : '')} /> Đồng Bộ Dữ Liệu
        </button>
      </div>

      {/* MAIN TWO-COLUMN WORKSPACE */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">

        {/* ======================================================== */}
        {/* LEFT COLUMN: HARDWARE ESP32 CONSOLE (5 cols)             */}
        {/* ======================================================== */}
        <div className="lg:col-span-5 space-y-6">
          <div className="rounded-3xl border border-slate-800 bg-slate-900/90 shadow-2xl p-6 relative overflow-hidden backdrop-blur-md">
            {/* Header / Power switch */}
            <div className="flex items-center justify-between border-b border-slate-800 pb-4 mb-5">
              <div className="flex items-center gap-2.5">
                <div className="p-2 rounded-xl bg-pink-500/20 text-pink-400 border border-pink-500/30 relative">
                  <Cpu className="w-5 h-5" />
                  {isRadarPulsing && (
                    <span className="absolute -inset-1 rounded-xl border-2 border-pink-400 animate-ping opacity-75" />
                  )}
                </div>
                <div>
                  <h2 className="text-sm font-black uppercase tracking-wider text-white">
                    Thông tin thiết bị liên kết
                  </h2>
                  <p className="text-[11px] text-slate-400">Bo mạch IoT, Màn hình OLED, Cảm biến TCRT5000 & Khóa Solenoid</p>
                </div>
              </div>
              <button
                onClick={() => setPiggyPower(!piggyPower)}
                className={'px-3 py-1.5 rounded-xl text-xs font-bold flex items-center gap-1.5 transition ' + (piggyPower ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' : 'bg-slate-800 text-slate-500 border border-slate-700')}
              >
                <Power className="w-3.5 h-3.5" /> {piggyPower ? 'BẬT NGUỒN' : 'TẮT NGUỒN'}
              </button>
            </div>

            {/* Piggy Body Container */}
            <div className={'rounded-2xl p-5 border transition-all duration-300 relative overflow-hidden ' + (
              !piggyPower ? 'bg-slate-950/60 border-slate-800 opacity-50' :
              hardwareTamperLid || hardwareImpactShock ? 'bg-amber-950/30 border-amber-500/60' :
              'bg-gradient-to-b from-slate-900 to-slate-950 border-pink-500/30 shadow-inner'
            )}>

              {/* MINI OLED SCREEN (0.96 inch SSD1306 Simulation) */}
              <div className="p-4 rounded-xl bg-black border-2 border-cyan-500/60 shadow-[0_0_15px_rgba(6,182,212,0.15)] font-mono text-cyan-400 space-y-2 relative">
                <div className="flex items-center justify-between text-[10px] border-b border-cyan-900/60 pb-1 text-cyan-300/80">
                  <span className="flex items-center gap-1 font-bold">
                    {piggyPower ? <Wifi className="w-3 h-3 text-emerald-400" /> : <WifiOff className="w-3 h-3 text-slate-500" />}
                    {piggyPower ? 'WIFI: ĐÃ KẾT NỐI' : 'MẤT KẾT NỐI'}
                  </span>
                  <span className="tracking-widest font-black uppercase text-[9px]">OLED SSD1306 (I2C)</span>
                </div>

                <div className="flex items-center justify-between pt-1">
                  <div>
                    <div className="text-[10px] text-cyan-500/80 uppercase">
                      Thiết Bị: {activeDevice ? activeDevice.device_name : broadcastingDevice ? (broadcastingDevice.name + ' (ĐANG PHÁT BEACON)') : 'CHƯA GHÉP ĐÔI'}
                    </div>
                    <div className="text-[9px] text-slate-500 font-mono">
                      MAC: {activeDevice ? activeDevice.mac_address : broadcastingDevice ? broadcastingDevice.mac : '24:6F:28:XX:YY:ZZ'}
                    </div>
                  </div>
                  <div className="text-right">
                    <span className={'text-[10px] font-bold px-1.5 py-0.5 rounded ' + (
                      !activeDevice ? (broadcastingDevice ? 'bg-pink-500/20 text-pink-400 border border-pink-500/40 animate-pulse' : 'bg-slate-800 text-slate-400 border border-slate-700') :
                      solenoidLocked ? 'bg-rose-500/20 text-rose-400 border border-rose-500/40' :
                      'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40'
                    )}>
                      {!activeDevice ? (broadcastingDevice ? 'PHÁT SÓNG BLE' : 'CHƯA GHÉP ĐÔI') : solenoidLocked ? 'KHÓA SOLENOID' : 'MỞ KHÓA 5V'}
                    </span>
                  </div>
                </div>

                {/* OLED Main Balance Display */}
                <div className="py-2 text-center bg-cyan-950/20 rounded border border-cyan-900/40">
                  <div className="text-[10px] text-cyan-400/70 uppercase">
                    {activeDevice ? ('Số Dư Heo Đất (' + (activeWallet?.name || 'Ví Đang Liên Kết') + ')') : 'Số Dư Heo Đất (Chưa Ghép Đôi)'}
                  </div>
                  <div className="text-xl font-black text-cyan-300 tracking-wider">
                    {!piggyPower ? '---' : !activeDevice ? '0 đ' : (((activeDevice.total_coins_dropped ?? activeWallet?.balance) || 0).toLocaleString('vi-VN') + ' đ')}
                  </div>
                </div>

                {/* Expressions & Status Ticker */}
                <div className="flex items-center justify-between text-[11px] pt-1">
                  <span className="flex items-center gap-1">
                    Biểu cảm:
                    {piggyExpression === 'HAPPY' && <span className="text-emerald-400 font-bold">VUI VẺ (ĐÃ ĂN NO)</span>}
                    {piggyExpression === 'HUNGRY' && <span className="text-amber-400 font-bold">ĐÓI BỤNG (CẦN TIẾT KIỆM)</span>}
                    {piggyExpression === 'SICK' && <span className="text-rose-400 font-bold">BỆNH / PIN YẾU</span>}
                    {piggyExpression === 'SHOCKED' && <span className="text-yellow-400 font-bold">BÁO ĐỘNG SỐC!</span>}
                  </span>
                  <span className="text-[10px] text-cyan-200/60 truncate max-w-[150px]">
                    {oledTicker}
                  </span>
                </div>
              </div>

              {/* ===================================================== */}
              {/* REALISTIC VISUAL PIGGY WITH COIN SLOT                 */}
              {/* ===================================================== */}
              <div className="mt-4 p-4 rounded-2xl bg-slate-950/80 border border-slate-800 text-center relative overflow-hidden">
                {/* Visual Radar Rings when Beacon Broadcasts */}
                {isRadarPulsing && (
                  <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                    <span className="absolute w-24 h-24 rounded-full border-2 border-pink-500/60 animate-ping" />
                    <span className="absolute w-40 h-40 rounded-full border-2 border-cyan-400/40 animate-ping delay-200" />
                    <span className="absolute w-56 h-56 rounded-full border-2 border-purple-500/20 animate-ping delay-500" />
                  </div>
                )}

                {/* Piggy Body Illustration */}
                <div className="py-3 flex flex-col items-center justify-center">
                  <div className="relative">
                    <div className={'w-24 h-24 rounded-full flex items-center justify-center shadow-2xl border-2 transition-all ' + (
                      !activeDevice ? 'bg-slate-800 border-slate-700 text-slate-400' :
                      'bg-gradient-to-br from-pink-500 via-rose-500 to-pink-600 border-pink-300 text-white shadow-pink-500/30'
                    )}>
                      <PiggyBank className="w-14 h-14" />
                    </div>

                    {/* Slot Indicator */}
                    <div className="absolute -top-1 left-1/2 -translate-x-1/2 w-10 h-2 bg-black rounded-full border border-pink-300/60 flex items-center justify-center">
                      <span className="w-6 h-0.5 bg-yellow-400/80 rounded" />
                    </div>
                  </div>

                  <div className="mt-2 text-xs font-bold text-slate-300 flex items-center gap-1.5">
                    <span>{activeDevice ? activeDevice.device_name : 'Heo Đất Vật Lý ESP32'}</span>
                    <span className="text-[10px] text-pink-400 font-mono">
                      ({activeDevice ? 'Đã Ghép Đôi' : 'Chưa Ghép Đôi'})
                    </span>
                  </div>
                </div>
              </div>

              {/* Physical Pairing Button (Hardware BLE Beacon) */}
              <div className="mt-5 space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-slate-300 flex items-center gap-1.5">
                    <Radio className="w-4 h-4 text-pink-400" /> Nút Vật Lý Ghép Đôi Phần Cứng
                  </span>
                  <span className="text-[10px] text-slate-500">Giữ 3s để phát sóng BLE/mDNS</span>
                </div>
                <button
                  onClick={handlePressHardwarePair}
                  disabled={!piggyPower || isLoading}
                  className="w-full bg-gradient-to-r from-pink-600 to-purple-600 hover:from-pink-500 hover:to-purple-500 text-white text-xs font-extrabold py-3 px-4 rounded-xl shadow-lg transition flex items-center justify-center gap-2 border border-pink-400/30"
                >
                  <Radio className={'w-4 h-4 ' + (isRadarPulsing ? 'animate-ping' : 'animate-pulse')} /> Tìm thiết bị liên kết
                </button>
                <p className="text-[11px] text-slate-400 leading-relaxed">
                  Mô phỏng thao tác bấm giữ nút vật lý trên vỏ heo. ESP32 sẽ sinh mã ngẫu nhiên, phát sóng radar BLE và gửi gói tin Beacon mDNS/BLE sang Bảng Phải (Ứng Dụng).
                </p>
              </div>

              {/* Physical Coin Slot & TCRT5000 Optical Sensor */}
              <div className="mt-6 pt-5 border-t border-slate-800 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-slate-300 flex items-center gap-1.5">
                    <DollarSign className="w-4 h-4 text-emerald-400" /> Khe Nhét Tiền & Cảm Biến Quang TCRT5000
                  </span>
                  <span className="text-[10px] font-mono text-emerald-400">Độ Trễ Khử Rung: 600ms</span>
                </div>

                <div className="grid grid-cols-3 gap-2">
                  {[10000, 20000, 50000, 100000, 200000, 500000].map(val => (
                    <button
                      key={val}
                      onClick={() => setSelectedCoin(val)}
                      className={'py-2 rounded-lg text-xs font-bold border transition ' + (
                        selectedCoin === val
                          ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/60 shadow-md shadow-emerald-500/10'
                          : 'bg-slate-800 text-slate-400 border-slate-700 hover:bg-slate-700'
                      )}
                    >
                      {val.toLocaleString('vi-VN')} đ
                    </button>
                  ))}
                </div>

                <button
                  onClick={() => handleOpenDepositOtpModal(selectedCoin)}
                  disabled={!piggyPower || isCoinDropping || isLoading || !activeDevice}
                  className={'w-full py-3 rounded-xl shadow-lg transition flex items-center justify-center gap-2 text-xs font-black border ' + (
                    !activeDevice
                      ? 'bg-slate-800 text-slate-500 border-slate-700 cursor-not-allowed'
                      : 'bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white border-emerald-400/30'
                  )}
                >
                  <DollarSign className={'w-4 h-4 ' + (isCoinDropping ? 'animate-bounce' : '')} />
                  {!activeDevice ? 'Vui Lòng Liên Kết Heo Đất Trước Khi Nhét Tiền' : isCoinDropping ? 'Đang Nhét Tiền Chui Vào Bụng Heo...' : ('CHO HEO ĂN NÈ')}
                </button>
                <p className="text-[11px] text-slate-400 leading-relaxed">
                  Nhập mã OTP để xác nhận nạp tiền. Sau khi xác thực, tờ tiền polymer sẽ trượt thẳng vào khe heo kèm hiệu ứng âm thanh keng keng, cảm biến TCRT5000 ghi nhận số dư tức thì vào CSDL thật.
                </p>
              </div>

              {/* Hardware Security Alerts Simulation */}
              <div className="mt-6 pt-5 border-t border-slate-800 space-y-3">
                <span className="text-xs font-bold text-slate-300 flex items-center gap-1.5">
                  <ShieldAlert className="w-4 h-4 text-amber-400" /> Mô Phỏng Sự Cố & Cảnh Báo An Ninh
                </span>
                <div className="grid grid-cols-2 gap-2">
                  <button
                    onClick={handleTriggerLidTamper}
                    className="p-2.5 rounded-xl border border-rose-500/30 bg-rose-950/30 hover:bg-rose-900/40 text-rose-300 text-xs font-bold flex items-center justify-center gap-1.5 transition"
                  >
                    <ShieldAlert className="w-3.5 h-3.5 text-rose-400" /> Cạy Nắp (Limit Switch)
                  </button>
                  <button
                    onClick={handleTriggerImpactShock}
                    className="p-2.5 rounded-xl border border-amber-500/30 bg-amber-950/30 hover:bg-amber-900/40 text-amber-300 text-xs font-bold flex items-center justify-center gap-1.5 transition"
                  >
                    <AlertTriangle className="w-3.5 h-3.5 text-amber-400" /> Va Đập (&gt;6G MPU6050)
                  </button>
                </div>
                {(hardwareTamperLid || hardwareImpactShock) && (
                  <button
                    onClick={handleUnfreezeHardware}
                    className="w-full bg-slate-800 hover:bg-slate-700 text-cyan-300 text-xs font-bold py-2 rounded-xl border border-cyan-500/40 flex items-center justify-center gap-1.5 transition"
                  >
                    <Unlock className="w-3.5 h-3.5 text-cyan-400" /> Mở Khóa Khẩn Cấp (Xóa Trạng Thái Báo Động)
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* ======================================================== */}
        {/* RIGHT COLUMN: FINTECH BANKING APP CONSOLE (7 cols)       */}
        {/* ======================================================== */}
        <div className="lg:col-span-7 space-y-6">

          {/* User Profile Card */}
          <div className="p-5 rounded-2xl border border-slate-800 bg-slate-900 shadow-xl flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-cyan-500/20 text-cyan-400 flex items-center justify-center font-bold text-sm border border-cyan-500/30 uppercase">
                {(currentLoggedUser.fullName ? currentLoggedUser.fullName.charAt(0) : (currentLoggedUser.username ? currentLoggedUser.username.charAt(0) : 'U'))}
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h3 className="text-sm font-bold text-white">{currentLoggedUser.fullName}</h3>
                  <span className="text-[10px] bg-emerald-500/20 text-emerald-400 px-2 py-0.5 rounded-full font-semibold border border-emerald-500/30">
                    Đang Hoạt Động (Active)
                  </span>
                </div>
                <p className="text-xs text-slate-400 mt-0.5">
                  Tài khoản: <span className="font-mono text-slate-300">{currentLoggedUser.username}</span> | Email: <span className="font-mono text-cyan-400">{currentLoggedUser.email}</span>
                </p>
              </div>
            </div>
            <div className="text-right hidden sm:block">
              <span className="text-[11px] text-slate-500 block">Phân hệ dịch vụ</span>
              <span className="text-xs font-bold text-pink-400">Ngân Hàng Bán Lẻ (Retail)</span>
            </div>
          </div>

          {/* WALLET SELECTION CARD */}
          <div className="p-6 rounded-3xl border border-slate-800 bg-slate-900 shadow-xl space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Wallet className="w-5 h-5 text-cyan-400" />
                <h3 className="text-sm font-black uppercase tracking-wider text-white">
                  Chọn Ví Tài Chính Để Liên Kết Heo Đất
                </h3>
              </div>
              <span className="text-[10px] text-slate-500">Quản lý tại menu 'Ví Tiền'</span>
            </div>

            <p className="text-xs text-slate-400 leading-relaxed">
              Các Ví tài chính được tạo và quản lý độc lập tại menu 'Quản lý Ví'. Tại màn hình này, bạn chọn 1 Ví tài chính đang hoạt động của mình để liên kết với Heo Đất Thông Minh:
            </p>

            {/* Wallet Select Dropdown */}
            <div className="space-y-2">
              <select
                value={selectedWalletId}
                onChange={e => handleSelectWallet(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 hover:border-slate-700 text-white rounded-xl px-4 py-3 text-xs font-bold focus:outline-none focus:border-cyan-500 transition"
              >
                {walletsList.map(w => (
                  <option key={w.id} value={w.id}>
                    {w.name} &mdash; Số TK: {w.wallet_account} (Số dư: {w.balance.toLocaleString('vi-VN')} đ) &mdash; [{w.status}]
                  </option>
                ))}
              </select>
            </div>

            {/* Selected Wallet Details Box */}
            {activeWallet && (
              <div className="p-4 rounded-2xl bg-slate-950/80 border border-slate-800/80 space-y-3">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-800 pb-3">
                  <div>
                    <span className="text-[10px] text-slate-500 uppercase tracking-wider block">Ví Đang Chọn</span>
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-bold text-white tracking-wide">{activeWallet.name} ({activeWallet.wallet_account})</span>
                      <span className={'text-[10px] font-bold px-2 py-0.5 rounded-full border ' + (
                        activeWallet.status === 'ACTIVE' ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30' :
                        activeWallet.status === 'FROZEN' ? 'bg-rose-500/20 text-rose-400 border-rose-500/30' :
                        'bg-amber-500/20 text-amber-400 border-amber-500/30'
                      )}>
                        {activeWallet.status}
                      </span>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    {activeWallet.status === 'FROZEN' && (
                      <button
                        onClick={() => handleQuickUnfreeze(activeWallet.id)}
                        disabled={isLoading}
                        className="bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold px-3 py-1.5 rounded-lg shadow transition flex items-center gap-1"
                      >
                        <Unlock className="w-3.5 h-3.5" /> Mở Khóa Ví Ngay
                      </button>
                    )}
                    {activeWallet.status === 'PENDING_ACTIVATION' && (
                      <button
                        onClick={() => handleQuickActivate(activeWallet.id)}
                        disabled={isLoading}
                        className="bg-amber-600 hover:bg-amber-500 text-black text-xs font-bold px-3 py-1.5 rounded-lg shadow transition flex items-center gap-1"
                      >
                        <CheckCircle className="w-3.5 h-3.5" /> Kích Hoạt Ví Ngay
                      </button>
                    )}
                    <div>
                      <span className="text-[10px] text-slate-500 uppercase tracking-wider block">Số Dư Khả Dụng</span>
                      <span className="text-lg font-mono font-extrabold text-emerald-400">
                        {(activeWallet.balance || 0).toLocaleString('vi-VN')} đ
                      </span>
                    </div>
                  </div>
                </div>

                {/* Pairing status of this wallet */}
                {activeDevice ? (
                  <div className="p-3 rounded-xl bg-emerald-950/30 border border-emerald-500/40 flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                    <div className="space-y-0.5">
                      <div className="text-xs font-bold text-emerald-300 flex items-center gap-1.5">
                        <CheckCircle className="w-4 h-4 text-emerald-400" />
                        Đang liên kết với: {activeDevice.device_name}
                      </div>
                      <div className="text-[10px] font-mono text-slate-400">
                        MAC: {activeDevice.mac_address} | Trạng thái: {activeDevice.status}
                      </div>
                    </div>
                    <button
                      onClick={handleRequestUnbindDevice}
                      disabled={isLoading}
                      className="bg-rose-950/60 hover:bg-rose-900/60 text-rose-300 border border-rose-500/40 text-xs font-bold px-3.5 py-1.5 rounded-lg transition flex items-center gap-1.5 self-start sm:self-auto"
                    >
                      <Unlink className="w-3.5 h-3.5" /> Hủy Liên Kết
                    </button>
                  </div>
                ) : (
                  <div className="p-3 rounded-xl bg-cyan-950/20 border border-cyan-500/30 text-xs text-cyan-300 flex items-center gap-2">
                    <Info className="w-4 h-4 text-cyan-400 shrink-0" />
                    Ví này chưa Liên kết thiết bị. Vui lòng chọn một Heo Đất từ danh sách phát hiện bên dưới và bấm 'Liên kết thiết bị'.
                  </div>
                )}
              </div>
            )}
          </div>

          {/* ======================================================== */}
          {/* DISCOVERED DEVICES & PAIRING WITH SINGLE DEVICE CONSTRAINT */}
          {/* ======================================================== */}
          <div className="p-6 rounded-3xl border border-slate-800 bg-slate-900 shadow-xl space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Radio className="w-5 h-5 text-pink-400" />
                <h3 className="text-sm font-black uppercase tracking-wider text-white">
                  Danh sách thiết bị liên kết
                </h3>
              </div>
              <span className="text-[10px] text-slate-500">Quét BLE/mDNS Tự Động</span>
            </div>

            <p className="text-xs text-slate-400 leading-relaxed">
              Bấm nút <span className="text-pink-400 font-bold">'Liên kết thiết bị'</span> để hệ thống Core gửi mã OTP 6 số đến Email của bạn (<span className="text-cyan-400 font-mono">{currentLoggedUser.email}</span>). <span className="text-amber-400 font-semibold">Chỉ được liên kết 1 Heo Đất duy nhất tại một thời điểm; các thiết bị còn lại sẽ tự động bị vô hiệu hóa!</span>
            </p>

            {/* Devices List */}
            <div className="space-y-2.5">
              {discoveredDevices.length === 0 ? (
                <p className="text-xs text-slate-500 text-center py-4">Chưa phát hiện thiết bị Heo Đất nào đang phát sóng BLE.</p>
              ) : (
                discoveredDevices.map(dev => {
                  const isCurrentlyPairedWithThisWallet = activeDevice && (activeDevice.mac_address === dev.mac || activeDevice.id === dev.id);
                  const isAnotherDevicePaired = activeDevice && !isCurrentlyPairedWithThisWallet;
                  const isThisDeviceLoading = pairLoadingDeviceId === dev.id;

                  return (
                    <div
                      key={dev.id}
                      className={'p-3.5 rounded-xl border transition flex flex-col sm:flex-row sm:items-center justify-between gap-3 ' + (
                        isCurrentlyPairedWithThisWallet ? 'border-emerald-500/50 bg-emerald-950/20 shadow-md shadow-emerald-500/10' :
                        isAnotherDevicePaired ? 'border-slate-800/60 bg-slate-950/40 opacity-50' :
                        'border-slate-800 bg-slate-950 hover:border-slate-700'
                      )}
                    >
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="text-xs font-bold text-white">{dev.name}</span>
                          <span className="text-[9px] bg-pink-500/20 text-pink-400 px-1.5 py-0.5 rounded font-mono">
                            {dev.signal} dBm
                          </span>
                        </div>
                        <div className="text-[10px] text-slate-500 font-mono">
                          MAC: {dev.mac} | FW: {dev.firmware}
                        </div>
                      </div>

                      {isCurrentlyPairedWithThisWallet ? (
                        <div className="flex items-center gap-2">
                          <span className="text-xs font-bold text-emerald-400 bg-emerald-500/20 px-3 py-1.5 rounded-xl border border-emerald-500/30 flex items-center gap-1">
                            <Check className="w-3.5 h-3.5" /> Đang liên kết
                          </span>
                        </div>
                      ) : isAnotherDevicePaired ? (
                        <button
                          disabled={true}
                          className="px-3.5 py-2 rounded-xl text-xs font-bold bg-slate-800/80 text-slate-500 cursor-not-allowed border border-slate-700/50 flex items-center gap-1.5"
                        >
                          <Lock className="w-3.5 h-3.5 text-slate-500" /> Vô Hiệu Hóa (Đã Có Thiết Bị Khác)
                        </button>
                      ) : (
                        <button
                          onClick={() => handleRequestPairOtp(dev)}
                          disabled={isLoading || isThisDeviceLoading || !activeWallet}
                          className={'px-3.5 py-2 rounded-xl text-xs font-extrabold flex items-center gap-1.5 transition ' + (
                            !activeWallet
                              ? 'bg-slate-800 text-slate-500 cursor-not-allowed' :
                            activeWallet.status === 'FROZEN'
                              ? 'bg-gradient-to-r from-rose-600 to-red-600 hover:from-rose-500 hover:to-red-500 text-white shadow-md' :
                            activeWallet.status === 'PENDING_ACTIVATION'
                              ? 'bg-gradient-to-r from-amber-600 to-yellow-600 hover:from-amber-500 hover:to-yellow-500 text-black shadow-md' :
                              'bg-gradient-to-r from-pink-600 to-purple-600 hover:from-pink-500 hover:to-purple-500 text-white shadow-md'
                          )}
                        >
                          {isThisDeviceLoading ? (
                            <>
                              <RefreshCw className="w-3.5 h-3.5 animate-spin" /> Đang Yêu Cầu OTP...
                            </>
                          ) : (
                            <>
                              <Mail className="w-3.5 h-3.5" />
                              {!activeWallet ? 'Chưa Chọn Ví' :
                               activeWallet.status === 'FROZEN' ? 'Mở Khóa Ví & Gửi OTP' :
                               activeWallet.status === 'PENDING_ACTIVATION' ? 'Kích Hoạt Ví & Gửi OTP' :
                               'Liên kết thiết bị'}
                            </>
                          )}
                        </button>
                      )}
                    </div>
                  );
                })
              )}
            </div>
          </div>

          {/* 4 FINANCIAL BUCKETS / SUB-POTS CARD */}
          <div className="p-6 rounded-3xl border border-slate-800 bg-slate-900 shadow-xl space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Layers className="w-5 h-5 text-emerald-400" />
                <h3 className="text-sm font-black uppercase tracking-wider text-white">
                  Hủ chi tiêu
                </h3>
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setShowTransferModal(true)}
                  disabled={!activeDevice || buckets.length < 2 || isLoading}
                  className="bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs px-3 py-1.5 rounded-lg font-semibold flex items-center gap-1.5 transition"
                >
                  <ArrowDownUp className="w-3.5 h-3.5 text-emerald-400" /> Điều Chuyển Hũ
                </button>
                <button
                  onClick={handleSimulateBankWithdraw}
                  className="bg-rose-950/40 hover:bg-rose-900/50 text-rose-300 border border-rose-500/30 text-xs px-3 py-1.5 rounded-lg font-semibold flex items-center gap-1.5 transition"
                >
                  Thử Rút Ra Ngân Hàng
                </button>
              </div>
            </div>

            {/* Buckets Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {buckets.length > 0 ? (
                buckets.map(b => (
                  <div key={b.id} className="p-3.5 rounded-xl border border-slate-800 bg-slate-950 space-y-1.5">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-bold text-white">{b.goal_name}</span>
                      <span className="text-[10px] text-slate-400 font-mono">
                        Mục tiêu: {(b.target_amount || 0).toLocaleString('vi-VN')} đ
                      </span>
                    </div>
                    <div className="flex items-center justify-between text-xs">
                      <span className="text-slate-400">Đã tích lũy:</span>
                      <span className="font-mono font-bold text-emerald-400">
                        {(b.current_amount || 0).toLocaleString('vi-VN')} đ
                      </span>
                    </div>
                    <div className="w-full bg-slate-800 h-1.5 rounded-full overflow-hidden">
                      <div
                        className="bg-emerald-500 h-full rounded-full transition-all duration-500"
                        style={{
                          width: Math.min(100, Math.round(((b.current_amount || 0) / (b.target_amount || 1)) * 100)) + '%'
                        }}
                      />
                    </div>
                  </div>
                ))
              ) : (
                <div className="col-span-2 py-4 text-center text-xs text-slate-500 bg-slate-950/50 rounded-xl border border-slate-800">
                  4 Hũ Tài Chính sẽ được hệ thống tự động tải khi bạn chọn Ví đã liên kết với Heo Đất.
                </div>
              )}
            </div>
          </div>

          {/* SMASH PIGGY & AI HEALTH ROW */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Smash Piggy Box */}
            <div className="p-5 rounded-2xl border border-rose-500/30 bg-gradient-to-b from-slate-900 to-rose-950/30 shadow-xl space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-rose-300 uppercase tracking-wider flex items-center gap-1.5">
                  <Hammer className="w-4 h-4 text-rose-400" /> Lễ Đập Heo Tất Toán
                </span>
                <span className="text-xs font-mono font-bold text-white">
                  {(activeWallet?.balance || 0).toLocaleString('vi-VN')} đ
                </span>
              </div>
              <p className="text-[11px] text-slate-400 leading-relaxed">
                Mở sân khấu hoạt cảnh lớn: Cây búa khổng lồ đập vỡ Heo Đất kèm âm thanh đập vỡ &rarr; Xác nhận Smart OTP &rarr; Tất toán toàn bộ số dư về 0 đ &rarr; Mở chốt Solenoid 5V.
              </p>
              <button
                onClick={handleOpenSmashTheater}
                disabled={isLoading || !activeDevice}
                className={'w-full py-3 rounded-xl shadow-lg transition flex items-center justify-center gap-2 text-xs font-black ' + (
                  !activeDevice
                    ? 'bg-slate-800 text-slate-500 cursor-not-allowed border border-slate-700'
                    : 'bg-gradient-to-r from-rose-600 via-red-600 to-amber-600 hover:from-rose-500 hover:to-amber-500 text-white shadow-rose-600/30 border border-rose-400/40'
                )}
              >
                <Hammer className="w-4 h-4 animate-bounce" />
                {!activeDevice ? 'Chưa Có Heo Đất Để Đập Tất Toán' : 'ĐẬP HEO'}
              </button>
            </div>

            {/* AI Health & Mode Y/N Box */}
            <div className="p-5 rounded-2xl border border-slate-800 bg-slate-900 shadow-xl space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-cyan-400 uppercase tracking-wider flex items-center gap-1.5">
                  <Heart className="w-4 h-4 text-rose-400" /> AI Đánh Giá Sức Khỏe Heo
                </span>
                <span className={'text-xs font-bold px-2 py-0.5 rounded ' + (healthScore < 20 ? 'bg-rose-500/20 text-rose-400' : 'bg-emerald-500/20 text-emerald-400')}>
                  {healthScore}% ({healthStatus})
                </span>
              </div>
              <p className="text-[11px] text-slate-400 leading-relaxed">
                Khi sức khỏe dưới 20%, heo sẽ tự động khóa và đòi nạp tiền. Có thể chuyển đổi giữa Chế độ AI Định kỳ và Chế độ Vui nhộn.
              </p>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setNotificationMode(prev => prev === 'MODE_Y' ? 'MODE_N' : 'MODE_Y')}
                  className={'w-full py-2 rounded-xl text-xs font-bold border transition ' + (
                    notificationMode === 'MODE_N'
                      ? 'bg-amber-500/20 text-amber-300 border-amber-500/40'
                      : 'bg-cyan-500/20 text-cyan-300 border-cyan-500/40'
                  )}
                >
                  Thông Báo: {notificationMode === 'MODE_Y' ? 'Mode Y (AI Định Kỳ)' : 'Mode N (Vui Nhộn 7s)'}
                </button>
              </div>
            </div>
          </div>

          {/* REAL-TIME MYSQL AUDIT LOG TERMINAL */}
          <div className="p-5 rounded-2xl border border-slate-800 bg-slate-950 shadow-2xl space-y-3">
            <div className="flex items-center justify-between border-b border-slate-800 pb-2">
              <div className="flex items-center gap-2">
                <Terminal className="w-4 h-4 text-pink-400" />
                <span className="text-xs font-black uppercase tracking-wider text-slate-300">
                  Nhật Ký Hệ Thống
                </span>
              </div>
              <span className="text-[10px] font-mono text-slate-500">{auditLogs.length} sự kiện đã lưu</span>
            </div>
            <div className="space-y-1.5 max-h-56 overflow-y-auto font-mono text-[11px] pr-1">
              {auditLogs.map(l => (
                <div key={l.id} className="p-2 rounded bg-slate-900/80 border border-slate-800/80 flex items-start justify-between gap-3">
                  <div className="space-y-0.5">
                    <div className="flex items-center gap-2">
                      <span className="text-slate-500 text-[10px]">{l.time}</span>
                      <span className="font-bold text-cyan-400 text-[10px]">{l.action}</span>
                    </div>
                    <div className="text-slate-300 text-xs">{l.detail}</div>
                  </div>
                  <span className={'text-[9px] font-bold px-1.5 py-0.5 rounded shrink-0 ' + (
                    l.status === 'SUCCESS' ? 'bg-emerald-500/20 text-emerald-400' :
                    l.status === 'ALARM' ? 'bg-rose-500/20 text-rose-400 animate-pulse' :
                    l.status === 'PENDING' ? 'bg-amber-500/20 text-amber-400' :
                    'bg-slate-700 text-slate-300'
                  )}>
                    {l.status}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* ========================================================================= */}
      {/* 🌟 1. CINEMATIC FULLSCREEN SMASH THEATER OVERLAY                         */}
      {/* ========================================================================= */}
      {smashTheaterOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/95 backdrop-blur-2xl animate-in fade-in duration-300">
          <div className="w-full max-w-2xl rounded-3xl p-8 border border-rose-500/40 bg-gradient-to-b from-slate-900 via-slate-950 to-black shadow-2xl relative overflow-hidden text-center space-y-6">

            {/* Close Theater Button */}
            <button
              onClick={() => setSmashTheaterOpen(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-white p-2 rounded-full hover:bg-slate-800 transition"
            >
              <X className="w-6 h-6" />
            </button>

            {/* Title Header */}
            <div>
              <span className="text-xs font-bold uppercase tracking-widest text-rose-400 px-3 py-1 rounded-full bg-rose-500/20 border border-rose-500/30">
                LỄ ĐẬP HEO TẤT TOÁN SỔ CÁI TÀI CHÍNH
              </span>
              <h2 className="text-2xl md:text-3xl font-black text-white mt-2">
                {smashStep === 'AIMING' && 'Chuẩn Bị Búa Thần Tài...'}
                {smashStep === 'STRIKING' && 'Vung Búa Giáng Xuống!'}
                {smashStep === 'SHATTERED' && 'BÙM! HEO ĐẤT ĐÃ VỠ VỤN!'}
                {smashStep === 'OTP_INPUT' && 'Xác Thực Smart OTP Tất Toán'}
              </h2>
              <p className="text-xs text-slate-400 mt-1">
                Số dư tích lũy cần tất toán: <span className="text-emerald-400 font-mono font-bold text-sm">{(((activeDevice?.total_coins_dropped ?? activeWallet?.balance) || 0)).toLocaleString('vi-VN')} đ</span>
              </p>
            </div>

            {/* ===================================================== */}
            {/* THEATRICAL PIGGY & HAMMER VISUAL STAGE                */}
            {/* ===================================================== */}
            <div className="h-64 relative flex items-center justify-center bg-slate-950/80 rounded-2xl border border-slate-800 overflow-hidden shadow-inner">
              {/* Radial Spotlight */}
              <div className="absolute inset-0 bg-radial from-rose-500/10 via-transparent to-transparent pointer-events-none" />

              {/* STAGE A: PIGGY INTACT OR SHATTERED */}
              {smashStep !== 'SHATTERED' && smashStep !== 'OTP_INPUT' ? (
                <div className="relative">
                  {/* Intact Ceramic Piggy */}
                  <div className="w-36 h-36 rounded-full bg-gradient-to-br from-pink-400 via-rose-500 to-pink-600 border-4 border-pink-200 shadow-[0_0_50px_rgba(244,63,94,0.4)] flex items-center justify-center text-white relative transition-transform duration-200">
                    <PiggyBank className="w-24 h-24 text-white drop-shadow-lg" />
                    {/* Coin slot with gold glow */}
                    <div className="absolute -top-1 left-1/2 -translate-x-1/2 w-14 h-3 bg-black rounded-full border-2 border-yellow-300 shadow-[0_0_10px_rgba(250,204,21,0.8)]" />
                  </div>
                </div>
              ) : (
                /* STAGE B: SHATTERED PIECES & FLYING COIN EXPLOSION */
                <div className="relative w-full h-full flex items-center justify-center animate-in zoom-in-50 duration-300">
                  {/* Flying Shard 1 (Top Left) */}
                  <div className="absolute -top-2 left-16 w-16 h-16 rounded-tl-full bg-rose-600/80 border-2 border-pink-300 -rotate-45 shadow-lg animate-bounce" />
                  {/* Flying Shard 2 (Top Right) */}
                  <div className="absolute -top-4 right-16 w-14 h-14 rounded-tr-full bg-pink-500/80 border-2 border-pink-200 rotate-45 shadow-lg animate-bounce delay-100" />
                  {/* Flying Shard 3 (Bottom Left) */}
                  <div className="absolute bottom-2 left-20 w-16 h-12 rounded-bl-full bg-rose-700/80 border-2 border-rose-300 -rotate-12 shadow-lg" />
                  {/* Flying Shard 4 (Bottom Right) */}
                  <div className="absolute bottom-3 right-20 w-16 h-14 rounded-br-full bg-pink-600/80 border-2 border-pink-300 rotate-12 shadow-lg" />

                  {/* Golden Coin Shower Particles */}
                  <div className="text-3xl animate-bounce delay-75">🪙</div>
                  <div className="text-4xl animate-bounce delay-150 absolute top-8 left-1/3">💰</div>
                  <div className="text-3xl animate-bounce delay-200 absolute bottom-10 right-1/3">🪙</div>
                  <div className="text-2xl animate-bounce delay-300 absolute top-12 right-1/4">✨</div>
                  <div className="text-2xl animate-bounce delay-100 absolute bottom-8 left-1/4">💵</div>

                  <div className="z-10 text-center">
                    <span className="text-4xl">💥</span>
                    <div className="text-rose-400 font-mono text-xs font-black uppercase tracking-widest mt-1">
                      KHO BÁU ĐÃ MỞ BUNG NẮP!
                    </div>
                  </div>
                </div>
              )}

              {/* THE HAMMER ANIMATION */}
              {smashStep === 'AIMING' && (
                <div className="absolute -top-2 right-1/4 text-6xl transform -rotate-45 transition-transform duration-500">
                  🔨
                </div>
              )}
              {smashStep === 'STRIKING' && (
                <div className="absolute top-8 left-1/2 -translate-x-1/2 text-7xl transform rotate-12 scale-125 transition-transform duration-200">
                  🔨💥
                </div>
              )}
            </div>

            {/* STAGE C: REVEAL OTP CONFIRMATION CARD */}
            {smashStep === 'OTP_INPUT' ? (
              <div className="p-5 rounded-2xl bg-slate-900 border border-rose-500/50 space-y-3 max-w-md mx-auto animate-in fade-in slide-in-from-bottom-4 duration-300">
                <div className="text-xs text-slate-300 leading-relaxed">
                  Nhập mã Smart OTP 6 số để kích hoạt lệnh tất toán toàn bộ số dư và mở khóa chốt Solenoid 5V:
                </div>
                <input
                  type="text"
                  value={smashOtpInput}
                  onChange={e => setSmashOtpInput(e.target.value)}
                  placeholder="Nhập 6 số Smart OTP..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-center text-2xl font-mono tracking-widest text-rose-400 font-bold focus:outline-none focus:border-rose-500"
                  maxLength={6}
                  autoFocus
                />
                <div className="flex items-center justify-between text-xs pt-1">
                  <span className="text-slate-500 text-[11px]">Mã OTP Demo: <span className="font-mono text-rose-400 font-bold">123456</span></span>
                  <button
                    onClick={() => setSmashOtpInput('123456')}
                    className="text-cyan-400 hover:text-cyan-300 font-semibold text-[11px] underline"
                  >
                    Điền nhanh 123456
                  </button>
                </div>
                <div className="flex items-center gap-3 pt-2">
                  <button
                    onClick={() => setSmashTheaterOpen(false)}
                    className="w-1/3 py-3 rounded-xl text-xs font-bold bg-slate-800 hover:bg-slate-700 text-slate-300"
                  >
                    Đóng
                  </button>
                  <button
                    onClick={handleConfirmSmashInTheater}
                    disabled={isLoading || !smashOtpInput.trim()}
                    className="w-2/3 py-3 rounded-xl text-xs font-extrabold bg-gradient-to-r from-rose-600 via-red-600 to-amber-600 hover:from-rose-500 hover:to-amber-500 text-white shadow-lg"
                  >
                    Xác Nhận Tất Toán Toàn Bộ
                  </button>
                </div>
              </div>
            ) : (
              <div className="text-xs text-slate-500">
                Hệ thống đang mô phỏng quá trình đập vỡ cơ học bằng búa thần tài...
              </div>
            )}
          </div>
        </div>
      )}

      {/* ========================================================================= */}
      {/* 🌟 2. CINEMATIC BANKNOTE FEEDING OVERLAY (Real Polymer Bill Slide-in)     */}
      {/* ========================================================================= */}
      {feedingStageOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/90 backdrop-blur-md animate-in fade-in duration-200">
          <div className="w-full max-w-md rounded-3xl p-6 border border-emerald-500/50 bg-slate-900 shadow-2xl text-center space-y-4">
            <div>
              <span className="text-[10px] font-bold uppercase tracking-widest text-emerald-400 px-3 py-1 rounded-full bg-emerald-500/20 border border-emerald-500/30">
                CẢM BIẾN QUANG HỌC TCRT5000 NHẬN DIỆN TIỀN
              </span>
              <h3 className="text-xl font-black text-white mt-2">
                {feedingStep === 'APPROACHING' && 'Tiền Polymer Đang Đưa Vào Khe...'}
                {feedingStep === 'SWALLOWING' && 'Heo Đang Nuốt Tiền Vào Bụng!'}
                {feedingStep === 'DIGESTED' && 'Nạp Tiền Thành Công!'}
              </h3>
            </div>

            {/* Visual Banknote Feeding Container */}
            <div className="h-60 relative flex flex-col items-center justify-center bg-slate-950 rounded-2xl border border-slate-800 overflow-hidden">
              {/* Realistic Polymer Banknote Illustration */}
              <div className={'w-52 h-24 rounded-lg bg-gradient-to-r from-emerald-600 via-teal-500 to-emerald-700 border-2 border-emerald-300 shadow-2xl p-2.5 text-left text-white transition-all duration-700 relative overflow-hidden ' + (
                feedingStep === 'APPROACHING' ? 'translate-y-0 opacity-100 rotate-2' :
                feedingStep === 'SWALLOWING' ? 'translate-y-16 scale-75 opacity-70' :
                'translate-y-28 scale-50 opacity-0'
              )}>
                {/* Banknote Microprint & Emblem */}
                <div className="flex justify-between items-start text-[8px] tracking-widest font-mono text-emerald-200">
                  <span>CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM</span>
                  <span>50000</span>
                </div>
                <div className="mt-1 flex items-center justify-between">
                  <div className="text-base font-extrabold font-mono tracking-wider text-yellow-300">
                    {feedingAmount.toLocaleString('vi-VN')} Đ
                  </div>
                  {/* Polymer clear window watermark */}
                  <div className="w-8 h-8 rounded-full bg-emerald-300/30 border border-emerald-200 flex items-center justify-center text-[9px] font-bold">
                    ★
                  </div>
                </div>
                <div className="text-[7px] text-emerald-100 font-mono mt-0.5">
                  NGÂN HÀNG NHÀ NƯỚC VIỆT NAM
                </div>
              </div>

              {/* Piggy Face & Mouth Slot Below */}
              <div className="mt-4 relative flex flex-col items-center">
                {/* Slot with yellow glow */}
                <div className="w-20 h-2 bg-black rounded-full border-2 border-emerald-400 shadow-[0_0_15px_rgba(16,185,129,0.8)]" />
                <div className="text-3xl mt-1">
                  {feedingStep === 'DIGESTED' ? '😋' : '🐷'}
                </div>
              </div>
            </div>

            <div className="text-emerald-400 font-mono text-xs font-bold">
              +{feedingAmount.toLocaleString('vi-VN')} đ đang nạp vào CSDL MySQL thật...
            </div>
          </div>
        </div>
      )}

      {/* ======================================================== */}
      {/* MODAL: PAIR DEVICE WITH REAL EMAIL OTP                   */}
      {/* ======================================================== */}
      {pairModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-in fade-in">
          <div className="w-full max-w-sm rounded-3xl p-6 border border-pink-500/40 bg-slate-900 shadow-2xl space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-extrabold text-white flex items-center gap-2">
                <Radio className="w-4 h-4 text-pink-400" /> Xác Thực Ghép Đôi Thiết Bị
              </h3>
              <button onClick={() => setPairModalOpen(false)} className="text-slate-400 hover:text-white">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="p-3.5 rounded-xl bg-slate-950 border border-slate-800 text-xs space-y-1.5">
              <div className="text-white font-bold">{selectedPairDevice?.name}</div>
              <div className="text-slate-400 font-mono text-[11px]">MAC: {selectedPairDevice?.mac}</div>
              <div className="text-emerald-400 font-bold text-[11px]">
                Liên kết tới Ví: {activeWallet?.name} ({activeWallet?.wallet_account})
              </div>
            </div>

            <div className="p-3 rounded-xl bg-pink-950/20 border border-pink-500/30 space-y-1">
              <span className="text-[11px] text-pink-300 font-semibold block flex items-center gap-1">
                <Mail className="w-3.5 h-3.5 text-pink-400" /> Đã gửi mã OTP tới Email:
              </span>
              <span className="text-xs font-mono font-bold text-white">{otpSentEmail}</span>
              {devBypassHint && (
                <div className="text-[10px] text-slate-400 pt-1">
                  Mã OTP từ Core Service: <span className="font-mono text-cyan-400 font-bold">{devBypassHint}</span>
                </div>
              )}
            </div>

            <p className="text-xs text-slate-400 leading-relaxed">
              Vui lòng kiểm tra email và nhập mã xác thực OTP 6 số để kích hoạt liên kết duy nhất:
            </p>

            <input
              type="text"
              value={pairOtpInput}
              onChange={e => setPairOtpInput(e.target.value)}
              placeholder="Nhập 6 số OTP..."
              className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2.5 text-center text-xl font-mono tracking-widest text-pink-400 font-bold focus:outline-none focus:border-pink-500"
              maxLength={6}
              autoFocus
            />

            <div className="flex items-center gap-2 pt-2">
              <button
                onClick={() => setPairModalOpen(false)}
                className="w-1/2 py-2.5 rounded-xl text-xs font-bold bg-slate-800 hover:bg-slate-700 text-slate-300"
              >
                Hủy Bỏ
              </button>
              <button
                onClick={handleConfirmPairDevice}
                disabled={isLoading || !pairOtpInput.trim()}
                className="w-1/2 py-2.5 rounded-xl text-xs font-extrabold bg-gradient-to-r from-pink-600 to-purple-600 hover:from-pink-500 hover:to-purple-500 text-white shadow-lg"
              >
                Xác Nhận Ghép Đôi
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ======================================================== */}
      {/* MODAL: DEPOSIT MONEY WITH OTP                            */}
      {/* ======================================================== */}
      {depositModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-in fade-in">
          <div className="w-full max-w-sm rounded-3xl p-6 border border-emerald-500/40 bg-slate-900 shadow-2xl space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-extrabold text-emerald-400 flex items-center gap-2">
                <DollarSign className="w-4 h-4 text-emerald-400" /> Xác Thực OTP Nạp Tiền Vào Heo Đất
              </h3>
              <button onClick={() => setDepositModalOpen(false)} className="text-slate-400 hover:text-white">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="p-3.5 rounded-2xl bg-emerald-950/30 border border-emerald-500/30 text-xs space-y-1.5 text-center">
              <span className="text-[11px] text-slate-400 uppercase tracking-wider block">Mệnh Giá Nhét Vào Khe Heo:</span>
              <div className="text-2xl font-mono font-extrabold text-emerald-400">
                +{depositPendingAmount.toLocaleString('vi-VN')} đ
              </div>
              <p className="text-[11px] text-slate-400 pt-1">
                Tiền polymer sẽ chui vào khe cảm biến TCRT5000 kèm âm thanh và cộng trực tiếp vào số dư ví CSDL thật sau khi xác thực.
              </p>
            </div>

            <div>
              <label className="block text-xs text-slate-400 mb-1">Nhập mã Smart OTP 6 số xác thực nạp tiền:</label>
              <input
                type="text"
                value={depositOtpInput}
                onChange={e => setDepositOtpInput(e.target.value)}
                placeholder="Nhập 6 số OTP..."
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2.5 text-center text-xl font-mono tracking-widest text-emerald-400 font-bold focus:outline-none focus:border-emerald-500"
                maxLength={6}
                autoFocus
              />
              <div className="text-[11px] text-slate-500 text-center mt-1">
                Mã OTP Demo Mặc Định: <span className="text-emerald-400 font-bold font-mono">123456</span>
              </div>
            </div>

            <div className="flex items-center gap-2 pt-2">
              <button
                onClick={() => setDepositModalOpen(false)}
                className="w-1/2 py-2.5 rounded-xl text-xs font-bold bg-slate-800 hover:bg-slate-700 text-slate-300"
              >
                Hủy Bỏ
              </button>
              <button
                onClick={handleConfirmDepositMoney}
                disabled={isLoading || !depositOtpInput.trim()}
                className="w-1/2 py-2.5 rounded-xl text-xs font-extrabold bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white shadow-lg"
              >
                Xác Nhận Nhét Tiền
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ======================================================== */}
      {/* MODAL: UNBIND DEVICE WITH OTP                            */}
      {/* ======================================================== */}
      {unbindModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-in fade-in">
          <div className="w-full max-w-sm rounded-3xl p-6 border border-rose-500/40 bg-slate-900 shadow-2xl space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-extrabold text-rose-400 flex items-center gap-2">
                <Unlink className="w-4 h-4 text-rose-400" /> Xác Thực OTP Hủy Liên Kết Heo Đất
              </h3>
              <button onClick={() => setUnbindModalOpen(false)} className="text-slate-400 hover:text-white">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="p-3.5 rounded-xl bg-slate-950 border border-slate-800 text-xs space-y-1.5">
              <div className="text-white font-bold">{activeDevice?.device_name}</div>
              <div className="text-slate-400 font-mono text-[11px]">MAC: {activeDevice?.mac_address}</div>
              <p className="text-[11px] text-rose-300 leading-relaxed pt-1">
                Sau khi hủy liên kết, số dư tích lũy của Heo Đất sẽ trở về 0 đ và tất cả thiết bị trong danh sách quét sẽ được mở khóa lại để bạn có thể ghép đôi thiết bị mới.
              </p>
            </div>

            <div>
              <label className="block text-xs text-slate-400 mb-1">Nhập mã Smart OTP 6 số để xác nhận hủy:</label>
              <input
                type="text"
                value={unbindOtpInput}
                onChange={e => setUnbindOtpInput(e.target.value)}
                placeholder="Nhập 6 số OTP..."
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2.5 text-center text-xl font-mono tracking-widest text-rose-400 font-bold focus:outline-none focus:border-rose-500"
                maxLength={6}
                autoFocus
              />
              <div className="text-[11px] text-slate-500 text-center mt-1">
                Mã OTP Demo Mặc Định: <span className="text-rose-400 font-bold font-mono">123456</span>
              </div>
            </div>

            <div className="flex items-center gap-2 pt-2">
              <button
                onClick={() => setUnbindModalOpen(false)}
                className="w-1/2 py-2.5 rounded-xl text-xs font-bold bg-slate-800 hover:bg-slate-700 text-slate-300"
              >
                Hủy Bỏ
              </button>
              <button
                onClick={handleConfirmUnbindDevice}
                disabled={isLoading || !unbindOtpInput.trim()}
                className="w-1/2 py-2.5 rounded-xl text-xs font-extrabold bg-gradient-to-r from-rose-600 to-red-600 hover:from-rose-500 hover:to-red-500 text-white shadow-lg"
              >
                Xác Nhận Hủy Liên Kết
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ======================================================== */}
      {/* MODAL: BUCKET TRANSFER                                   */}
      {/* ======================================================== */}
      {showTransferModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-in fade-in">
          <div className="w-full max-w-md rounded-3xl p-6 border border-emerald-500/40 bg-slate-900 shadow-2xl space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-extrabold text-white flex items-center gap-2">
                <ArrowDownUp className="w-4 h-4 text-emerald-400" /> Điều Chuyển Giữa Các Hũ Tiết Kiệm
              </h3>
              <button onClick={() => setShowTransferModal(false)} className="text-slate-400 hover:text-white">
                <X className="w-4 h-4" />
              </button>
            </div>
            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-400 mb-1">Chuyển từ Hũ nguồn:</label>
                <select
                  value={fromBucket}
                  onChange={e => setFromBucket(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-white font-bold"
                >
                  <option value="">-- Chọn hũ nguồn --</option>
                  {buckets.map(b => (
                    <option key={b.id} value={b.id}>
                      {b.goal_name} (Số dư: {(b.current_amount || 0).toLocaleString('vi-VN')} đ)
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-slate-400 mb-1">Đến Hũ đích:</label>
                <select
                  value={toBucket}
                  onChange={e => setToBucket(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-white font-bold"
                >
                  <option value="">-- Chọn hũ đích --</option>
                  {buckets.map(b => (
                    <option key={b.id} value={b.id}>
                      {b.goal_name} (Số dư: {(b.current_amount || 0).toLocaleString('vi-VN')} đ)
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-slate-400 mb-1">Số tiền điều chuyển (VNĐ):</label>
                <input
                  type="number"
                  value={transferAmount}
                  onChange={e => setTransferAmount(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-emerald-400 font-mono font-bold"
                />
              </div>
            </div>
            <div className="flex items-center gap-2 pt-2">
              <button
                onClick={() => setShowTransferModal(false)}
                className="w-1/2 py-2.5 rounded-xl text-xs font-bold bg-slate-800 hover:bg-slate-700 text-slate-300"
              >
                Hủy Bỏ
              </button>
              <button
                onClick={handleConfirmTransfer}
                disabled={isLoading}
                className="w-1/2 py-2.5 rounded-xl text-xs font-extrabold bg-emerald-600 hover:bg-emerald-500 text-white"
              >
                Thực Hiện Chuyển
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ======================================================== */}
      {/* MODAL: BANK WITHDRAW POLICY REJECTION                    */}
      {/* ======================================================== */}
      {policyRejectModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-in fade-in">
          <div className="w-full max-w-md rounded-3xl p-6 border border-rose-500/50 bg-slate-900 shadow-2xl space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-extrabold text-rose-400 flex items-center gap-2">
                <ShieldAlert className="w-5 h-5 text-rose-500" /> Từ Chối Giao Dịch: Rút Tiền Bị Khóa
              </h3>
              <button onClick={() => setPolicyRejectModalOpen(false)} className="text-slate-400 hover:text-white">
                <X className="w-4 h-4" />
              </button>
            </div>
            <div className="p-4 rounded-2xl bg-rose-950/30 border border-rose-500/30 space-y-2">
              <span className="text-xs font-bold text-rose-300 uppercase tracking-wider block">
                Chính Sách FinTech Tích Lũy Kỷ Luật
              </span>
              <p className="text-xs text-slate-300 leading-relaxed">
                Heo Đất Thông Minh hoạt động theo nguyên tắc <span className="text-rose-400 font-bold">Chỉ Nạp Tiết Kiệm (Deposit-Only)</span>. Người dùng không được phép rút tiền lẻ ra tài khoản ngân hàng nhằm rèn luyện thói quen kỷ luật tài chính.
              </p>
              <p className="text-[11px] text-slate-400 leading-relaxed pt-1">
                Để rút toàn bộ số tiền đã tiết kiệm, bạn bắt buộc phải thực hiện quy trình <span className="text-amber-300 font-bold">Đập Heo Tất Toán</span> có xác thực Smart OTP.
              </p>
            </div>
            <button
              onClick={() => setPolicyRejectModalOpen(false)}
              className="w-full py-2.5 rounded-xl text-xs font-black bg-slate-800 hover:bg-slate-700 text-white"
            >
              Tôi Đã Hiểu Chính Sách
            </button>
          </div>
        </div>
      )}

      {/* ======================================================== */}
      {/* GLOBAL POPUP NOTIFICATION MODAL                          */}
      {/* ======================================================== */}
      {popupModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-in fade-in duration-200">
          <div className={'w-full max-w-md rounded-3xl p-6 border shadow-2xl space-y-4 transform transition-all ' + (
            popupModal.type === 'success' ? 'bg-gradient-to-b from-slate-900 via-slate-900 to-emerald-950/40 border-emerald-500/50 shadow-emerald-500/20' :
            popupModal.type === 'error' ? 'bg-gradient-to-b from-slate-900 via-slate-900 to-rose-950/40 border-rose-500/50 shadow-rose-500/20' :
            popupModal.type === 'warning' ? 'bg-gradient-to-b from-slate-900 via-slate-900 to-amber-950/40 border-amber-500/50 shadow-amber-500/20' :
            'bg-gradient-to-b from-slate-900 via-slate-900 to-cyan-950/40 border-cyan-500/50 shadow-cyan-500/20'
          )}>
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-3">
                <div className={'p-3 rounded-2xl border ' + (
                  popupModal.type === 'success' ? 'bg-emerald-500/20 border-emerald-500/40 text-emerald-400' :
                  popupModal.type === 'error' ? 'bg-rose-500/20 border-rose-500/40 text-rose-400' :
                  popupModal.type === 'warning' ? 'bg-amber-500/20 border-amber-500/40 text-amber-400' :
                  'bg-cyan-500/20 border-cyan-500/40 text-cyan-400'
                )}>
                  {popupModal.type === 'success' && <CheckCircle className="w-6 h-6" />}
                  {popupModal.type === 'error' && <ShieldAlert className="w-6 h-6" />}
                  {popupModal.type === 'warning' && <AlertTriangle className="w-6 h-6" />}
                  {popupModal.type === 'info' && <Bell className="w-6 h-6" />}
                </div>
                <div>
                  <span className={'text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full border ' + (
                    popupModal.type === 'success' ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30' :
                    popupModal.type === 'error' ? 'bg-rose-500/20 text-rose-300 border-rose-500/30' :
                    popupModal.type === 'warning' ? 'bg-amber-500/20 text-amber-300 border-amber-500/30' :
                    'bg-cyan-500/20 text-cyan-300 border-cyan-500/30'
                  )}>
                    {popupModal.type === 'success' ? 'THÀNH CÔNG' :
                     popupModal.type === 'error' ? 'LỖI / BÁO ĐỘNG' :
                     popupModal.type === 'warning' ? 'CẢNH BÁO' : 'THÔNG BÁO HỆ THỐNG'}
                  </span>
                  <h3 className="text-base font-extrabold text-white mt-1">{popupModal.title}</h3>
                </div>
              </div>
              <button
                onClick={() => setPopupModal(null)}
                className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className="text-xs text-slate-300 leading-relaxed pt-1">
              {popupModal.message}
            </p>

            <div className="pt-2">
              <button
                onClick={() => setPopupModal(null)}
                className={'w-full py-2.5 rounded-xl text-xs font-black transition shadow-lg ' + (
                  popupModal.type === 'success' ? 'bg-emerald-600 hover:bg-emerald-500 text-white' :
                  popupModal.type === 'error' ? 'bg-rose-600 hover:bg-rose-500 text-white' :
                  popupModal.type === 'warning' ? 'bg-amber-600 hover:bg-amber-500 text-black' :
                  'bg-cyan-600 hover:bg-cyan-500 text-white'
                )}
              >
                ĐÃ HIỂU & ĐÓNG
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SmartPiggyDemoPage;
