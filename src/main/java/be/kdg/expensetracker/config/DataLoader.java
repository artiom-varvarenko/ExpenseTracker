package be.kdg.expensetracker.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import be.kdg.expensetracker.domain.Expense;
import be.kdg.expensetracker.domain.User;
import be.kdg.expensetracker.repository.ExpenseRepository;
import be.kdg.expensetracker.repository.UserRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, ExpenseRepository expenseRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Only seed if no users exist
        if (userRepository.count() == 0) {
            // Create admin user
            User admin = new User("admin@expenses.com", "Administrator", passwordEncoder.encode("admin123"));
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
            
            // Create manager user
            User manager = new User("manager@expenses.com", "Manager User", passwordEncoder.encode("manager123"));
            manager.setRole("ROLE_MANAGER");
            userRepository.save(manager);
            
            // Create regular users
            User john = new User("john@expenses.com", "John Doe", passwordEncoder.encode("john123"));
            john.setRole("ROLE_USER");
            userRepository.save(john);
            
            User jane = new User("jane@expenses.com", "Jane Smith", passwordEncoder.encode("jane123"));
            jane.setRole("ROLE_USER");
            userRepository.save(jane);
            
            // Create expenses for users
            createExpensesForUser(admin, List.of(
                new Expense("Admin Conference", new BigDecimal("499.99"), LocalDate.now().minusDays(14), admin),
                new Expense("Admin Office Supplies", new BigDecimal("89.50"), LocalDate.now().minusDays(7), admin)
            ));
            
            createExpensesForUser(manager, List.of(
                new Expense("Team Lunch", new BigDecimal("132.75"), LocalDate.now().minusDays(3), manager),
                new Expense("Office Equipment", new BigDecimal("299.99"), LocalDate.now().minusDays(10), manager),
                new Expense("Travel Expenses", new BigDecimal("215.50"), LocalDate.now().minusDays(5), manager)
            ));
            
            createExpensesForUser(john, List.of(
                new Expense("Groceries", new BigDecimal("67.89"), LocalDate.now().minusDays(2), john),
                new Expense("Gas", new BigDecimal("45.25"), LocalDate.now().minusDays(8), john),
                new Expense("Internet Bill", new BigDecimal("79.99"), LocalDate.now().minusDays(15), john)
            ));
            
            createExpensesForUser(jane, List.of(
                new Expense("Restaurant", new BigDecimal("83.47"), LocalDate.now().minusDays(1), jane),
                new Expense("Movie Tickets", new BigDecimal("24.99"), LocalDate.now().minusDays(4), jane)
            ));
            
            System.out.println("Sample users and expenses created.");
        }
    }
    
    private void createExpensesForUser(User user, List<Expense> expenses) {
        for (Expense expense : expenses) {
            expenseRepository.save(expense);
        }
    }
} 