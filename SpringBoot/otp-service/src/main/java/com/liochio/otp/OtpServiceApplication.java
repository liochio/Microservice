package com.liochio.otp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.liochio.common", "com.liochio.otp"})
@OpenAPIDefinition(
        info = @Info(
                title = "Dedicated OTP & SmartOTP Service API",
                version = "1.0",
                description = "Microservice độc lập chuyên biệt quản lý sinh, xác thực OTP, TOTP RFC 6238 SmartOTP và cờ Bật/Tắt Bypass cho môi trường Dev"
        )
)
public class OtpServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtpServiceApplication.class, args);
    }
}
