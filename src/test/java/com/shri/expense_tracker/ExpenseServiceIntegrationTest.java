package com.shri.expense_tracker;


import com.shri.expense_tracker.dto.ExpenseDto;
import com.shri.expense_tracker.exception.ResourceNotFoundException;
import com.shri.expense_tracker.model.Category;
import com.shri.expense_tracker.model.Expense;
import com.shri.expense_tracker.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional // each test method's DB changes are rolled back automatically after it finishes
class ExpenseServiceIntegrationTest {

    @Autowired
    private ExpenseService expenseService;

    @Test
    void create_thenFetch_shouldReturnSameExpense() {
        Expense created = expenseService.create(
                new ExpenseDto("Coffee", new BigDecimal("4.50"), Category.FOOD, LocalDate.now()));

        Expense fetched = expenseService.getById(created.getId());

        assertThat(fetched.getDescription()).isEqualTo("Coffee");
        assertThat(fetched.getCategory()).isEqualTo(Category.FOOD);
    }

    @Test
    void getById_withInvalidId_shouldThrow() {
        assertThatThrownBy(() -> expenseService.getById(9999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByCategory_shouldOnlyReturnMatchingCategory() {
        expenseService.create(new ExpenseDto("Bus ticket", new BigDecimal("2.00"), Category.TRAVEL, LocalDate.now()));
        expenseService.create(new ExpenseDto("Pizza", new BigDecimal("12.00"), Category.FOOD, LocalDate.now()));

        List<Expense> travelExpenses = expenseService.getByCategory(Category.TRAVEL);

        assertThat(travelExpenses).allMatch(e -> e.getCategory() == Category.TRAVEL);
    }

    @Test
    void getTotalSpendByCategory_shouldSumOnlyMatchingCategory() {
        expenseService.create(new ExpenseDto("Snacks", new BigDecimal("10.00"), Category.FOOD, LocalDate.now()));
        expenseService.create(new ExpenseDto("Dinner", new BigDecimal("25.00"), Category.FOOD, LocalDate.now()));

        BigDecimal foodTotal = expenseService.getTotalSpendByCategory(Category.FOOD);

        assertThat(foodTotal).isEqualByComparingTo(new BigDecimal("35.00"));
    }
}
