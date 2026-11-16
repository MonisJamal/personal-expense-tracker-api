package com.monis.expensetracker.repository;

import com.monis.expensetracker.entity.ExpenseCategory;

import java.math.BigDecimal;

public interface CategorySummaryProjection {
    ExpenseCategory getCategory();
    BigDecimal getTotalAmount();
    Long getCount();
}
