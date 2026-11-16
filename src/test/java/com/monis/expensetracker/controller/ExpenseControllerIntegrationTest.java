package com.monis.expensetracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monis.expensetracker.dto.ExpenseRequestDto;
import com.monis.expensetracker.entity.Expense;
import com.monis.expensetracker.entity.ExpenseCategory;
import com.monis.expensetracker.entity.Role;
import com.monis.expensetracker.entity.User;
import com.monis.expensetracker.repository.ExpenseRepository;
import com.monis.expensetracker.repository.UserRepository;
import com.monis.expensetracker.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String validJwt;
    private User testUser;

    @BeforeEach
    void initContext() {
        expenseRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("Developer Test");
        testUser.setEmail("dev.test@tracker.org");
        testUser.setPassword("dummyEncoded");
        testUser.setRoles(Set.of(Role.ROLE_USER));
        testUser = userRepository.save(testUser);

        validJwt = "Bearer " + jwtTokenProvider.generateTokenFromUsername(testUser.getEmail());
    }

    @Test
    @DisplayName("GET /api/v1/expenses without authorization header returns 401 Unauthorized")
    void shouldBlockUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/expenses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/expenses with valid JWT creates expense and returns 201")
    void shouldCreateExpenseWhenAuthenticated() throws Exception {
        ExpenseRequestDto dto = new ExpenseRequestDto(
                "Coworking Desk Fee",
                "Monthly flex desk subscription",
                new BigDecimal("4500.00"),
                LocalDate.now(),
                ExpenseCategory.HOUSING
        );

        mockMvc.perform(post("/api/v1/expenses")
                        .header("Authorization", validJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Coworking Desk Fee")))
                .andExpect(jsonPath("$.amount", is(4500.0)));
    }

    @Test
    @DisplayName("GET /api/v1/expenses/summary returns monthly aggregated metrics")
    void shouldCalculateMonthlySummary() throws Exception {
        LocalDate current = LocalDate.now();

        Expense exp1 = new Expense(
                null, "Organic Groceries", "Weekly shopping",
                new BigDecimal("1200.00"), current.withDayOfMonth(2),
                ExpenseCategory.FOOD, testUser
        );
        Expense exp2 = new Expense(
                null, "Subway Commute", "Travel to office",
                new BigDecimal("300.00"), current.withDayOfMonth(5),
                ExpenseCategory.TRANSPORTATION, testUser
        );
        expenseRepository.save(exp1);
        expenseRepository.save(exp2);

        mockMvc.perform(get("/api/v1/expenses/summary")
                        .header("Authorization", validJwt)
                        .param("year", String.valueOf(current.getYear()))
                        .param("month", String.valueOf(current.getMonthValue())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year", is(current.getYear())))
                .andExpect(jsonPath("$.month", is(current.getMonthValue())))
                .andExpect(jsonPath("$.totalSpent", is(1500.0)))
                .andExpect(jsonPath("$.totalTransactions", is(2)))
                .andExpect(jsonPath("$.categories", hasSize(2)));
    }
}
