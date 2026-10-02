package com.shri.expense_tracker.listener;

import com.shri.expense_tracker.config.RabbitMQConfig;
import com.shri.expense_tracker.event.ExpenseCreatedMessage;
import com.shri.expense_tracker.model.ProcessedMessage;
import com.shri.expense_tracker.repository.ProcessedMessageRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class ExpenseNotificationConsumer {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("10000");

    private final ProcessedMessageRepository processedMessageRepository;

    public ExpenseNotificationConsumer(ProcessedMessageRepository processedMessageRepository) {
        this.processedMessageRepository = processedMessageRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATIONS_QUEUE)
    @Transactional
    public void onExpenseCreatedMessage(ExpenseCreatedMessage message) {
        String messageKey = "expense-created-" + message.expenseId();

        if (processedMessageRepository.existsByMessageKey(messageKey)) {
            System.out.printf("[IDEMPOTENCY] Skipping already-processed message: %s%n", messageKey);
            return;
        }

        if (message.amount().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            System.out.printf(
                    "[RABBITMQ NOTIFICATION] Would email %s: large expense '%s' of Rs.%s logged.%n",
                    message.userEmail(), message.description(), message.amount());
        }

        try {
            processedMessageRepository.save(new ProcessedMessage(messageKey));
        } catch (DataIntegrityViolationException e) {
            System.out.printf("[IDEMPOTENCY] Race detected, message already recorded: %s%n", messageKey);
        }
    }
}