package com.shri.expense_tracker.repository;


import com.shri.expense_tracker.model.Category;
import com.shri.expense_tracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Derived query - Spring Data builds this from the method name (same pattern as the Todo app)
    List<Expense> findByCategory(Category category);

    List<Expense> findByDateBetween(LocalDate startDate, LocalDate endDate);

    // Aggregation - method-name derivation can't express "sum a column", so we write JPQL directly
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
    BigDecimal getTotalSpend();

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.category = :category")
    BigDecimal getTotalSpendByCategory(@Param("category") Category category);
}