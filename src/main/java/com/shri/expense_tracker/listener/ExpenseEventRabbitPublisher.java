package com.shri.expense_tracker.listener;

import com.shri.expense_tracker.config.RabbitMQConfig;
import com.shri.expense_tracker.event.ExpenseCreatedEvent;
import com.shri.expense_tracker.event.ExpenseCreatedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ExpenseEventRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    public ExpenseEventRabbitPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void forwardToRabbit(ExpenseCreatedEvent event) {
        ExpenseCreatedMessage message = new ExpenseCreatedMessage(
                event.getExpenseId(), event.getDescription(), event.getAmount(), event.getUserEmail());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.EXPENSE_CREATED_ROUTING_KEY,
                message);
    }
}