
import os
import json
from sqlalchemy import text
from app.db.session import engine


def auto_sync_i18n_json_to_db():
    """
    👑 ENGINE ĐỒNG BỘ TỐI CAO i18n 3-IN-1:
    Tự động quét trọn bộ ma trận dữ liệu (errors, messages, labels) của cả 3 ngôn ngữ (vi, en, zh),
    phân loại bằng cột 'msg_type' và găm thẳng xuống bảng gốc 'error_messages' hệ thống.

    🛡️ BẢO MẬT CAO AN TOÀN TUYỆT ĐỐI: Lỗi bất kỳ trong file JSON hoặc DB sẽ bị cô lập hoàn toàn,
    không bao giờ được phép làm ảnh hưởng hoặc crash sập tiến trình khởi động Uvicorn.
    """
    try:
        # Định vị thư mục gốc dự án từ app/core/translator/
        base_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

        # Định nghĩa ma trận tệp tin cần quét sạch
        target_files = {
            "ERROR": "errors.json",
            "MESSAGE": "messages.json",
            "LABEL": "labels.json"
        }

        languages = ["vi", "en", "zh"]

        # Câu lệnh UPSERT bảo mật cao master: Khóa cứng error_code gốc hệ thống, ép cập nhật nội dung đa ngôn ngữ
        query = text("""
                     INSERT INTO error_messages (id, error_code, msg_type, lang_vi, lang_en, lang_zh, status,
                                                 created_at, updated_at)
                     VALUES (UUID(), :error_code, :msg_type, :lang_vi, :lang_en, :lang_zh, 'ACTIVE', NOW(),
                             NOW()) ON DUPLICATE KEY
                     UPDATE
                         msg_type =
                     VALUES (msg_type), lang_vi =
                     VALUES (lang_vi), lang_en =
                     VALUES (lang_en), lang_zh =
                     VALUES (lang_zh), updated_at = NOW();
                     """)

        total_records_synced = 0

        with engine.connect() as connection:
            # Vòng lặp tối cao quét qua từng loại dữ liệu hiển thị (ERROR, MESSAGE, LABEL)
            for msg_type, file_name in target_files.items():

                # Nạp dữ liệu thô từ 3 file ngôn ngữ tương ứng vào RAM
                data_by_lang = {}
                for lang in languages:
                    json_path = os.path.join(base_dir, "i18n", lang, file_name)

                    # Bẫy lỗi cục bộ cho từng file JSON, file nào lỗi cú pháp thì bỏ qua để quét tiếp file khác
                    if os.path.exists(json_path):
                        try:
                            with open(json_path, "r", encoding="utf-8") as f:
                                data_by_lang[lang] = json.load(f)
                        except Exception as json_err:
                            print(
                                f"[I18N_SYNC_WARNING] Tệp tin {json_path} lỗi cú pháp JSON rác, tự động bỏ qua: {str(json_err)}")
                            data_by_lang[lang] = {}
                    else:
                        data_by_lang[lang] = {}

                # Lấy tập hợp toàn bộ Key (Mã Code) có mặt trong bất kỳ ngôn ngữ nào
                all_keys = set()
                for l in languages:
                    all_keys.update(data_by_lang.get(l, {}).keys())

                for error_code in all_keys:
                    text_vi = data_by_lang.get("vi", {}).get(error_code) or data_by_lang.get("en", {}).get(error_code) or error_code
                    text_en = data_by_lang.get("en", {}).get(error_code) or text_vi
                    text_zh = data_by_lang.get("zh", {}).get(error_code) or text_vi

                    connection.execute(query, {
                        "error_code": error_code,
                        "msg_type": msg_type,
                        "lang_vi": text_vi,
                        "lang_en": text_en,
                        "lang_zh": text_zh
                    })
                    total_records_synced += 1

            connection.commit()
            print(
                f"[I18N_MASTER_SYNC] Đồng bộ thành công {total_records_synced} bản ghi (gồm ERRORS, MESSAGES, LABELS) kèm nhãn cột type xuống bảng error_messages.")

    except Exception as e:
        # 🛡️ GÁC CỔNG TỐI CAO: Bắt lại toàn bộ lỗi nghiêm trọng (Mất kết nối DB...) in ra log cảnh báo, giữ an toàn cho Uvicorn
        print(
            f"[I18N_MASTER_SYNC_ERROR] Thất bại khi đồng bộ tổng hợp i18n xuống bảng gốc, tiến trình Uvicorn vẫn được giữ an toàn: {str(e)}")