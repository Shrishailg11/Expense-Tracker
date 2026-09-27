package com.shri.expense_tracker.listener;

import com.shri.expense_tracker.config.RabbitMQConfig;
import com.shri.expense_tracker.event.ExpenseCreatedMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ExpenseNotificationConsumer {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("10000");

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATIONS_QUEUE)
    public void onExpenseCreatedMessage(ExpenseCreatedMessage message) {
        if (message.amount().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            System.out.printf(
                    "[RABBITMQ NOTIFICATION] Would email %s: large expense '%s' of ₹%s logged.%n",
                    message.userEmail(), message.description(), message.amount());
        }
    }
}