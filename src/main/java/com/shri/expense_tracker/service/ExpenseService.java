package com.shri.expense_tracker.service;

import com.shri.expense_tracker.dto.ExpenseDto;
import com.shri.expense_tracker.dto.ExpenseResponseDto;
import com.shri.expense_tracker.exception.ResourceNotFoundException;
import com.shri.expense_tracker.model.Category;
import com.shri.expense_tracker.model.Expense;
import com.shri.expense_tracker.model.MonthlySummary;
import com.shri.expense_tracker.model.User;
import com.shri.expense_tracker.repository.ExpenseRepository;
import com.shri.expense_tracker.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shri.expense_tracker.event.ExpenseCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.shri.expense_tracker.event.ExpenseChangedEvent;
import com.shri.expense_tracker.dto.MonthlySummaryResponseDto;
import com.shri.expense_tracker.repository.MonthlySummaryRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.time.YearMonth;
import java.util.Set;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MonthlySummaryRepository monthlySummaryRepository;
    private final StringRedisTemplate redisTemplate;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository,
                          ApplicationEventPublisher eventPublisher, MonthlySummaryRepository monthlySummaryRepository, StringRedisTemplate redisTemplate) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.monthlySummaryRepository = monthlySummaryRepository;
        this.redisTemplate = redisTemplate;
    }

    private Expense findExpenseOrThrow(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDto> getAll() {
        return expenseRepository.findAll().stream()
                .map(ExpenseResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> getAll(Pageable pageable) {
        return expenseRepository.findAll(pageable).map(ExpenseResponseDto::from);
    }

    @Transactional(readOnly = true)
    public ExpenseResponseDto getById(Long id) {
        return ExpenseResponseDto.from(findExpenseOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDto> getByCategory(Category category) {
        return expenseRepository.findByCategory(category).stream()
                .map(ExpenseResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponseDto> getByDateRange(LocalDate start, LocalDate end) {
        return expenseRepository.findByDateBetween(start, end).stream()
                .map(ExpenseResponseDto::from)
                .toList();
    }

    @Transactional
    public ExpenseResponseDto create(ExpenseDto dto) {
        User owner = userRepository.findById(dto.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.userId()));

        Expense expense = new Expense(dto.description(), dto.amount(), dto.category(), dto.date());
        expense.setUser(owner);
        Expense saved = expenseRepository.save(expense);

        eventPublisher.publishEvent(new ExpenseCreatedEvent(
                saved.getId(), saved.getDescription(), saved.getAmount(), owner.getEmail()));

        String month = YearMonth.from(saved.getDate()).toString();
        eventPublisher.publishEvent(new ExpenseChangedEvent(owner.getId(), Set.of(month)));

        return ExpenseResponseDto.from(saved);
    }

    @Transactional
    public ExpenseResponseDto update(Long id, ExpenseDto dto) {
        Expense existing = findExpenseOrThrow(id);
        String oldMonth = YearMonth.from(existing.getDate()).toString();
        Long userId = existing.getUser().getId();

        existing.setDescription(dto.description());
        existing.setAmount(dto.amount());
        existing.setCategory(dto.category());
        existing.setDate(dto.date());
        Expense saved = expenseRepository.save(existing);

        String newMonth = YearMonth.from(saved.getDate()).toString();
        Set<String> affectedMonths = oldMonth.equals(newMonth) ? Set.of(oldMonth) : Set.of(oldMonth, newMonth);
        eventPublisher.publishEvent(new ExpenseChangedEvent(userId, affectedMonths));

        return ExpenseResponseDto.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Expense existing = findExpenseOrThrow(id);
        String month = YearMonth.from(existing.getDate()).toString();
        Long userId = existing.getUser().getId();

        expenseRepository.delete(existing);

        eventPublisher.publishEvent(new ExpenseChangedEvent(userId, Set.of(month)));
    }

    public BigDecimal getTotalSpend() {
        return expenseRepository.getTotalSpend();
    }

    public BigDecimal getTotalSpendByCategory(Category category) {
        return expenseRepository.getTotalSpendByCategory(category);
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponseDto getMonthlySummary(Long userId, String month) {
        String cacheKey = "summary:" + userId + ":" + month;

        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            System.out.println("[CACHE HIT] " + cacheKey);
            return new MonthlySummaryResponseDto(userId, month, new BigDecimal(cached));
        }

        System.out.println("[CACHE MISS] " + cacheKey);
        BigDecimal total = monthlySummaryRepository.findByUserIdAndMonth(userId, month)
                .map(MonthlySummary::getTotalSpent)
                .orElse(BigDecimal.ZERO);

        redisTemplate.opsForValue().set(cacheKey, total.toString(), Duration.ofHours(24));

        return new MonthlySummaryResponseDto(userId, month, total);
    }
}