package com.shri.expense_tracker.listener;

import com.shri.expense_tracker.config.RabbitMQConfig;
import com.shri.expense_tracker.event.ExpenseChangedMessage;
import com.shri.expense_tracker.model.MonthlySummary;
import com.shri.expense_tracker.repository.ExpenseRepository;
import com.shri.expense_tracker.repository.MonthlySummaryRepository;
import com.shri.expense_tracker.repository.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Component
public class MonthlySummaryConsumer {

    private final ExpenseRepository expenseRepository;
    private final MonthlySummaryRepository monthlySummaryRepository;
    private final UserRepository userRepository;

    public MonthlySummaryConsumer(ExpenseRepository expenseRepository,
                                  MonthlySummaryRepository monthlySummaryRepository,
                                  UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.monthlySummaryRepository = monthlySummaryRepository;
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.SUMMARY_RECOMPUTE_QUEUE)
    @Transactional
    public void onExpenseChanged(ExpenseChangedMessage message) {
        for (String month : message.affectedMonths()) {
            recomputeOne(message.userId(), month);
        }
    }

    private void recomputeOne(Long userId, String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        BigDecimal total = expenseRepository.getTotalSpendByUserAndDateRange(userId, start, end);

        MonthlySummary summary = monthlySummaryRepository.findByUserIdAndMonth(userId, month)
                .orElseGet(() -> new MonthlySummary(userRepository.getReferenceById(userId), month, BigDecimal.ZERO));

        summary.setTotalSpent(total);
        summary.setLastUpdated(LocalDateTime.now());
        monthlySummaryRepository.save(summary);

        System.out.printf("[SUMMARY RECOMPUTE] user=%d month=%s total=%s%n", userId, month, total);
    }
}