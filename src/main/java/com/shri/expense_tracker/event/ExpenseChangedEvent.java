package com.shri.expense_tracker.event;

import java.util.Set;

public class ExpenseChangedEvent {
    private final Long userId;
    private final Set<String> affectedMonths;

    public ExpenseChangedEvent(Long userId, Set<String> affectedMonths) {
        this.userId = userId;
        this.affectedMonths = affectedMonths;
    }

    public Long getUserId() { return userId; }
    public Set<String> getAffectedMonths() { return affectedMonths; }
}