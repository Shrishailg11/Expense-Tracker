package com.shri.expense_tracker.listener;

import com.shri.expense_tracker.config.RabbitMQConfig;
import com.shri.expense_tracker.event.MonthlyBudgetExceededEvent;
import com.shri.expense_tracker.event.MonthlyBudgetExceededMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BudgetExceededEventRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    public BudgetExceededEventRabbitPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void forwardToRabbit(MonthlyBudgetExceededEvent event) {
        MonthlyBudgetExceededMessage message = new MonthlyBudgetExceededMessage(
                event.getUserId(), event.getUserEmail(), event.getBudget(), event.getSpent(), event.getMonthLabel());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE, RabbitMQConfig.BUDGET_EXCEEDED_ROUTING_KEY, message);
    }
}