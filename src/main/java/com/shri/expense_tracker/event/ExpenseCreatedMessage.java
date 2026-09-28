package com.shri.expense_tracker.event;

import java.math.BigDecimal;

public record ExpenseCreatedMessage(
        Long expenseId,
        String description,
        BigDecimal amount,
        String userEmail
) {}