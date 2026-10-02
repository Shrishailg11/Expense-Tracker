package com.shri.expense_tracker.event;

import java.math.BigDecimal;

public record MonthlyBudgetExceededMessage(
        Long userId,
        String userEmail,
        BigDecimal budget,
        BigDecimal spent,
        String monthLabel
) {}