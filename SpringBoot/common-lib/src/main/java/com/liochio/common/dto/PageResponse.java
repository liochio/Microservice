package com.liochio.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * ==============================================================================
 * Chuẩn Hóa Phân Trang Dữ Liệu Toàn Hệ Thống (Standardized Page Response)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đóng gói kết quả phân trang từ Spring Data JPA `Page<T>` thành DTO gọn nhẹ,
 *   loại bỏ các trường metadata dư thừa của Spring Data giúp tiết kiệm băng thông mạng.
 * 
 * Khi nào gọi:
 * - Được Service hoặc Controller sử dụng khi trả về danh sách có phân trang
 *   (tìm kiếm portfolio, danh sách bài viết, danh mục, audit logs).
 * 
 * @param <T> Kiểu dữ liệu của phần tử trong danh sách
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * Danh sách phần tử trong trang hiện tại
     */
    private List<T> content;

    /**
     * Chỉ số trang hiện tại (0-indexed)
     */
    private int pageNumber;

    /**
     * Số lượng bản ghi tối đa trên một trang
     */
    private int pageSize;

    /**
     * Tổng số bản ghi thỏa mãn điều kiện lọc trên toàn bộ cơ sở dữ liệu
     */
    private long totalElements;

    /**
     * Tổng số trang tương ứng
     */
    private int totalPages;

    /**
     * Có phải trang cuối cùng hay không
     */
    private boolean isLast;

    /**
     * Có phải trang đầu tiên hay không
     */
    private boolean isFirst;

    /**
     * Chuyển đổi từ Spring Data `Page<T>` sang `PageResponse<T>`
     *
     * @param page Đối tượng Spring Data Page
     * @param <T>  Kiểu dữ liệu
     * @return PageResponse chuẩn hóa
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .isFirst(page.isFirst())
                .build();
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        return from(page);
    }
}
