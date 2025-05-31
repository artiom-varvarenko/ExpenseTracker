package be.kdg.expensetracker.service;

import be.kdg.expensetracker.domain.Category;
import be.kdg.expensetracker.domain.Expense;
import be.kdg.expensetracker.domain.ExpenseCategory;
import be.kdg.expensetracker.domain.User;
import be.kdg.expensetracker.repository.CategoryRepository;
import be.kdg.expensetracker.repository.ExpenseCategoryRepository;
import be.kdg.expensetracker.repository.ExpenseRepository;
import be.kdg.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceUnitTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseCategoryRepository expenseCategoryRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User testUser;
    private Expense testExpense;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = new User("test@test.com", "Test User", "password");
        testUser.setId(1);

        testExpense = new Expense("Test Expense", new BigDecimal("50.00"), LocalDate.now(), testUser);
        testExpense.setId(1);

        testCategory = new Category("Food", "Food expenses");
        testCategory.setId(1);
    }

    // Test addExpenseWithCategories - Happy path
    @Test
    void addExpenseWithCategories_ValidData_ShouldCreateExpenseWithCategories() {
        // Arrange
        List<Integer> categoryIds = Arrays.asList(1, 2);
        Category category2 = new Category("Transport", "Transport expenses");
        category2.setId(2);

        Expense newExpense = new Expense("New Expense", new BigDecimal("100.00"), LocalDate.now(), testUser);
        newExpense.setId(2);

        given(userRepository.findById(1)).willReturn(Optional.of(testUser));
        given(expenseRepository.save(any(Expense.class))).willReturn(newExpense);
        given(categoryRepository.findById(1)).willReturn(Optional.of(testCategory));
        given(categoryRepository.findById(2)).willReturn(Optional.of(category2));
        given(expenseCategoryRepository.save(any(ExpenseCategory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // Act
        Expense result = expenseService.addExpenseWithCategories(
                "New Expense",
                new BigDecimal("100.00"),
                LocalDate.now(),
                1,
                categoryIds
        );

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getId());

        // Verify all interactions
        verify(userRepository, times(1)).findById(1);
        verify(expenseRepository, times(1)).save(any(Expense.class));
        verify(categoryRepository, times(1)).findById(1);
        verify(categoryRepository, times(1)).findById(2);
        verify(expenseCategoryRepository, times(2)).save(any(ExpenseCategory.class));

        // Verify the ExpenseCategory objects were created correctly
        ArgumentCaptor<ExpenseCategory> captor = ArgumentCaptor.forClass(ExpenseCategory.class);
        verify(expenseCategoryRepository, times(2)).save(captor.capture());
        List<ExpenseCategory> capturedCategories = captor.getAllValues();
        assertEquals(2, capturedCategories.size());
    }

    // Test addExpenseWithCategories - User not found
    @Test
    void addExpenseWithCategories_UserNotFound_ShouldThrowException() {
        // Arrange
        given(userRepository.findById(999)).willReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseService.addExpenseWithCategories(
                    "New Expense",
                    new BigDecimal("100.00"),
                    LocalDate.now(),
                    999,
                    Collections.emptyList()
            );
        });

        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository, times(1)).findById(999);
        verify(expenseRepository, never()).save(any());
    }

    // Test addExpenseWithCategories - Category not found
    @Test
    void addExpenseWithCategories_CategoryNotFound_ShouldThrowException() {
        // Arrange
        List<Integer> categoryIds = Arrays.asList(999);
        given(userRepository.findById(1)).willReturn(Optional.of(testUser));
        given(expenseRepository.save(any(Expense.class))).willReturn(testExpense);
        given(categoryRepository.findById(999)).willReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            expenseService.addExpenseWithCategories(
                    "New Expense",
                    new BigDecimal("100.00"),
                    LocalDate.now(),
                    1,
                    categoryIds
            );
        });

        assertTrue(exception.getMessage().contains("Category not found"));
        verify(categoryRepository, times(1)).findById(999);
    }

    // Test updateExpense - Owner can update
    @Test
    void updateExpense_AsOwner_ShouldUpdateSuccessfully() {
        // Arrange
        setupSecurityContext("test@test.com", "ROLE_USER");

        given(expenseRepository.findById(1)).willReturn(Optional.of(testExpense));
        given(expenseRepository.save(any(Expense.class))).willAnswer(invocation -> invocation.getArgument(0));

        // Act
        Expense result = expenseService.updateExpense(
                1,
                "Updated Description",
                new BigDecimal("75.00"),
                LocalDate.now().minusDays(1)
        );

        // Assert
        assertNotNull(result);
        verify(expenseRepository, times(1)).findById(1);
        verify(expenseRepository, times(1)).save(any(Expense.class));

        // Verify the expense was updated with correct values
        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(captor.capture());
        Expense updatedExpense = captor.getValue();
        assertEquals("Updated Description", updatedExpense.getDescription());
        assertEquals(new BigDecimal("75.00"), updatedExpense.getAmount());
    }

    // Test updateExpense - Non-owner cannot update
    @Test
    void updateExpense_AsNonOwner_ShouldThrowAccessDeniedException() {
        // Arrange
        setupSecurityContext("other@test.com", "ROLE_USER");

        given(expenseRepository.findById(1)).willReturn(Optional.of(testExpense));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            expenseService.updateExpense(1, "Updated", null, null);
        });

        verify(expenseRepository, times(1)).findById(1);
        verify(expenseRepository, never()).save(any());
    }

    // Test updateExpense - Admin can update any expense
    @Test
    void updateExpense_AsAdmin_ShouldUpdateSuccessfully() {
        // Arrange
        setupSecurityContext("admin@test.com", "ROLE_ADMIN");

        given(expenseRepository.findById(1)).willReturn(Optional.of(testExpense));
        given(expenseRepository.save(any(Expense.class))).willAnswer(invocation -> invocation.getArgument(0));

        // Act
        Expense result = expenseService.updateExpense(
                1,
                "Admin Updated",
                null,
                null
        );

        // Assert
        assertNotNull(result);
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    // Test deleteExpense - Verify cascade deletion
    @Test
    void deleteExpense_WithCategories_ShouldDeleteExpenseOnly() {
        // Arrange
        setupSecurityContext("test@test.com", "ROLE_USER");

        ExpenseCategory expenseCategory = new ExpenseCategory(testExpense, testCategory);
        testExpense.setExpenseCategories(Arrays.asList(expenseCategory));

        given(expenseRepository.findExpenseWithCategories(1)).willReturn(Optional.of(testExpense));
        doNothing().when(expenseRepository).delete(any(Expense.class));

        // Act
        expenseService.deleteExpense(1);

        // Assert
        verify(expenseRepository, times(1)).findExpenseWithCategories(1);
        verify(expenseRepository, times(1)).delete(testExpense);
        // Verify that category repository was not called for deletion
        verify(categoryRepository, never()).delete(any());
    }

    // Helper method to setup security context
    private void setupSecurityContext(String username, String role) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }
}