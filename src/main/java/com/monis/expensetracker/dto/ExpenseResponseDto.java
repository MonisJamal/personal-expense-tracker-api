package com.monis.expensetracker.dto;

import com.monis.expensetracker.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenseResponseDto {

    private Long id;
    private String title;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private ExpenseCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ExpenseResponseDto() {
    }

    public ExpenseResponseDto(Long id, String title, String description, BigDecimal amount,
                              LocalDate expenseDate, ExpenseCategory category,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
