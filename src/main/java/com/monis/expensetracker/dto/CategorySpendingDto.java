package com.monis.expensetracker.dto;

import com.monis.expensetracker.entity.ExpenseCategory;

import java.math.BigDecimal;

public class CategorySpendingDto {

    private ExpenseCategory category;
    private BigDecimal totalAmount;
    private double percentage;
    private long transactionCount;

    public CategorySpendingDto() {
    }

    public CategorySpendingDto(ExpenseCategory category, BigDecimal totalAmount, double percentage, long transactionCount) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
        this.transactionCount = transactionCount;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }
}
