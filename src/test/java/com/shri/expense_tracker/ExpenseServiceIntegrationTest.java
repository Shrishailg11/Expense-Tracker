package com.shri.expense_tracker;

import com.shri.expense_tracker.dto.ExpenseDto;
import com.shri.expense_tracker.dto.ExpenseResponseDto;
import com.shri.expense_tracker.exception.ResourceNotFoundException;
import com.shri.expense_tracker.model.Category;
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
@Transactional
class ExpenseServiceIntegrationTest {

    @Autowired
    private ExpenseService expenseService;

    @Test
    void create_thenFetch_shouldReturnSameExpense() {
        ExpenseResponseDto created = expenseService.create(
                new ExpenseDto("Coffee", new BigDecimal("4.50"), Category.FOOD, LocalDate.now(), 1L));

        ExpenseResponseDto fetched = expenseService.getById(created.id());

        assertThat(fetched.description()).isEqualTo("Coffee");
        assertThat(fetched.category()).isEqualTo(Category.FOOD);
    }

    @Test
    void getById_withInvalidId_shouldThrow() {
        assertThatThrownBy(() -> expenseService.getById(9999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByCategory_shouldOnlyReturnMatchingCategory() {
        expenseService.create(new ExpenseDto("Bus ticket", new BigDecimal("2.00"), Category.TRAVEL, LocalDate.now(), 1L));
        expenseService.create(new ExpenseDto("Pizza", new BigDecimal("12.00"), Category.FOOD, LocalDate.now(), 1L));

        List<ExpenseResponseDto> travelExpenses = expenseService.getByCategory(Category.TRAVEL);

        assertThat(travelExpenses).allMatch(e -> e.category() == Category.TRAVEL);
    }

    @Test
    void getTotalSpendByCategory_shouldSumOnlyMatchingCategory() {
        expenseService.create(new ExpenseDto("Snacks", new BigDecimal("10.00"), Category.FOOD, LocalDate.now(), 1L));
        expenseService.create(new ExpenseDto("Dinner", new BigDecimal("25.00"), Category.FOOD, LocalDate.now(), 1L));

        BigDecimal foodTotal = expenseService.getTotalSpendByCategory(Category.FOOD);

        assertThat(foodTotal).isEqualByComparingTo(new BigDecimal("35.00"));
    }
}