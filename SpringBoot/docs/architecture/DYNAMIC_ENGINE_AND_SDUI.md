# ĐỘNG CƠ THỰC THỂ ĐỘNG & GIAO DIỆN MÁY CHỦ ĐIỀU KHIỂN (SERVER-DRIVEN UI & HYBRID EAV)

> **Mục tiêu:** Một bộ mã nguồn và cơ sở dữ liệu duy nhất hỗ trợ 100% các loại website khác nhau (Du lịch, Blog, Nhiếp ảnh, Âm nhạc, Khóa học, Bán hàng).  
> **Kiến trúc dữ liệu:** Hybrid EAV (Entity-Attribute-Value) kết hợp MySQL 8.0 JSON Document.  
> **Server-Driven UI (SDUI):** Toàn bộ layout giao diện, menu điều hướng và biểu mẫu form được máy chủ trả về dạng JSON để Frontend React/Next.js/Flutter render động.  

---

## 1. MÔ HÌNH HYBRID EAV + JSONB DYNAMIC ENTITY

Thay vì tạo bảng vật lý mới mỗi khi phát sinh một thể loại dữ liệu (bảng 'tours', 'hotels', 'albums', 'cars'...), hệ thống gom về bảng 'dynamic_entities' với sự hỗ trợ của 'entity_types' và 'dynamic_field_definitions':

'''
[ entity_types ]
  ├── code: "TOUR_PACKAGE"
  ├── name: "Gói Tour Du Lịch"
  └── is_hierarchical: false
          │
          ├── (1-N) ──► [ dynamic_field_definitions ]
          │                ├── field_key: "destination" (data_type: STRING, ui: INPUT_TEXT)
          │                ├── field_key: "departure_date" (data_type: DATE, ui: DATE_PICKER)
          │                └── field_key: "price_vnd" (data_type: NUMBER, ui: INPUT_NUMBER)
          │
          └── (1-N) ──► [ dynamic_entities ]
                           ├── title: "Tour Khám Phá Hà Giang 3N2Đ"
                           ├── slug: "tour-kham-pha-ha-giang-3n2d"
                           ├── attributes (JSON):
                           │     {
                           │       "destination": "Đồng Văn, Mèo Vạc",
                           │       "departure_date": "2026-09-01",
                           │       "price_vnd": 3500000
                           │     }
                           └── i18n_content (JSON):
                                 {
                                   "en": { "title": "Ha Giang 3D2N Discovery Tour", "summary": "..." }
                                 }
'''

---

## 2. SERVER-DRIVEN UI (SDUI) & DYNAMIC FORMS

1. **Giao diện Theo Tuyến Đường ('ui_configurations')**:
   - Mỗi trang web ('page_route': '/', '/tours', '/about') lưu trữ một cây các block layout ('layout_blocks' JSON).
   - Ví dụ: '[ { "block": "HERO_BANNER", "props": {...} }, { "block": "TOUR_GRID", "props": {...} } ]'.
   - Admin có thể kéo thả thay đổi vị trí các block trên Dashboard, Frontend tự động cập nhật ngay lập tức mà không cần build lại ứng dụng.

2. **Cây Menu Đa Cấp ('navigation_menus')**:
   - Hỗ trợ menu cha/con phân cấp đa tầng, icon tùy biến và kiểm tra quyền hiển thị ('required_permission').

3. **Biểu mẫu Động ('form_definitions')**:
   - Cấu hình form đặt chỗ, liên hệ, khảo sát dạng JSON Schema kèm URL đích ('submit_action_url').
