export interface MpuSensorData {
  accelX: number;
  accelY: number;
  accelZ: number;
  totalG: number;
  isFatalCrash: boolean; // > 6.0G
  isTamperVibration: boolean;
}

export interface WatchdogTelemetry {
  deviceId: string;
  status: 'ONLINE' | 'OFFLINE_SUSPICIOUS' | 'TAMPER_ALERT' | 'HARDWARE_FAULT';
  lastHeartbeatSecondsAgo: number;
  pingMs: number;
  batteryPct: number;
  wifiRssi: number;
}

export interface WriteOffRequest {
  deviceId: string;
  lostAmount: number;
  reason: 'FATAL_CRASH_PHYSICAL_LOSS' | 'THEFT_TAMPER' | 'MECHANICAL_DESTROYED';
  approverAdminId: string;
  notes: string;
}
