package com.shri.expense_tracker.repository;

import com.shri.expense_tracker.model.MonthlySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlySummaryRepository extends JpaRepository<MonthlySummary, Long> {
    Optional<MonthlySummary> findByUserIdAndMonth(Long userId, String month);
}