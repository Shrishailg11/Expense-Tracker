package com.shri.expense_tracker.controller;

import com.shri.expense_tracker.dto.ExpenseDto;
import com.shri.expense_tracker.model.Category;
import com.shri.expense_tracker.model.Expense;
import com.shri.expense_tracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public List<Expense> getAll() {
        return expenseService.getAll();
    }

    @GetMapping("/{id}")
    public Expense getById(@PathVariable Long id) {
        return expenseService.getById(id);
    }

    @GetMapping("/category/{category}")
    public List<Expense> getByCategory(@PathVariable Category category) {
        return expenseService.getByCategory(category);
    }

    @GetMapping("/range")
    public List<Expense> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return expenseService.getByDateRange(start, end);
    }

    @GetMapping("/total")
    public BigDecimal getTotalSpend() {
        return expenseService.getTotalSpend();
    }

    @GetMapping("/total/category/{category}")
    public BigDecimal getTotalSpendByCategory(@PathVariable Category category) {
        return expenseService.getTotalSpendByCategory(category);
    }

    @PostMapping
    public ResponseEntity<Expense> create(@RequestBody @Valid ExpenseDto dto) {
        Expense created = expenseService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public Expense update(@PathVariable Long id, @RequestBody @Valid ExpenseDto dto) {
        return expenseService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}