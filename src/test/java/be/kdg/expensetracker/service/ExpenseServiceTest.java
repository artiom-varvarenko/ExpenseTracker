package be.kdg.expensetracker.service;

import be.kdg.expensetracker.domain.Category;
import be.kdg.expensetracker.domain.Expense;
import be.kdg.expensetracker.domain.User;
import be.kdg.expensetracker.repository.ExpenseRepository;
import be.kdg.expensetracker.testutil.TestDataSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExpenseServiceTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TestDataSeeder testDataSeeder;

    @BeforeEach
    void setUp() {
        // Clean and seed test data
        testDataSeeder.cleanupTestData();
        testDataSeeder.seedTestData();
    }

    @Test
    void testAddExpenseWithCategories_Success() {
        // Arrange
        User user = testDataSeeder.getTestUser1();
        String description = "New expense with categories";
        BigDecimal amount = new BigDecimal("75.00");
        LocalDate date = LocalDate.now();
        List<Integer> categoryIds = Arrays.asList(
                testDataSeeder.getFoodCategory().getId(),
                testDataSeeder.getEntertainmentCategory().getId()
        );

        // Act
        Expense createdExpense = expenseService.addExpenseWithCategories(
                description, amount, date, user.getId(), categoryIds
        );

        // Assert
        assertNotNull(createdExpense, "Created expense should not be null");
        assertNotNull(createdExpense.getId(), "Created expense should have an ID");
        assertEquals(description, createdExpense.getDescription());
        assertEquals(0, amount.compareTo(createdExpense.getAmount()));
        assertEquals(date, createdExpense.getDate());
        assertEquals(user.getId(), createdExpense.getUser().getId());

        // Verify categories were added
        assertNotNull(createdExpense.getExpenseCategories());
        assertEquals(2, createdExpense.getExpenseCategories().size(),
                "Expense should have 2 categories");
    }

    @Test
    void testAddExpenseWithCategories_UserNotFound_ShouldThrowException() {
        // Arrange
        Integer nonExistentUserId = 999999;
        String description = "Test expense";
        BigDecimal amount = new BigDecimal("50.00");
        LocalDate date = LocalDate.now();
        List<Integer> categoryIds = Arrays.asList(testDataSeeder.getFoodCategory().getId());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseService.addExpenseWithCategories(
                    description, amount, date, nonExistentUserId, categoryIds
            );
        });

        assertTrue(exception.getMessage().contains("User not found"),
                "Exception message should indicate user not found");
    }

    @Test
    void testAddExpenseWithCategories_InvalidCategoryId_ShouldThrowException() {
        // Arrange
        User user = testDataSeeder.getTestUser1();
        String description = "Test expense with invalid category";
        BigDecimal amount = new BigDecimal("30.00");
        LocalDate date = LocalDate.now();
        List<Integer> categoryIds = Arrays.asList(999999); // Non-existent category

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseService.addExpenseWithCategories(
                    description, amount, date, user.getId(), categoryIds
            );
        });

        assertTrue(exception.getMessage().contains("Category not found"),
                "Exception message should indicate category not found");
    }

    @Test
    @WithMockUser(username = "test1@test.com", roles = "USER")
    void testUpdateExpense_ByOwner_ShouldSucceed() {
        // Arrange
        Expense existingExpense = testDataSeeder.getExpense1();
        Integer expenseId = existingExpense.getId();
        String newDescription = "Updated lunch expense";
        BigDecimal newAmount = new BigDecimal("35.75");
        LocalDate newDate = LocalDate.now();

        // Act
        Expense updatedExpense = expenseService.updateExpense(
                expenseId, newDescription, newAmount, newDate
        );

        // Assert
        assertNotNull(updatedExpense);
        assertEquals(expenseId, updatedExpense.getId());
        assertEquals(newDescription, updatedExpense.getDescription());
        assertEquals(0, newAmount.compareTo(updatedExpense.getAmount()));
        assertEquals(newDate, updatedExpense.getDate());
    }

    @Test
    @WithMockUser(username = "test2@test.com", roles = "USER")
    void testUpdateExpense_ByNonOwner_ShouldThrowAccessDeniedException() {
        // Arrange
        Expense expenseOwnedByUser1 = testDataSeeder.getExpense1();
        Integer expenseId = expenseOwnedByUser1.getId();
        String newDescription = "Trying to update someone else's expense";

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            expenseService.updateExpense(expenseId, newDescription, null, null);
        }, "Non-owner should not be able to update expense");
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void testUpdateExpense_ByAdmin_ShouldSucceed() {
        // Arrange
        Expense userExpense = testDataSeeder.getExpense1();
        Integer expenseId = userExpense.getId();
        String newDescription = "Admin updated expense";

        // Act
        Expense updatedExpense = expenseService.updateExpense(
                expenseId, newDescription, null, null
        );

        // Assert
        assertNotNull(updatedExpense);
        assertEquals(newDescription, updatedExpense.getDescription());
    }

    @Test
    void testUpdateExpense_NonExistentExpense_ShouldThrowException() {
        // Arrange
        Integer nonExistentId = 999999;

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseService.updateExpense(nonExistentId, "New description", null, null);
        });

        assertTrue(exception.getMessage().contains("Expense not found"),
                "Exception message should indicate expense not found");
    }

    @Test
    @WithMockUser(username = "test1@test.com", roles = "USER")
    void testDeleteExpense_ByOwner_ShouldSucceed() {
        // Arrange - Create a new expense without categories for this test
        User user = testDataSeeder.getTestUser1();
        Expense expense = new Expense("Expense to delete", new BigDecimal("50.00"), LocalDate.now(), user);
        expense = expenseRepository.save(expense);
        expenseRepository.flush();

        Integer expenseId = expense.getId();

        // Act
        expenseService.deleteExpense(expenseId);

        // Assert
        assertFalse(expenseRepository.existsById(expenseId),
                "Expense should be deleted");
    }

    @Test
    @WithMockUser(username = "test2@test.com", roles = "USER")
    void testDeleteExpense_ByNonOwner_ShouldThrowAccessDeniedException() {
        // Arrange
        Expense expenseOwnedByUser1 = testDataSeeder.getExpense1();
        Integer expenseId = expenseOwnedByUser1.getId();

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            expenseService.deleteExpense(expenseId);
        }, "Non-owner should not be able to delete expense");
    }

    @Test
    void testDeleteExpense_WithoutAuthentication_ShouldThrowAccessDeniedException() {
        // Arrange
        Expense expense = testDataSeeder.getExpense1();
        Integer expenseId = expense.getId();

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            expenseService.deleteExpense(expenseId);
        }, "Unauthenticated user should not be able to delete expense");
    }
}