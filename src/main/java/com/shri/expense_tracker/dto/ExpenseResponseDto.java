package com.shri.expense_tracker.dto;

import com.shri.expense_tracker.model.Category;
import com.shri.expense_tracker.model.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponseDto(
        Long id,
        String description,
        BigDecimal amount,
        Category category,
        LocalDate date,
        Long userId,
        String userName
) {
    public static ExpenseResponseDto from(Expense expense) {
        return new ExpenseResponseDto(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate(),
                expense.getUser().getId(),
                expense.getUser().getName()
        );
    }
}