package com.liochio.worker.repository;

import com.liochio.worker.entity.MailLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MailLogRepository extends JpaRepository<MailLogEntity, Long> {
    List<MailLogEntity> findTop50ByStatusInOrderByCreatedAtAsc(List<String> statuses);
    Page<MailLogEntity> findByStatus(String status, Pageable pageable);
    Page<MailLogEntity> findByRecipientContaining(String recipient, Pageable pageable);
    Optional<MailLogEntity> findByTraceId(String traceId);
    long countByStatus(String status);
}
