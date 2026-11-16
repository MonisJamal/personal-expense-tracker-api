package com.monis.expensetracker.config;

import com.monis.expensetracker.entity.Expense;
import com.monis.expensetracker.entity.ExpenseCategory;
import com.monis.expensetracker.entity.Role;
import com.monis.expensetracker.entity.User;
import com.monis.expensetracker.repository.ExpenseRepository;
import com.monis.expensetracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ExpenseRepository expenseRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User demoUser = new User();
        demoUser.setName("Demo User");
        demoUser.setEmail("demo@expensetracker.com");
        demoUser.setPassword(passwordEncoder.encode("Password123!"));
        demoUser.setRoles(Set.of(Role.ROLE_USER));

        User savedUser = userRepository.save(demoUser);

        LocalDate now = LocalDate.now();

        Expense e1 = new Expense(null, "Monthly Apartment Rent", "House rent transfer",
                new BigDecimal("18000.00"), now.withDayOfMonth(1), ExpenseCategory.HOUSING, savedUser);
        Expense e2 = new Expense(null, "Supermarket Groceries", "Vegetables, fruits, and pantry essentials",
                new BigDecimal("3250.50"), now.minusDays(5), ExpenseCategory.FOOD, savedUser);
        Expense e3 = new Expense(null, "Electricity & Water Bill", "Utility bill payment",
                new BigDecimal("1450.00"), now.minusDays(8), ExpenseCategory.UTILITIES, savedUser);
        Expense e4 = new Expense(null, "Metro Commute Pass", "Monthly rail metro card recharge",
                new BigDecimal("1200.00"), now.minusDays(10), ExpenseCategory.TRANSPORTATION, savedUser);
        Expense e5 = new Expense(null, "Movie & Popcorn Night", "Cinema ticket with friends",
                new BigDecimal("850.00"), now.minusDays(12), ExpenseCategory.ENTERTAINMENT, savedUser);
        Expense e6 = new Expense(null, "Health Checkup & Pharmacy", "Routine doctor consultation and vitamins",
                new BigDecimal("2100.00"), now.minusDays(14), ExpenseCategory.HEALTHCARE, savedUser);

        expenseRepository.saveAll(List.of(e1, e2, e3, e4, e5, e6));
    }
}
