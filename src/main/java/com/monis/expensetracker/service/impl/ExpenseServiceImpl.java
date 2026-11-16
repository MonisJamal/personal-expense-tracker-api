package com.monis.expensetracker.service.impl;

import com.monis.expensetracker.dto.CategorySpendingDto;
import com.monis.expensetracker.dto.ExpenseRequestDto;
import com.monis.expensetracker.dto.ExpenseResponseDto;
import com.monis.expensetracker.dto.MonthlySummaryResponseDto;
import com.monis.expensetracker.dto.PaginatedResponseDto;
import com.monis.expensetracker.entity.Expense;
import com.monis.expensetracker.entity.ExpenseCategory;
import com.monis.expensetracker.entity.User;
import com.monis.expensetracker.exception.ResourceNotFoundException;
import com.monis.expensetracker.repository.CategorySummaryProjection;
import com.monis.expensetracker.repository.ExpenseRepository;
import com.monis.expensetracker.service.AuthService;
import com.monis.expensetracker.service.ExpenseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "title", "amount", "expenseDate", "category", "createdAt"
    );

    private final ExpenseRepository expenseRepository;
    private final AuthService authService;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, AuthService authService) {
        this.expenseRepository = expenseRepository;
        this.authService = authService;
    }

    @Override
    public ExpenseResponseDto createExpense(ExpenseRequestDto requestDto) {
        User currentUser = authService.getCurrentAuthenticatedUser();

        Expense expense = new Expense();
        expense.setTitle(requestDto.getTitle().trim());
        expense.setDescription(requestDto.getDescription());
        expense.setAmount(requestDto.getAmount());
        expense.setExpenseDate(requestDto.getExpenseDate());
        expense.setCategory(requestDto.getCategory());
        expense.setUser(currentUser);

        Expense saved = expenseRepository.save(expense);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<ExpenseResponseDto> getExpenses(
            ExpenseCategory category, LocalDate startDate, LocalDate endDate,
            int page, int size, String sortBy, String sortDir) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Pageable pageable = createPageable(page, size, sortBy, sortDir);

        Page<Expense> expensePage;

        if (category != null && startDate != null && endDate != null) {
            expensePage = expenseRepository.findByUserIdAndCategoryAndExpenseDateBetween(
                    currentUser.getId(), category, startDate, endDate, pageable);
        } else if (category != null) {
            expensePage = expenseRepository.findByUserIdAndCategory(currentUser.getId(), category, pageable);
        } else if (startDate != null && endDate != null) {
            expensePage = expenseRepository.findByUserIdAndExpenseDateBetween(
                    currentUser.getId(), startDate, endDate, pageable);
        } else {
            expensePage = expenseRepository.findByUserId(currentUser.getId(), pageable);
        }

        return mapToPaginatedResponse(expensePage);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponseDto getExpenseById(Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Expense expense = expenseRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));
        return mapToDto(expense);
    }

    @Override
    public ExpenseResponseDto updateExpense(Long id, ExpenseRequestDto requestDto) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Expense expense = expenseRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));

        expense.setTitle(requestDto.getTitle().trim());
        expense.setDescription(requestDto.getDescription());
        expense.setAmount(requestDto.getAmount());
        expense.setExpenseDate(requestDto.getExpenseDate());
        expense.setCategory(requestDto.getCategory());

        Expense updated = expenseRepository.save(expense);
        return mapToDto(updated);
    }

    @Override
    public void deleteExpense(Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Expense expense = expenseRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));
        expenseRepository.delete(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlySummaryResponseDto getMonthlySummary(int year, int month) {
        User currentUser = authService.getCurrentAuthenticatedUser();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        BigDecimal totalSpent = expenseRepository.getTotalSpentInDateRange(currentUser.getId(), startDate, endDate);
        List<CategorySummaryProjection> projections =
                expenseRepository.getMonthlyCategorySummary(currentUser.getId(), startDate, endDate);

        long totalTransactions = 0;
        List<CategorySpendingDto> categories = new ArrayList<>();

        for (CategorySummaryProjection proj : projections) {
            totalTransactions += proj.getCount();
            double percentage = 0.0;
            if (totalSpent.compareTo(BigDecimal.ZERO) > 0) {
                percentage = proj.getTotalAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(totalSpent, 2, RoundingMode.HALF_UP)
                        .doubleValue();
            }
            categories.add(new CategorySpendingDto(
                    proj.getCategory(),
                    proj.getTotalAmount(),
                    percentage,
                    proj.getCount()
            ));
        }

        return new MonthlySummaryResponseDto(
                year,
                month,
                totalSpent,
                totalTransactions,
                categories
        );
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        String safeSort = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "expenseDate";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size)), Sort.by(direction, safeSort));
    }

    private PaginatedResponseDto<ExpenseResponseDto> mapToPaginatedResponse(Page<Expense> expensePage) {
        List<ExpenseResponseDto> content = expensePage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PaginatedResponseDto<>(
                content,
                expensePage.getNumber(),
                expensePage.getSize(),
                expensePage.getTotalElements(),
                expensePage.getTotalPages(),
                expensePage.isLast()
        );
    }

    private ExpenseResponseDto mapToDto(Expense expense) {
        return new ExpenseResponseDto(
                expense.getId(),
                expense.getTitle(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getCategory(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }
}
