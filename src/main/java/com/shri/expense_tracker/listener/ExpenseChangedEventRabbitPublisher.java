package com.shri.expense_tracker.listener;

import com.shri.expense_tracker.config.RabbitMQConfig;
import com.shri.expense_tracker.event.ExpenseChangedEvent;
import com.shri.expense_tracker.event.ExpenseChangedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ExpenseChangedEventRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    public ExpenseChangedEventRabbitPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void forwardToRabbit(ExpenseChangedEvent event) {
        ExpenseChangedMessage message = new ExpenseChangedMessage(event.getUserId(), event.getAffectedMonths());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.EXPENSE_CHANGED_ROUTING_KEY, message);
    }
}