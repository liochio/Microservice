package com.liochio.worker.repository;

import com.liochio.worker.entity.DlqMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DlqMessageRepository extends JpaRepository<DlqMessageEntity, Long> {
    List<DlqMessageEntity> findTop50ByStatusOrderByCreatedAtAsc(String status);
    Page<DlqMessageEntity> findByStatus(String status, Pageable pageable);
    long countByStatus(String status);
}
