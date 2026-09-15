
import json
from pathlib import Path
from typing import Union, Dict
from fastapi import Request

# 👑 XÁC ĐỊNH THƯ MỤC CHỨA TỪ ĐIỂN i18n
BASE_DIR = Path(__file__).resolve().parent.parent.parent.parent
I18N_DIR = BASE_DIR / "i18n"


class I18nTranslator:
    """
    👑 ENGINE TRA CỨU TỪ ĐIỂN ĐA NGÔN NGỮ SIÊU TỐC (IN-MEMORY DICTIONARY CACHE)
    🎯 Mục đích:
       - Tải toàn bộ ma trận ngôn ngữ (errors, messages, labels) từ các file JSON vào RAM khi khởi động.
       - Tra cứu với độ phức tạp O(1) tức thì, loại bỏ 100% việc kết nối SQL chỉ để dịch câu chữ.
       - Hỗ trợ fallback linh hoạt: Ngôn ngữ yêu cầu -> Tiếng Việt ('vi') -> Tiếng Anh ('en') -> Mã lỗi gốc.
    """

    def __init__(self):
        # Bộ nhớ đệm RAM lưu trữ từ điển: { "vi": {...}, "en": {...}, "zh": {...} }
        self._cache: Dict[str, Dict[str, str]] = {"vi": {}, "en": {}, "zh": {}}
        self.load_translations_to_memory()

    def load_translations_to_memory(self) -> None:
        """
        👑 HÀM NẠP TỪ ĐIỂN VÀO RAM:
        Quét qua toàn bộ thư mục i18n/{lang}/{errors,messages,labels}.json để gom vào _cache.
        """
        target_files = ["errors.json", "messages.json", "labels.json"]
        languages = ["vi", "en", "zh"]

        for lang in languages:
            lang_dict: Dict[str, str] = {}
            for file_name in target_files:
                file_path = I18N_DIR / lang / file_name
                if file_path.exists():
                    try:
                        with open(file_path, "r", encoding="utf-8") as f:
                            data = json.load(f)
                            if isinstance(data, dict):
                                lang_dict.update(data)
                    except Exception as e:
                        print(f"[i18n_CACHE_WARNING] Lỗi đọc file {file_path}: {str(e)}")
            self._cache[lang] = lang_dict

        total_keys = sum(len(d) for d in self._cache.values())
        print(f"[i18n_ENGINE] Da nap thanh cong {total_keys} ban dich tu dien vao bo nho RAM O(1).")

    def _extract_lang(self, request_or_lang: Union[Request, str, None]) -> str:
        """
        👑 HÀM BÓC TÁCH MÃ NGÔN NGỮ:
        Trích xuất ngôn ngữ từ Header Accept-Language hoặc nhận trực tiếp chuỗi 'vi', 'en', 'zh'.
        """
        if isinstance(request_or_lang, str):
            lang = request_or_lang.strip().lower()
            return lang if lang in ["vi", "en", "zh"] else "vi"

        if isinstance(request_or_lang, Request):
            raw_lang = request_or_lang.headers.get("Accept-Language", "vi")
            lang = raw_lang.split(",")[0].split("-")[0].lower()
            return lang if lang in ["vi", "en", "zh"] else "vi"

        return "vi"

    def translate(self, request_or_lang: Union[Request, str, None], error_code: str, msg_type: str = "ERROR") -> str:
        """
        👑 HÀM DỊCH THÔNG ĐIỆP ĐA NGÔN NGỮ SIÊU TỐC:
        1. Bóc mã ngôn ngữ (vi/en/zh).
        2. Tra cứu trực tiếp trên RAM dictionary (độ trễ 0ms).
        3. Nếu không thấy trong ngôn ngữ hiện tại, fallback về tiếng Việt, rồi tiếng Anh.
        4. Nếu hoàn toàn không có, trả về chuỗi định dạng an toàn.
        """
        if not error_code:
            return ""

        target_lang = self._extract_lang(request_or_lang)

        # 1. Tra cứu theo ngôn ngữ người dùng yêu cầu
        if target_lang in self._cache and error_code in self._cache[target_lang]:
            return self._cache[target_lang][error_code]

        # 2. Fallback sang tiếng Việt ('vi')
        if "vi" in self._cache and error_code in self._cache["vi"]:
            return self._cache["vi"][error_code]

        # 3. Fallback sang tiếng Anh ('en')
        if "en" in self._cache and error_code in self._cache["en"]:
            return self._cache["en"][error_code]

        # 4. Fallback cuối cùng nếu không tìm thấy key
        return f"[{msg_type}] {error_code}"


# Khởi tạo Singleton Instance duy nhất dùng cho toàn hệ thống
i18n_translator = I18nTranslator()