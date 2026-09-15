package com.liochio.common.config;

import com.liochio.common.constant.HeaderConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ==============================================================================
 * Cấu Hình Tài Liệu Hóa OpenAPI 3 / Swagger (SpringDoc OpenAPI Configuration)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tự động sinh tài liệu API trực quan tại '/swagger-ui.html' và '/v3/api-docs'.
 * - Tích hợp cấu hình xác thực JWT Bearer Token trực tiếp trên giao diện Swagger.
 * - Tự động thêm Header 'X-Tenant-ID' và 'Accept-Language' vào mọi API request.
 * 
 * Khi nào gọi:
 * - Được SpringDoc khởi tạo khi ứng dụng khởi chạy.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Liochio FinTech Core Banking & Digital Finance Platform API")
                        .description("Tài liệu kỹ thuật và cổng kết nối RESTful API cho Hệ thống Ngân hàng số và Quản lý Tài chính Microservices.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Liochio FinTech Engineering Team")
                                .email("engineering@fintech.liochio.com"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public OperationCustomizer customizeGlobalHeaders() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(new HeaderParameter()
                    .name(HeaderConstants.X_TENANT_ID)
                    .description("Mã định danh khách thuê (Multi-Tenancy)")
                    .required(false)
                    .example("default"));

            operation.addParametersItem(new HeaderParameter()
                    .name(HeaderConstants.ACCEPT_LANGUAGE)
                    .description("Ngôn ngữ phản hồi (vi, en, zh)")
                    .required(false)
                    .example("vi"));

            return operation;
        };
    }
}
