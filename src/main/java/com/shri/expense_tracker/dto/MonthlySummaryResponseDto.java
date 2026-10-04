package com.shri.expense_tracker.dto;

import java.math.BigDecimal;

public record MonthlySummaryResponseDto(Long userId, String month, BigDecimal totalSpent) {}