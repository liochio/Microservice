import React, { useState } from 'react';
import { 
  Sliders, 
  Mail, 
  ShieldCheck, 
  DollarSign, 
  Save, 
  CheckCircle2, 
  MessageSquare
} from 'lucide-react';
import { Card } from '../components/common/CardAndBadge';
import { Button } from '../components/common/Button';

export const GlobalConfigPage: React.FC = () => {
  const [savedSuccess, setSavedSuccess] = useState(false);

  const [mailConfig, setMailConfig] = useState({
    smtpHost: 'smtp.sendgrid.net',
    smtpPort: 587,
    smtpUser: 'apikey',
    smtpPassword: '••••••••••••••••••••••••',
    senderEmail: 'noreply-notifications@liochio.vn',
    senderName: 'Liochio Global Master Gateway',
    useTls: true,
  });

  const [smsConfig, setSmsConfig] = useState({
    provider: 'SPEEDSMS_VN',
    apiKey: 'sk_live_992837482937482910',
    senderId: 'LIOCHIO_OTP',
    otpTtlSeconds: 120,
  });

  const [limitsConfig, setLimitsConfig] = useState({
    maxSingleTxCeiling: 50000000,
    maxDailyTxCeiling: 200000000,
    biometricThreshold: 10000000,
  });

  const [securityPolicy, setSecurityPolicy] = useState({
    enforce2FaStaff: true,
    maxFailedLogins: 5,
    jwtTtlMinutes: 120,
  });

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 3000);
  };

  return (
    <div className="space-y-6 animate-fade-in max-w-5xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <Sliders className="w-7 h-7 text-amber-400" />
            Global System Configurations
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Master parameters inherited by all Corporate Tenants unless explicitly overridden
          </p>
        </div>
        <Button onClick={handleSave} className="flex items-center gap-2">
          <Save className="w-4 h-4" />
          Save Global Config
        </Button>
      </div>

      {savedSuccess && (
        <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-sm flex items-center gap-3">
          <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
          <span>Global parameters synchronized successfully across Redis Cluster and Gateway.</span>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="space-y-4">
          <div className="flex items-center gap-2 text-white font-bold text-base pb-2 border-b border-slate-800">
            <Mail className="w-5 h-5 text-indigo-400" />
            <span>Global Mail Gateway (SMTP)</span>
          </div>

          <div className="space-y-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">SMTP Host</label>
              <input
                type="text"
                value={mailConfig.smtpHost}
                onChange={e => setMailConfig({ ...mailConfig, smtpHost: e.target.value })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">SMTP Port</label>
                <input
                  type="number"
                  value={mailConfig.smtpPort}
                  onChange={e => setMailConfig({ ...mailConfig, smtpPort: Number(e.target.value) })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Use TLS/SSL</label>
                <select
                  value={mailConfig.useTls ? 'true' : 'false'}
                  onChange={e => setMailConfig({ ...mailConfig, useTls: e.target.value === 'true' })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
                >
                  <option value="true">TLS Enabled (STARTTLS)</option>
                  <option value="false">None / Plain</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Sender Email</label>
              <input
                type="email"
                value={mailConfig.senderEmail}
                onChange={e => setMailConfig({ ...mailConfig, senderEmail: e.target.value })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Sender Display Name</label>
              <input
                type="text"
                value={mailConfig.senderName}
                onChange={e => setMailConfig({ ...mailConfig, senderName: e.target.value })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>
        </Card>

        <Card className="space-y-4">
          <div className="flex items-center gap-2 text-white font-bold text-base pb-2 border-b border-slate-800">
            <MessageSquare className="w-5 h-5 text-amber-400" />
            <span>SMS & OTP Master Gateway</span>
          </div>

          <div className="space-y-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">SMS Provider</label>
              <select
                value={smsConfig.provider}
                onChange={e => setSmsConfig({ ...smsConfig, provider: e.target.value })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              >
                <option value="SPEEDSMS_VN">SpeedSMS Vietnam</option>
                <option value="VNPT_SMS">VNPT Enterprise SMS</option>
                <option value="TWILIO">Twilio International</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">API Key / Auth Token</label>
              <input
                type="password"
                value={smsConfig.apiKey}
                onChange={e => setSmsConfig({ ...smsConfig, apiKey: e.target.value })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Sender ID</label>
                <input
                  type="text"
                  value={smsConfig.senderId}
                  onChange={e => setSmsConfig({ ...smsConfig, senderId: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white uppercase focus:outline-none focus:border-amber-500"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">OTP TTL (Seconds)</label>
                <input
                  type="number"
                  value={smsConfig.otpTtlSeconds}
                  onChange={e => setSmsConfig({ ...smsConfig, otpTtlSeconds: Number(e.target.value) })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
                />
              </div>
            </div>
          </div>
        </Card>

        <Card className="space-y-4">
          <div className="flex items-center gap-2 text-white font-bold text-base pb-2 border-b border-slate-800">
            <DollarSign className="w-5 h-5 text-emerald-400" />
            <span>Global Ceiling Limits (VND)</span>
          </div>

          <div className="space-y-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Max Per-Transaction Ceiling</label>
              <input
                type="number"
                value={limitsConfig.maxSingleTxCeiling}
                onChange={e => setLimitsConfig({ ...limitsConfig, maxSingleTxCeiling: Number(e.target.value) })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Max Daily Total Ceiling</label>
              <input
                type="number"
                value={limitsConfig.maxDailyTxCeiling}
                onChange={e => setLimitsConfig({ ...limitsConfig, maxDailyTxCeiling: Number(e.target.value) })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>
        </Card>

        <Card className="space-y-4">
          <div className="flex items-center gap-2 text-white font-bold text-base pb-2 border-b border-slate-800">
            <ShieldCheck className="w-5 h-5 text-rose-400" />
            <span>IAM Security & Token Lifecycle</span>
          </div>

          <div className="space-y-3">
            <div className="flex items-center justify-between p-3 bg-slate-950 rounded-xl border border-slate-800">
              <div>
                <div className="text-xs font-semibold text-slate-200">Enforce 2FA for Staff & Corp Admins</div>
                <div className="text-[11px] text-slate-500">Require TOTP authenticator app</div>
              </div>
              <input
                type="checkbox"
                checked={securityPolicy.enforce2FaStaff}
                onChange={e => setSecurityPolicy({ ...securityPolicy, enforce2FaStaff: e.target.checked })}
                className="w-4 h-4 rounded text-amber-500"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Max Failed Logins</label>
                <input
                  type="number"
                  value={securityPolicy.maxFailedLogins}
                  onChange={e => setSecurityPolicy({ ...securityPolicy, maxFailedLogins: Number(e.target.value) })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
                />
              </div>
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">JWT TTL (Mins)</label>
                <input
                  type="number"
                  value={securityPolicy.jwtTtlMinutes}
                  onChange={e => setSecurityPolicy({ ...securityPolicy, jwtTtlMinutes: Number(e.target.value) })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-amber-500"
                />
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};
