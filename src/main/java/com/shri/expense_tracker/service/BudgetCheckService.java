package com.shri.expense_tracker.service;

import com.shri.expense_tracker.event.MonthlyBudgetExceededEvent;
import com.shri.expense_tracker.model.MonthlySummary;
import com.shri.expense_tracker.model.User;
import com.shri.expense_tracker.repository.MonthlySummaryRepository;
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
    private final MonthlySummaryRepository monthlySummaryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BudgetCheckService(UserRepository userRepository, MonthlySummaryRepository monthlySummaryRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.monthlySummaryRepository = monthlySummaryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "${budget.check.cron}")
    @Transactional(readOnly = true)
    public void checkAllBudgets() {
        String monthLabel = YearMonth.from(LocalDate.now()).toString();
        System.out.println("[BUDGET CHECK] Running for " + monthLabel);

        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getMonthlyBudget() == null) {
                System.out.println("[BUDGET CHECK] Skipping user " + user.getId() + " - no budget set");
                continue;
            }

            BigDecimal spent = monthlySummaryRepository.findByUserIdAndMonth(user.getId(), monthLabel)
                    .map(MonthlySummary::getTotalSpent)
                    .orElse(BigDecimal.ZERO);

            System.out.printf("[BUDGET CHECK] User %d: spent %s of %s%n", user.getId(), spent, user.getMonthlyBudget());

            if (spent.compareTo(user.getMonthlyBudget()) > 0) {
                eventPublisher.publishEvent(new MonthlyBudgetExceededEvent(
                        user.getId(), user.getEmail(), user.getMonthlyBudget(), spent, monthLabel));
            }
        }
    }
}