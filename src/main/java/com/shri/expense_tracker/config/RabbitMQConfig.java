package com.shri.expense_tracker.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "expense.exchange";
    public static final String NOTIFICATIONS_QUEUE = "expense.notifications.queue";
    public static final String EXPENSE_CREATED_ROUTING_KEY = "expense.created";

    public static final String DLX = "expense.dlx";
    public static final String DLQ = "expense.notifications.dlq";
    public static final String DLQ_ROUTING_KEY = "expense.created.dlq";

    public static final String BUDGET_ALERTS_QUEUE = "budget.alerts.queue";
    public static final String BUDGET_EXCEEDED_ROUTING_KEY = "budget.exceeded";

    @Bean
    public TopicExchange expenseExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, TopicExchange expenseExchange) {
        return BindingBuilder.bind(notificationsQueue).to(expenseExchange).with(EXPENSE_CREATED_ROUTING_KEY);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, DLX, DLQ_ROUTING_KEY);
    }

    @Bean
    public Queue budgetAlertsQueue() {
        return QueueBuilder.durable(BUDGET_ALERTS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding budgetAlertsBinding(Queue budgetAlertsQueue, TopicExchange expenseExchange) {
        return BindingBuilder.bind(budgetAlertsQueue).to(expenseExchange).with(BUDGET_EXCEEDED_ROUTING_KEY);
    }
}