package com.shri.expense_tracker;

import com.shri.expense_tracker.controller.ExpenseController;
import com.shri.expense_tracker.dto.ExpenseDto;
import com.shri.expense_tracker.model.Category;
import com.shri.expense_tracker.model.Expense;
import com.shri.expense_tracker.service.ExpenseService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    @Test
    void getAll_shouldReturnListOfExpenses() throws Exception {
        Expense expense = new Expense("Groceries", new BigDecimal("45.50"), Category.FOOD, LocalDate.now());
        Page<Expense> page = new PageImpl<>(List.of(expense));
        when(expenseService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].description").value("Groceries"));
    }

    @Test
    void create_withBlankDescription_shouldReturn400() throws Exception {
        ExpenseDto invalidDto = new ExpenseDto("", new BigDecimal("10"), Category.FOOD, LocalDate.now());

        mockMvc.perform(post("/api/expenses")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withNegativeAmount_shouldReturn400() throws Exception {
        ExpenseDto invalidDto = new ExpenseDto("Suspicious refund", new BigDecimal("-10"), Category.FOOD, LocalDate.now());

        mockMvc.perform(post("/api/expenses")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withValidData_shouldReturn201() throws Exception {
        ExpenseDto dto = new ExpenseDto("Groceries", new BigDecimal("45.50"), Category.FOOD, LocalDate.now());
        Expense saved = new Expense("Groceries", new BigDecimal("45.50"), Category.FOOD, LocalDate.now());
        when(expenseService.create(any(ExpenseDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/expenses")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Groceries"));
    }

    @Test
    void getByCategory_withInvalidCategoryInUrl_shouldReturn400WithValidValuesListed() throws Exception {
        mockMvc.perform(get("/api/expenses/category/food")) // lowercase - the exact bug tested manually earlier
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Valid values are")));
    }

    @Test
    void getTotalSpend_shouldReturnSum() throws Exception {
        when(expenseService.getTotalSpend()).thenReturn(new BigDecimal("150.75"));

        mockMvc.perform(get("/api/expenses/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("150.75"));
    }
}