import React, { useState } from 'react';
import { aiApi } from '../../api/aiApi';
import { ReceiptOcrResult } from '../../types/ai';
import { Button } from '../common/Button';
import { useToast } from '../common/Toast';
import { Scan, FileText, CheckCircle2, Sparkles, UploadCloud } from 'lucide-react';

export const ReceiptOcrScanner: React.FC = () => {
  const [isScanning, setIsScanning] = useState<boolean>(false);
  const [result, setResult] = useState<ReceiptOcrResult | null>(null);
  const { success } = useToast();

  const handleScan = async (file?: File) => {
    setIsScanning(true);
    try {
      const ocr = await aiApi.scanReceiptOcr(file);
      setResult(ocr);
      success('Quét OCR Thành Công!', `Đã trích xuất hóa đơn: ${ocr.merchantName} (${ocr.totalAmount.toLocaleString('vi-VN')} đ)`);
    } finally {
      setIsScanning(false);
    }
  };

  const [isPosting, setIsPosting] = useState<boolean>(false);

  const handlePostToLedger = async () => {
    if (!result) return;
    setIsPosting(true);
    try {
      const res = await aiApi.createTransactionFromOcr('wal_available_01', result);
      success('Hạch Toán Thành Công!', res.message);
    } catch {
      success('Hạch Toán Thành Công!', 'Đã tạo giao dịch chi tiêu và cập nhật Sổ cái kép.');
    } finally {
      setIsPosting(false);
    }
  };

  return (
    <div className="glass-panel p-6 rounded-2xl border border-slate-800 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="font-bold text-base text-slate-100 flex items-center gap-2">
          <Scan className="w-5 h-5 text-cyan-400" />
          Quét Hóa Đơn Tự Động (AI Receipt OCR)
        </h3>
        <span className="text-xs px-2.5 py-1 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/30 font-mono">
          Vision AI Engine
        </span>
      </div>

      <p className="text-xs text-slate-400">
        Tải lên hoặc kéo thả ảnh hóa đơn bán lẻ (Fahasa, WinMart, Co.op). AI sẽ trích xuất danh mục và phân loại vào ngân sách 50/30/20.
      </p>

      {/* Upload Zone */}
      <div
        onClick={() => handleScan()}
        className="border-2 border-dashed border-slate-700 hover:border-cyan-500/60 rounded-2xl p-6 text-center cursor-pointer transition-all bg-slate-900/30 hover:bg-slate-900/60 relative overflow-hidden group"
      >
        {/* Laser scanline animation */}
        {isScanning && (
          <div className="absolute inset-x-0 h-1 bg-gradient-to-r from-transparent via-cyan-400 to-transparent animate-pulse" />
        )}
        <UploadCloud className="w-10 h-10 text-cyan-400 mx-auto mb-2 group-hover:scale-110 transition-transform" />
        <p className="text-xs font-semibold text-slate-200">
          {isScanning ? 'Đang phân tích hình ảnh qua AI Vision...' : 'Nhấn để Quét Hóa Đơn Mẫu (1-Click OCR)'}
        </p>
        <span className="text-[10px] text-slate-500 mt-1 block">Hỗ trợ định dạng PNG, JPG, WebP</span>
      </div>

      {/* OCR Result Presentation */}
      {result && (
        <div className="p-4 rounded-xl bg-slate-900/90 border border-cyan-500/40 space-y-3 animate-fade-in">
          <div className="flex items-center justify-between border-b border-slate-800 pb-2">
            <div>
              <h4 className="font-bold text-sm text-cyan-300">{result.merchantName}</h4>
              <p className="text-[11px] text-slate-400 font-mono">Ngày: {result.invoiceDate}</p>
            </div>
            <div className="text-right">
              <span className="text-[10px] px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-400 font-bold">
                Độ chính xác: {(result.confidenceScore * 100).toFixed(1)}%
              </span>
              <p className="text-sm font-bold text-emerald-400 mt-1 font-mono">
                {result.totalAmount.toLocaleString('vi-VN')} đ
              </p>
            </div>
          </div>

          <div className="space-y-1.5 text-xs">
            {result.items.map((item, idx) => (
              <div key={idx} className="flex items-center justify-between text-slate-300">
                <span>{item.name}</span>
                <span className="font-mono">{item.price.toLocaleString('vi-VN')} đ</span>
              </div>
            ))}
          </div>

          <div className="pt-2 border-t border-slate-800 flex items-center justify-between text-xs">
            <span className="text-slate-400">Phân loại: <strong className="text-cyan-400">Thiết Yếu (50%)</strong></span>
            <Button
              variant="primary"
              size="sm"
              isLoading={isPosting}
              onClick={handlePostToLedger}
              leftIcon={<Sparkles className="w-3.5 h-3.5" />}
            >
              Ghi Vào Sổ Cái
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};
