package com.liochio.auth.repository;

import com.liochio.auth.entity.ApprovalHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistoryEntity, Long> {
    List<ApprovalHistoryEntity> findByApprovalRequestIdOrderByCreatedAtAsc(Long approvalRequestId);
}
