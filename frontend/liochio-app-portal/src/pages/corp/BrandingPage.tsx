import React, { useState } from 'react';
import { Palette, Globe, Save, CheckCircle2 } from 'lucide-react';
import { Card } from '../../components/common/CardAndBadge';
import { Button } from '../../components/common/Button';

export const BrandingPage: React.FC = () => {
  const [savedSuccess, setSavedSuccess] = useState(false);
  const [branding, setBranding] = useState({
    companyName: 'VPBank Digital Banking',
    brandTitle: 'VPBank Piggy Junior Savings',
    logoUrl: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150',
    primaryColor: '#06b6d4',
    accentColor: '#3b82f6',
    customDomain: 'piggy.vpbank.com.vn',
  });

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 3000);
  };

  return (
    <div className="space-y-6 animate-fade-in max-w-4xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <Palette className="w-7 h-7 text-cyan-400" />
            White-Label Branding & Theme
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Customize portal colors, logo, and custom tenant domain for your bank / enterprise
          </p>
        </div>
        <Button onClick={handleSave} className="flex items-center gap-2">
          <Save className="w-4 h-4" />
          Save Theme & Domain
        </Button>
      </div>

      {savedSuccess && (
        <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-sm flex items-center gap-3">
          <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
          <span>Tenant white-label customizations saved and deployed to edge CDN.</span>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="space-y-4">
          <h3 className="font-bold text-white text-base pb-2 border-b border-slate-800">
            Brand Identity
          </h3>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">Company Legal Name</label>
            <input
              type="text"
              value={branding.companyName}
              onChange={e => setBranding({ ...branding, companyName: e.target.value })}
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">Brand Product Title</label>
            <input
              type="text"
              value={branding.brandTitle}
              onChange={e => setBranding({ ...branding, brandTitle: e.target.value })}
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">Logo URL</label>
            <input
              type="text"
              value={branding.logoUrl}
              onChange={e => setBranding({ ...branding, logoUrl: e.target.value })}
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-cyan-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">Custom Dedicated Subdomain</label>
            <div className="flex items-center bg-slate-950 border border-slate-800 rounded-xl px-3 py-2">
              <Globe className="w-4 h-4 text-slate-500 mr-2 shrink-0" />
              <input
                type="text"
                value={branding.customDomain}
                onChange={e => setBranding({ ...branding, customDomain: e.target.value })}
                className="w-full bg-transparent text-xs text-white focus:outline-none font-mono"
              />
            </div>
          </div>
        </Card>

        <Card className="space-y-4">
          <h3 className="font-bold text-white text-base pb-2 border-b border-slate-800">
            Brand Preview
          </h3>

          <div className="p-6 rounded-2xl bg-slate-950 border border-slate-800 space-y-4">
            <div className="flex items-center gap-3">
              <div 
                className="w-10 h-10 rounded-xl flex items-center justify-center font-black text-white"
                style={{ backgroundColor: branding.primaryColor }}
              >
                VP
              </div>
              <div>
                <div className="font-bold text-sm text-white">{branding.brandTitle}</div>
                <div className="text-[11px] text-slate-400">{branding.companyName}</div>
              </div>
            </div>

            <div className="pt-3 border-t border-slate-800 space-y-2">
              <div className="text-xs text-slate-400">PWA Button Style:</div>
              <button 
                className="w-full py-2.5 rounded-xl font-bold text-xs text-slate-950 transition"
                style={{ backgroundColor: branding.primaryColor }}
              >
                Nạp Tiền Vào Heo Đất
              </button>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};
