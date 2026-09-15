# Package: com.liochio.common.i18n

## 1. Vai trò & Chức năng
- Cung cấp hạ tầng đa ngôn ngữ (i18n) toàn diện từ tầng Controller đến Exception Handler.
- Tự động phân giải ngôn ngữ qua Header 'Accept-Language' (hỗ trợ 'vi', 'en', 'zh').

## 2. Các thành phần chính
- 'I18nConfig.java': Cấu hình 'AcceptHeaderLocaleResolver' và 'ReloadableResourceBundleMessageSource'.
- 'MessageService.java': Tiện ích lấy nội dung thông báo đã dịch theo Message Key.
- 'messages_{lang}.properties': Tập tin tài nguyên ngôn ngữ UTF-8.
