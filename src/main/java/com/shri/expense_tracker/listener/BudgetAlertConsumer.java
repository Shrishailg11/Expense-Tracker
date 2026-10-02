package com.shri.expense_tracker.listener;

import com.shri.expense_tracker.config.RabbitMQConfig;
import com.shri.expense_tracker.event.MonthlyBudgetExceededMessage;
import com.shri.expense_tracker.model.ProcessedMessage;
import com.shri.expense_tracker.repository.ProcessedMessageRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BudgetAlertConsumer {

    private final ProcessedMessageRepository processedMessageRepository;

    public BudgetAlertConsumer(ProcessedMessageRepository processedMessageRepository) {
        this.processedMessageRepository = processedMessageRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.BUDGET_ALERTS_QUEUE)
    @Transactional
    public void onBudgetExceeded(MonthlyBudgetExceededMessage message) {
        String messageKey = "budget-exceeded-" + message.userId() + "-" + message.monthLabel();

        if (processedMessageRepository.existsByMessageKey(messageKey)) {
            System.out.printf("[IDEMPOTENCY] Skipping already-alerted budget: %s%n", messageKey);
            return;
        }

        System.out.printf(
                "[BUDGET ALERT] Would email %s: spent Rs.%s of ₹Rs.%s budget for %s.%n",
                message.userEmail(), message.spent(), message.budget(), message.monthLabel());

        try {
            processedMessageRepository.save(new ProcessedMessage(messageKey));
        } catch (DataIntegrityViolationException e) {
            System.out.printf("[IDEMPOTENCY] Race detected, already recorded: %s%n", messageKey);
        }
    }
}