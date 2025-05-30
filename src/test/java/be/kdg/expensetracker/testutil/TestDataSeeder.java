package be.kdg.expensetracker.testutil;

import be.kdg.expensetracker.domain.Category;
import be.kdg.expensetracker.domain.Expense;
import be.kdg.expensetracker.domain.ExpenseCategory;
import be.kdg.expensetracker.domain.User;
import be.kdg.expensetracker.repository.CategoryRepository;
import be.kdg.expensetracker.repository.ExpenseCategoryRepository;
import be.kdg.expensetracker.repository.ExpenseRepository;
import be.kdg.expensetracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class TestDataSeeder {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final PasswordEncoder passwordEncoder;

    // Test data references
    private User testUser1;
    private User testUser2;
    private User adminUser;
    private Category foodCategory;
    private Category transportCategory;
    private Category entertainmentCategory;
    private Expense expense1;
    private Expense expense2;
    private Expense expense3;

    public TestDataSeeder(UserRepository userRepository,
                          ExpenseRepository expenseRepository,
                          CategoryRepository categoryRepository,
                          ExpenseCategoryRepository expenseCategoryRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.expenseCategoryRepository = expenseCategoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void seedTestData() {
        seedUsers();
        seedCategories();
        seedExpenses();
        seedExpenseCategories();
    }

    public void cleanupTestData() {
        expenseCategoryRepository.deleteAll();
        expenseRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void seedUsers() {
        testUser1 = new User("test1@test.com", "Test User 1", passwordEncoder.encode("password123"));
        testUser1.setRole("ROLE_USER");
        testUser1 = userRepository.save(testUser1);

        testUser2 = new User("test2@test.com", "Test User 2", passwordEncoder.encode("password123"));
        testUser2.setRole("ROLE_USER");
        testUser2 = userRepository.save(testUser2);

        adminUser = new User("admin@test.com", "Test Admin", passwordEncoder.encode("admin123"));
        adminUser.setRole("ROLE_ADMIN");
        adminUser = userRepository.save(adminUser);
    }

    private void seedCategories() {
        foodCategory = new Category("Food", "Food and dining expenses");
        foodCategory = categoryRepository.save(foodCategory);

        transportCategory = new Category("Transport", "Transportation expenses");
        transportCategory = categoryRepository.save(transportCategory);

        entertainmentCategory = new Category("Entertainment", "Entertainment and leisure");
        entertainmentCategory = categoryRepository.save(entertainmentCategory);
    }

    private void seedExpenses() {
        expense1 = new Expense("Lunch at restaurant", new BigDecimal("25.50"),
                LocalDate.now().minusDays(1), testUser1);
        expense1 = expenseRepository.save(expense1);

        expense2 = new Expense("Bus ticket", new BigDecimal("2.50"),
                LocalDate.now().minusDays(2), testUser1);
        expense2 = expenseRepository.save(expense2);

        expense3 = new Expense("Movie tickets", new BigDecimal("15.00"),
                LocalDate.now().minusDays(3), testUser2);
        expense3 = expenseRepository.save(expense3);
    }

    private void seedExpenseCategories() {
        ExpenseCategory ec1 = new ExpenseCategory(expense1, foodCategory);
        expenseCategoryRepository.save(ec1);

        ExpenseCategory ec2 = new ExpenseCategory(expense2, transportCategory);
        expenseCategoryRepository.save(ec2);

        ExpenseCategory ec3 = new ExpenseCategory(expense3, entertainmentCategory);
        expenseCategoryRepository.save(ec3);
    }

    // Getter methods for test data
    public User getTestUser1() { return testUser1; }
    public User getTestUser2() { return testUser2; }
    public User getAdminUser() { return adminUser; }
    public Category getFoodCategory() { return foodCategory; }
    public Category getTransportCategory() { return transportCategory; }
    public Category getEntertainmentCategory() { return entertainmentCategory; }
    public Expense getExpense1() { return expense1; }
    public Expense getExpense2() { return expense2; }
    public Expense getExpense3() { return expense3; }
}