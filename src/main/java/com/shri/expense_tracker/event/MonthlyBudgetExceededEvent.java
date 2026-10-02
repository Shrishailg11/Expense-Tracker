package com.shri.expense_tracker.event;

import java.math.BigDecimal;

public class MonthlyBudgetExceededEvent {
    private final Long userId;
    private final String userEmail;
    private final BigDecimal budget;
    private final BigDecimal spent;
    private final String monthLabel;

    public MonthlyBudgetExceededEvent(Long userId, String userEmail, BigDecimal budget,
                                      BigDecimal spent, String monthLabel) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.budget = budget;
        this.spent = spent;
        this.monthLabel = monthLabel;
    }

    public Long getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public BigDecimal getBudget() { return budget; }
    public BigDecimal getSpent() { return spent; }
    public String getMonthLabel() { return monthLabel; }
}