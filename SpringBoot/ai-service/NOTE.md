# AI Service - Dịch Vụ Trợ Lý Trí Tuệ Nhân Tạo & Vector Knowledge Base

## 1. Giới thiệu tổng quan
`ai-service` là Microservice chuyên biệt phụ trách toàn bộ nghiệp vụ AI Chatbot, RAG (Retrieval-Augmented Generation), quản lý Session hội thoại và Knowledge Base theo mô hình **Database-per-Domain**.

- **Cổng chạy mặc định**: `8086` (`http://localhost:8086`)
- **Định tuyến qua Gateway**: `/api/v1/ai/**`, `/api/ai/**`
- **Cơ sở dữ liệu độc lập**: `db_ai_vector` (MySQL 8.0)

## 2. Danh sách bảng cơ sở dữ liệu (`db_ai_vector`)
1. `tenant_ai_configs`: Cấu hình LLM theo từng Tenant (API key OpenAI/Claude/Gemini, model_name, system_prompt, temperature, max_tokens).
2. `ai_chat_sessions`: Phiên làm việc và lịch sử hội thoại giữa người dùng/khách truy cập và bot.
3. `ai_chat_messages`: Chi tiết từng câu hỏi/câu trả lời (role user/assistant/system, token sử dụng, model thực thi).
4. `ai_knowledge_base`: Cơ sở tri thức tài liệu được phân mảnh (chunks) phục vụ truy vấn RAG.
5. `ai_vector_embeddings`: Lưu trữ vector embedding 1536 chiều tương ứng với từng chunk tri thức.
6. `ai_prompts`: Thư viện mẫu câu nhắc (Prompt Templates) cho từng tác vụ chuyên sâu.

## 3. Quy trình xử lý RAG & Chat
1. Client gửi câu hỏi -> `ai-service` lấy ngữ cảnh session từ `ai_chat_sessions`.
2. Trích xuất embedding vector câu hỏi -> Tìm kiếm độ tương đồng với `ai_vector_embeddings` trong `db_ai_vector`.
3. Gửi prompt tích hợp ngữ cảnh tới LLM Provider -> Lưu câu trả lời vào `ai_chat_messages` và phản hồi về Client.
