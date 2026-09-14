package com.liochio.ledger.repository;

import com.liochio.ledger.entity.JournalEntryDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalEntryDetailRepository extends JpaRepository<JournalEntryDetailEntity, Long> {

    List<JournalEntryDetailEntity> findByJournalEntryId(Long journalEntryId);
}
