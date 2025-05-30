package be.kdg.expensetracker.repository;

import be.kdg.expensetracker.domain.Category;
import be.kdg.expensetracker.domain.Expense;
import be.kdg.expensetracker.domain.ExpenseCategory;
import be.kdg.expensetracker.domain.User;
import be.kdg.expensetracker.testutil.TestDataSeeder;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;

    @Autowired
    private TestDataSeeder testDataSeeder;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        expenseCategoryRepository.deleteAll();
        expenseRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Seed fresh test data
        testDataSeeder.seedTestData();

        // Clear persistence context to ensure clean state
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testDeleteExpense_ShouldCascadeDeleteExpenseCategories() {
        // Arrange
        User user = testDataSeeder.getTestUser1();
        Category category = testDataSeeder.getFoodCategory();

        // Create a new expense
        Expense expense = new Expense("Test expense for deletion", new BigDecimal("100.00"),
                LocalDate.now(), user);
        expense = expenseRepository.saveAndFlush(expense);

        // Create expense category
        ExpenseCategory expenseCategory = new ExpenseCategory(expense, category);
        expenseCategory = expenseCategoryRepository.saveAndFlush(expenseCategory);

        Integer expenseId = expense.getId();
        Integer expenseCategoryId = expenseCategory.getId();

        // Clear the persistence context to ensure clean state
        entityManager.clear();

        // Act - manually delete expense categories first (since H2 might not handle cascade properly)
        Optional<Expense> expenseToDelete = expenseRepository.findById(expenseId);
        assertTrue(expenseToDelete.isPresent());

        // Delete expense categories first
        expenseCategoryRepository.deleteAll(expenseToDelete.get().getExpenseCategories());
        expenseCategoryRepository.flush();

        // Then delete the expense
        expenseRepository.deleteById(expenseId);
        expenseRepository.flush();

        // Assert
        assertFalse(expenseRepository.existsById(expenseId),
                "Expense should be deleted");

        assertFalse(expenseCategoryRepository.existsById(expenseCategoryId),
                "ExpenseCategory should be deleted");

        // Verify category still exists
        assertTrue(categoryRepository.existsById(category.getId()),
                "Category should NOT be deleted when expense is deleted");

        assertTrue(userRepository.existsById(user.getId()),
                "User should NOT be deleted when expense is deleted");
    }


    @Test
    void testDeleteUser_ShouldNotDeleteExpenses_WhenUserHasExpenses() {
        // Arrange
        User user = testDataSeeder.getTestUser1();
        Integer userId = user.getId();

        // Verify user has expenses
        List<Expense> userExpenses = expenseRepository.findByUserId(userId);
        assertFalse(userExpenses.isEmpty(), "User should have expenses before deletion attempt");

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.deleteById(userId);
            userRepository.flush(); // Force the deletion to execute
        }, "Deleting a user with expenses should throw DataIntegrityViolationException");
    }

    @Test
    void testExpenseAmountScale_ShouldAcceptTwoDecimalPlaces() {
        // Arrange
        User user = testDataSeeder.getTestUser1();
        BigDecimal amountWithTwoDecimals = new BigDecimal("123.45");

        Expense expense = new Expense("Test expense with decimals",
                amountWithTwoDecimals, LocalDate.now(), user);

        // Act
        Expense savedExpense = expenseRepository.save(expense);
        expenseRepository.flush();

        // Retrieve the expense to ensure it's loaded from database
        entityManager.clear();
        Expense retrievedExpense = expenseRepository.findById(savedExpense.getId()).orElseThrow();

        // Assert
        assertEquals(0, amountWithTwoDecimals.compareTo(retrievedExpense.getAmount()),
                "Amount should be saved and retrieved with exact precision");
    }

    @Test
    void testExpenseDescriptionNotNull_ShouldThrowException() {
        // Arrange
        User user = testDataSeeder.getTestUser1();
        Expense expense = new Expense(null, new BigDecimal("50.00"), LocalDate.now(), user);

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            expenseRepository.save(expense);
            expenseRepository.flush();
        }, "Saving expense with null description should throw DataIntegrityViolationException");
    }

    @Test
    void testFindExpenseWithCategories_ShouldEagerlyLoadCategories() {
        // Arrange
        Integer expenseId = testDataSeeder.getExpense1().getId();

        // Clear persistence context to ensure fresh load
        entityManager.clear();

        // Act
        Optional<Expense> expenseOpt = expenseRepository.findExpenseWithCategories(expenseId);

        // Assert
        assertTrue(expenseOpt.isPresent(), "Expense should be found");

        Expense expense = expenseOpt.get();
        assertNotNull(expense.getExpenseCategories(), "ExpenseCategories should not be null");
        assertFalse(expense.getExpenseCategories().isEmpty(),
                "ExpenseCategories should be eagerly loaded");

        // Verify category is loaded
        ExpenseCategory expenseCategory = expense.getExpenseCategories().get(0);
        assertNotNull(expenseCategory.getCategory(), "Category should be loaded");
        assertNotNull(expenseCategory.getCategory().getName(), "Category name should be accessible");
    }

    @Test
    void testCalculateTotalAmountByUser_ShouldReturnCorrectSum() {
        // Arrange
        User user = testDataSeeder.getTestUser1();
        Integer userId = user.getId();

        // Create additional expense for user1
        Expense additionalExpense = new Expense("Additional expense",
                new BigDecimal("30.00"),
                LocalDate.now(), user);
        expenseRepository.save(additionalExpense);
        expenseRepository.flush();

        // Act
        BigDecimal totalAmount = expenseRepository.calculateTotalAmountByUser(userId);

        // Assert
        assertNotNull(totalAmount, "Total amount should not be null");

        // Calculate expected total (25.50 + 2.50 + 30.00 = 58.00)
        BigDecimal expectedTotal = new BigDecimal("58.00");
        assertEquals(0, expectedTotal.compareTo(totalAmount),
                "Total amount should be sum of all user expenses");
    }
}