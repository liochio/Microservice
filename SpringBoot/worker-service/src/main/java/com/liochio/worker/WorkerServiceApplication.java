package com.liochio.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.liochio.worker", "com.liochio.common"})
@EntityScan(basePackages = {"com.liochio.worker.entity", "com.liochio.common.entity"})
@EnableJpaRepositories(basePackages = {"com.liochio.worker.repository", "com.liochio.common.repository"})
public class WorkerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkerServiceApplication.class, args);
    }
}
