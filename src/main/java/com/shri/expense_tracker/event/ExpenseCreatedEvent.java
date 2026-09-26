package com.shri.expense_tracker.event;

import java.math.BigDecimal;

public class ExpenseCreatedEvent {
    private final Long expenseId;
    private final String description;
    private final BigDecimal amount;
    private final String userEmail;

    public ExpenseCreatedEvent(Long expenseId, String description, BigDecimal amount, String userEmail) {
        this.expenseId = expenseId;
        this.description = description;
        this.amount = amount;
        this.userEmail = userEmail;
    }

    public Long getExpenseId() { return expenseId; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public String getUserEmail() { return userEmail; }
}