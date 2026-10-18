package com.shri.expense_tracker.listener;

import com.shri.expense_tracker.config.RabbitMQConfig;
import com.shri.expense_tracker.event.ExpenseCreatedMessage;
import com.shri.expense_tracker.model.ProcessedMessage;
import com.shri.expense_tracker.repository.ProcessedMessageRepository;
import com.shri.expense_tracker.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class ExpenseNotificationConsumer {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("10000");

    private final ProcessedMessageRepository processedMessageRepository;
    private final EmailService emailService;

    public ExpenseNotificationConsumer(ProcessedMessageRepository processedMessageRepository,
                                       EmailService emailService) {
        this.processedMessageRepository = processedMessageRepository;
        this.emailService = emailService;
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
            String subject = "Large expense recorded: " + message.description();
            String text = """
                    Hi,

                    A large expense was just recorded on your account.

                    Description: %s
                    Amount: Rs.%s

                    If this wasn't you, please review your account.
                    """.formatted(message.description(), message.amount());

            emailService.send(message.userEmail(), subject, text);
            System.out.printf("[EMAIL SENT] expense notification to %s%n", message.userEmail());
        }

        try {
            processedMessageRepository.save(new ProcessedMessage(messageKey));
        } catch (DataIntegrityViolationException e) {
            System.out.printf("[IDEMPOTENCY] Race detected, message already recorded: %s%n", messageKey);
        }
    }
}