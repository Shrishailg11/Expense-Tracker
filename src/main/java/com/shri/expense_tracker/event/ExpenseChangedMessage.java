package com.shri.expense_tracker.event;

import java.util.Set;

public record ExpenseChangedMessage(Long userId, Set<String> affectedMonths) {}