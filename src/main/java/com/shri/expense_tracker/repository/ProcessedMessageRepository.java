package com.shri.expense_tracker.repository;

import com.shri.expense_tracker.model.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, Long> {
    boolean existsByMessageKey(String messageKey);
}