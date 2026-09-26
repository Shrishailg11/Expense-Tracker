package com.shri.expense_tracker.service;

import com.shri.expense_tracker.dto.ExpenseDto;
import com.shri.expense_tracker.dto.ExpenseResponseDto;
import com.shri.expense_tracker.exception.ResourceNotFoundException;
import com.shri.expense_tracker.model.Category;
import com.shri.expense_tracker.model.Expense;
import com.shri.expense_tracker.model.User;
import com.shri.expense_tracker.repository.ExpenseRepository;
import com.shri.expense_tracker.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shri.expense_tracker.event.ExpenseCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
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

        return ExpenseResponseDto.from(saved);
    }

    @Transactional
    public ExpenseResponseDto update(Long id, ExpenseDto dto) {
        Expense existing = findExpenseOrThrow(id);
        existing.setDescription(dto.description());
        existing.setAmount(dto.amount());
        existing.setCategory(dto.category());
        existing.setDate(dto.date());
        return ExpenseResponseDto.from(expenseRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        expenseRepository.delete(findExpenseOrThrow(id));
    }

    public BigDecimal getTotalSpend() {
        return expenseRepository.getTotalSpend();
    }

    public BigDecimal getTotalSpendByCategory(Category category) {
        return expenseRepository.getTotalSpendByCategory(category);
    }
}