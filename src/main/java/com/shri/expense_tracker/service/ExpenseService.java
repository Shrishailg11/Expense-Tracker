package com.shri.expense_tracker.service;

import com.shri.expense_tracker.dto.ExpenseDto;
import com.shri.expense_tracker.exception.ResourceNotFoundException;
import com.shri.expense_tracker.model.Category;
import com.shri.expense_tracker.model.Expense;
import com.shri.expense_tracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> getAll() {
        return expenseRepository.findAll();
    }

    public Expense getById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
    }

    public List<Expense> getByCategory(Category category) {
        return expenseRepository.findByCategory(category);
    }

    public List<Expense> getByDateRange(LocalDate start, LocalDate end) {
        return expenseRepository.findByDateBetween(start, end);
    }

    public Expense create(ExpenseDto dto) {
        Expense expense = new Expense(dto.description(), dto.amount(), dto.category(), dto.date());
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense update(Long id, ExpenseDto dto) {
        Expense existing = getById(id);
        existing.setDescription(dto.description());
        existing.setAmount(dto.amount());
        existing.setCategory(dto.category());
        existing.setDate(dto.date());
        return expenseRepository.save(existing);
    }

    public void delete(Long id) {
        Expense existing = getById(id);
        expenseRepository.delete(existing);
    }

    public BigDecimal getTotalSpend() {
        return expenseRepository.getTotalSpend();
    }

    public BigDecimal getTotalSpendByCategory(Category category) {
        return expenseRepository.getTotalSpendByCategory(category);
    }
}
