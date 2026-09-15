# Package: com.liochio.common.advice

## 1. Vai trò & Chức năng
- Tự động can thiệp vào quá trình phản hồi API và đo đạc hiệu năng thực thi bằng AOP.

## 2. Các thành phần chính
- 'GlobalResponseWrapper.java': 'ResponseBodyAdvice<Object>' tự động bọc response thành 'ApiResponse<T>'.
- 'LoggingAspect.java': AOP Aspect đo thời gian thực thi của Controller/Service và cảnh báo API chậm (>1000ms).
