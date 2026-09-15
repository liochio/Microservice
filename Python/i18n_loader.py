
import os
import json
from sqlalchemy import text
from app.db.session import engine


def auto_sync_i18n_json_to_db():
    """
    👑 ENGINE ĐỒNG BỘ TỐI CAO i18n 3-IN-1:
    Tự động quét trọn bộ ma trận dữ liệu (errors, messages, labels) của cả 3 ngôn ngữ (vi, en, zh),
    phân loại bằng cột 'msg_type' và găm thẳng xuống bảng gốc 'error_messages' hệ thống.

    🛡️ KHẮC PHỤC LỖI ĐÈ DATA: Sử dụng logic Python để cô lập dòng theo cặp (error_code, msg_type),
    đảm bảo cả 3 nhãn ERROR, MESSAGE, LABEL nằm song song hiên ngang dưới DB mà không bị đè bấy dữ liệu.
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

        # Câu lệnh kiểm tra xem bản ghi đã tồn tại theo đúng cặp (error_code, msg_type) chưa
        check_query = text("""
                           SELECT id
                           FROM error_messages
                           WHERE error_code = :error_code
                             AND msg_type = :msg_type
                           """)

        # Câu lệnh INSERT dòng mới tinh khôi
        insert_query = text("""
                            INSERT INTO error_messages (id, error_code, msg_type, lang_vi, lang_en, lang_zh, status,
                                                        created_at, updated_at)
                            VALUES (UUID(), :error_code, :msg_type, :lang_vi, :lang_en, :lang_zh, 'ACTIVE', NOW(),
                                    NOW())
                            """)

        # Câu lệnh UPDATE nội dung dịch nếu trùng khít cả code lẫn type
        update_query = text("""
                            UPDATE error_messages
                            SET lang_vi    = :lang_vi,
                                lang_en    = :lang_en,
                                lang_zh    = :lang_zh,
                                updated_at = NOW()
                            WHERE error_code = :error_code
                              AND msg_type = :msg_type
                            """)

        total_records_synced = 0

        with engine.connect() as connection:
            # 🛡️ BƯỚC LÀM SẠCH MẶT BẰNG: Xóa sạch dữ liệu cũ đúng 1 lần duy nhất trước khi nạp để quét mới tinh khôi
            print("[I18N_SYNC] Đang làm sạch bảng từ điển hiển thị toàn cục...")
            connection.execute(text("SET FOREIGN_KEY_CHECKS = 0;"))
            connection.execute(text("TRUNCATE TABLE error_messages;"))
            connection.execute(text("SET FOREIGN_KEY_CHECKS = 1;"))
            connection.commit()

            # Vòng lặp tối cao quét qua từng loại dữ liệu hiển thị (ERROR, MESSAGE, LABEL)
            for current_type, file_name in target_files.items():

                # Nạp dữ liệu thô từ 3 file ngôn ngữ tương ứng vào RAM (Cô lập theo từng vòng lặp)
                data_by_lang = {}
                for lang in languages:
                    json_path = os.path.join(base_dir, "i18n", lang, file_name)

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

                # Lấy file tiếng Việt làm trục tọa độ gốc để duyệt Key (Mã Code)
                current_vi_dict = data_by_lang.get("vi", {})
                if not current_vi_dict:
                    current_vi_dict = data_by_lang.get("en", {})
                    if not current_vi_dict:
                        continue

                # Thực thi so khớp logic tách dòng bằng Python
                for target_code, text_vi in current_vi_dict.items():
                    # Cơ chế dự phòng (Fallback) nếu thiếu ngôn ngữ phụ
                    text_en = data_by_lang["en"].get(target_code, text_vi)
                    text_zh = data_by_lang["zh"].get(target_code, text_vi)

                    # Kiểm tra sự tồn tại của cặp bài trùng (error_code + msg_type)
                    result = connection.execute(check_query,
                                                {"error_code": target_code, "msg_type": current_type}).fetchone()

                    if result:
                        # Nếu đã tồn tại đúng cặp này -> Chỉ UPDATE nội dung text đa ngôn ngữ
                        connection.execute(update_query, {
                            "error_code": target_code,
                            "msg_type": current_type,
                            "lang_vi": text_vi,
                            "lang_en": text_en,
                            "lang_zh": text_zh
                        })
                    else:
                        # Nếu chưa có -> Tạo mới dòng riêng biệt, giữ nguyên Key sạch và ghi nhận đúng nhãn type
                        connection.execute(insert_query, {
                            "error_code": target_code,
                            "msg_type": current_type,
                            "lang_vi": text_vi,
                            "lang_en": text_en,
                            "lang_zh": text_zh
                        })

                    total_records_synced += 1

            connection.commit()
            print(
                f"[I18N_MASTER_SYNC] Thành công rực rỡ! Đã tự động phân tách và đồng bộ trọn vẹn {total_records_synced} bản ghi (Gồm phân loại ERROR, MESSAGE, LABEL) xuống bảng error_messages.")

    except Exception as e:
        print(f"[I18N_MASTER_SYNC_ERROR] Thất bại khi đồng bộ tổng hợp i18n xuống bảng gốc: {str(e)}")