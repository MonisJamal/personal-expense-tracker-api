package com.monis.expensetracker.service;

import com.monis.expensetracker.dto.ExpenseRequestDto;
import com.monis.expensetracker.dto.ExpenseResponseDto;
import com.monis.expensetracker.dto.MonthlySummaryResponseDto;
import com.monis.expensetracker.dto.PaginatedResponseDto;
import com.monis.expensetracker.entity.ExpenseCategory;

import java.time.LocalDate;

public interface ExpenseService {

    ExpenseResponseDto createExpense(ExpenseRequestDto requestDto);

    PaginatedResponseDto<ExpenseResponseDto> getExpenses(
            ExpenseCategory category, LocalDate startDate, LocalDate endDate,
            int page, int size, String sortBy, String sortDir);

    ExpenseResponseDto getExpenseById(Long id);

    ExpenseResponseDto updateExpense(Long id, ExpenseRequestDto requestDto);

    void deleteExpense(Long id);

    MonthlySummaryResponseDto getMonthlySummary(int year, int month);
}
