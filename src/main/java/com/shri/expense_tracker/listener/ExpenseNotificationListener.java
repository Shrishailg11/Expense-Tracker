package com.shri.expense_tracker.listener;

import com.shri.expense_tracker.event.ExpenseCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ExpenseNotificationListener {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("10000");

    @EventListener
    public void onExpenseCreated(ExpenseCreatedEvent event) {
        if (event.getAmount().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            System.out.printf(
                    "[NOTIFICATION] Would email %s: large expense '%s' of ₹%s logged.%n",
                    event.getUserEmail(), event.getDescription(), event.getAmount());
        }
    }
}