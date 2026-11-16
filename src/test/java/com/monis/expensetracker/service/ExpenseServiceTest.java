package com.monis.expensetracker.service;

import com.monis.expensetracker.dto.ExpenseRequestDto;
import com.monis.expensetracker.dto.ExpenseResponseDto;
import com.monis.expensetracker.entity.Expense;
import com.monis.expensetracker.entity.ExpenseCategory;
import com.monis.expensetracker.entity.Role;
import com.monis.expensetracker.entity.User;
import com.monis.expensetracker.exception.ResourceNotFoundException;
import com.monis.expensetracker.repository.ExpenseRepository;
import com.monis.expensetracker.service.impl.ExpenseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private User sampleUser;
    private Expense sampleExpense;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Alice Johnson", "alice@example.com", "pass", Set.of(Role.ROLE_USER));
        sampleExpense = new Expense(100L, "Coffee & Bagel", "Morning snack",
                new BigDecimal("150.00"), LocalDate.now(), ExpenseCategory.FOOD, sampleUser);
    }

    @Test
    @DisplayName("Create expense associates with current authenticated user")
    void shouldCreateExpenseForCurrentUser() {
        ExpenseRequestDto request = new ExpenseRequestDto("Coffee & Bagel", "Morning snack",
                new BigDecimal("150.00"), LocalDate.now(), ExpenseCategory.FOOD);

        when(authService.getCurrentAuthenticatedUser()).thenReturn(sampleUser);
        when(expenseRepository.save(any(Expense.class))).thenReturn(sampleExpense);

        ExpenseResponseDto response = expenseService.createExpense(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Coffee & Bagel");
        assertThat(response.getCategory()).isEqualTo(ExpenseCategory.FOOD);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("Get expense by ID throws ResourceNotFoundException when expense does not belong to user")
    void shouldThrowWhenExpenseNotFoundOrNotBelongingToUser() {
        when(authService.getCurrentAuthenticatedUser()).thenReturn(sampleUser);
        when(expenseRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpenseById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Expense not found with id: '999'");
    }

    @Test
    @DisplayName("Delete expense successfully when owned by authenticated user")
    void shouldDeleteExpenseOwnedByUser() {
        when(authService.getCurrentAuthenticatedUser()).thenReturn(sampleUser);
        when(expenseRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleExpense));

        expenseService.deleteExpense(100L);

        verify(expenseRepository).delete(sampleExpense);
    }
}
