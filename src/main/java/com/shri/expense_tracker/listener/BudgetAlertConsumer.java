package com.shri.expense_tracker.listener;

import com.shri.expense_tracker.config.RabbitMQConfig;
import com.shri.expense_tracker.event.MonthlyBudgetExceededMessage;
import com.shri.expense_tracker.model.ProcessedMessage;
import com.shri.expense_tracker.repository.ProcessedMessageRepository;
import com.shri.expense_tracker.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BudgetAlertConsumer {

    private final ProcessedMessageRepository processedMessageRepository;
    private final EmailService emailService;

    public BudgetAlertConsumer(ProcessedMessageRepository processedMessageRepository,
                               EmailService emailService) {
        this.processedMessageRepository = processedMessageRepository;
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitMQConfig.BUDGET_ALERTS_QUEUE)
    @Transactional
    public void onBudgetExceeded(MonthlyBudgetExceededMessage message) {
        String messageKey = "budget-exceeded-" + message.userId() + "-" + message.monthLabel();

        if (processedMessageRepository.existsByMessageKey(messageKey)) {
            System.out.printf("[IDEMPOTENCY] Skipping already-alerted budget: %s%n", messageKey);
            return;
        }

        String subject = "Monthly budget exceeded for " + message.monthLabel();
        String text = """
                Hi,

                Your spending for %s has gone over your monthly budget.

                Spent: Rs.%s
                Budget: Rs.%s
                """.formatted(message.monthLabel(), message.spent(), message.budget());

        emailService.send(message.userEmail(), subject, text);
        System.out.printf("[EMAIL SENT] budget alert to %s%n", message.userEmail());

        try {
            processedMessageRepository.save(new ProcessedMessage(messageKey));
        } catch (DataIntegrityViolationException e) {
            System.out.printf("[IDEMPOTENCY] Race detected, already recorded: %s%n", messageKey);
        }
    }
}