import React, { useState } from 'react';
import { Mail, CheckCircle2, Save, Send } from 'lucide-react';
import { Card, Badge } from '../../components/common/CardAndBadge';
import { Button } from '../../components/common/Button';

export const CorpMailConfigPage: React.FC = () => {
  const [useCustomMail, setUseCustomMail] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);
  const [testSuccess, setTestSuccess] = useState(false);

  const [customConfig, setCustomConfig] = useState({
    smtpHost: 'smtp.office365.com',
    smtpPort: 587,
    smtpUser: 'notifications@vpbank.com.vn',
    smtpPassword: '••••••••••••••••',
    senderEmail: 'piggy-support@vpbank.com.vn',
    senderName: 'VPBank Piggy Notification Center',
    useTls: true,
  });

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 3000);
  };

  const handleTestMail = () => {
    setTestSuccess(true);
    setTimeout(() => setTestSuccess(false), 3000);
  };

  return (
    <div className="space-y-6 animate-fade-in max-w-4xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <Mail className="w-7 h-7 text-cyan-400" />
            Tenant Mail Delivery Configuration
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Choose whether to inherit the SuperAdmin Global Gateway or configure a dedicated corporate SMTP server
          </p>
        </div>
        <Button onClick={handleSave} className="flex items-center gap-2">
          <Save className="w-4 h-4" />
          Save Configuration
        </Button>
      </div>

      {savedSuccess && (
        <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-sm flex items-center gap-3">
          <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
          <span>Tenant email routing strategy updated successfully.</span>
        </div>
      )}

      {testSuccess && (
        <div className="p-4 rounded-xl bg-cyan-500/10 border border-cyan-500/30 text-cyan-300 text-sm flex items-center gap-3">
          <CheckCircle2 className="w-5 h-5 text-cyan-400 shrink-0" />
          <span>Test email dispatched successfully to admin inbox.</span>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div 
          onClick={() => setUseCustomMail(false)}
          className={`p-5 rounded-2xl border cursor-pointer transition ${
            !useCustomMail 
              ? 'bg-cyan-500/10 border-cyan-500/40 text-white shadow-lg' 
              : 'bg-slate-900/60 border-slate-800 text-slate-400 hover:border-slate-700'
          }`}
        >
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-sm">Inherit Global Gateway</h3>
            {!useCustomMail && <Badge variant="info">Active</Badge>}
          </div>
          <p className="text-xs text-slate-400 mt-2 leading-relaxed">
            Uses the high-throughput Global Mail Gateway managed and monitored by SuperAdmin. Zero configuration required.
          </p>
        </div>

        <div 
          onClick={() => setUseCustomMail(true)}
          className={`p-5 rounded-2xl border cursor-pointer transition ${
            useCustomMail 
              ? 'bg-cyan-500/10 border-cyan-500/40 text-white shadow-lg' 
              : 'bg-slate-900/60 border-slate-800 text-slate-400 hover:border-slate-700'
          }`}
        >
          <div className="flex items-center justify-between">
            <h3 className="font-bold text-sm">Dedicated Custom SMTP</h3>
            {useCustomMail && <Badge variant="info">Active</Badge>}
          </div>
          <p className="text-xs text-slate-400 mt-2 leading-relaxed">
            Route OTPs and alerts through your bank's private Exchange / SendGrid server with custom DKIM & SPF records.
          </p>
        </div>
      </div>

      {useCustomMail && (
        <Card className="space-y-4">
          <h3 className="font-bold text-white text-base pb-2 border-b border-slate-800">
            Dedicated Corporate SMTP Settings
          </h3>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">SMTP Host</label>
              <input
                type="text"
                value={customConfig.smtpHost}
                onChange={e => setCustomConfig({ ...customConfig, smtpHost: e.target.value })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">SMTP Port</label>
              <input
                type="number"
                value={customConfig.smtpPort}
                onChange={e => setCustomConfig({ ...customConfig, smtpPort: Number(e.target.value) })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">SMTP Username</label>
              <input
                type="text"
                value={customConfig.smtpUser}
                onChange={e => setCustomConfig({ ...customConfig, smtpUser: e.target.value })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">SMTP Password</label>
              <input
                type="password"
                value={customConfig.smtpPassword}
                onChange={e => setCustomConfig({ ...customConfig, smtpPassword: e.target.value })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Sender Email</label>
              <input
                type="email"
                value={customConfig.senderEmail}
                onChange={e => setCustomConfig({ ...customConfig, senderEmail: e.target.value })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">Sender Name</label>
              <input
                type="text"
                value={customConfig.senderName}
                onChange={e => setCustomConfig({ ...customConfig, senderName: e.target.value })}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
              />
            </div>
          </div>

          <div className="pt-2 flex justify-end">
            <Button type="button" variant="outline" onClick={handleTestMail} className="flex items-center gap-2">
              <Send className="w-4 h-4" />
              Send Test Verification Email
            </Button>
          </div>
        </Card>
      )}
    </div>
  );
};
