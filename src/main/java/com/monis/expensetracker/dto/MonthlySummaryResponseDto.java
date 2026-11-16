package com.monis.expensetracker.dto;

import java.math.BigDecimal;
import java.util.List;

public class MonthlySummaryResponseDto {

    private int year;
    private int month;
    private BigDecimal totalSpent;
    private long totalTransactions;
    private List<CategorySpendingDto> categories;

    public MonthlySummaryResponseDto() {
    }

    public MonthlySummaryResponseDto(int year, int month, BigDecimal totalSpent,
                                     long totalTransactions, List<CategorySpendingDto> categories) {
        this.year = year;
        this.month = month;
        this.totalSpent = totalSpent;
        this.totalTransactions = totalTransactions;
        this.categories = categories;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public List<CategorySpendingDto> getCategories() {
        return categories;
    }

    public void setCategories(List<CategorySpendingDto> categories) {
        this.categories = categories;
    }
}
