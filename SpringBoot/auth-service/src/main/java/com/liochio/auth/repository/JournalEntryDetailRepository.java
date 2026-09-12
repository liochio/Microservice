package com.liochio.auth.repository;

import com.liochio.auth.entity.JournalEntryDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalEntryDetailRepository extends JpaRepository<JournalEntryDetailEntity, Long> {

    List<JournalEntryDetailEntity> findByJournalEntryId(Long journalEntryId);
}
