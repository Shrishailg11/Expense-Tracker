package com.shri.expense_tracker.service;

import com.shri.expense_tracker.event.MonthlyBudgetExceededEvent;
import com.shri.expense_tracker.model.User;
import com.shri.expense_tracker.repository.ExpenseRepository;
import com.shri.expense_tracker.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class BudgetCheckService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BudgetCheckService(UserRepository userRepository, ExpenseRepository expenseRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "${budget.check.cron}")
    @Transactional(readOnly = true)
    public void checkAllBudgets() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        String monthLabel = YearMonth.from(today).toString();

        System.out.println("[BUDGET CHECK] Running for " + monthLabel);

        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getMonthlyBudget() == null) {
                System.out.println("[BUDGET CHECK] Skipping user " + user.getId() + " - no budget set");
                continue;
            }

            BigDecimal spent = expenseRepository.getTotalSpendByUserAndDateRange(user.getId(), monthStart, monthEnd);
            System.out.printf("[BUDGET CHECK] User %d: spent %s of %s%n", user.getId(), spent, user.getMonthlyBudget());

            if (spent.compareTo(user.getMonthlyBudget()) > 0) {
                eventPublisher.publishEvent(new MonthlyBudgetExceededEvent(
                        user.getId(), user.getEmail(), user.getMonthlyBudget(), spent, monthLabel));
            }
        }
    }
}