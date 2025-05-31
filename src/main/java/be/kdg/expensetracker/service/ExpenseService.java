package be.kdg.expensetracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.kdg.expensetracker.domain.Category;
import be.kdg.expensetracker.domain.Expense;
import be.kdg.expensetracker.domain.ExpenseCategory;
import be.kdg.expensetracker.domain.User;
import be.kdg.expensetracker.repository.CategoryRepository;
import be.kdg.expensetracker.repository.ExpenseCategoryRepository;
import be.kdg.expensetracker.repository.ExpenseRepository;
import be.kdg.expensetracker.repository.UserRepository;

@Service
@Transactional
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ExpenseService(ExpenseRepository expenseRepository,
                          UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          ExpenseCategoryRepository expenseCategoryRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.expenseCategoryRepository = expenseCategoryRepository;
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public Expense getExpenseById(int id) {
        return expenseRepository.findExpenseWithCategories(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
    }

    public List<Expense> getExpensesByUser(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return expenseRepository.findByUser(user);
    }

    public List<Expense> getExpensesByUserId(int userId) {
        // Return empty list if user doesn't exist instead of throwing exception
        if (!userRepository.existsById(userId)) {
            return new ArrayList<>();
        }
        return expenseRepository.findByUserId(userId);
    }

    public List<Expense> getExpensesByUser(User user) {
        return expenseRepository.findByUser(user);
    }

    // New search method
    public List<Expense> searchExpenses(String description, String minAmount, String maxAmount) {
        List<Expense> allExpenses = expenseRepository.findAll();

        return allExpenses.stream()
                .filter(expense -> {
                    // Filter by description if provided
                    if (description != null && !description.trim().isEmpty()) {
                        if (!expense.getDescription().toLowerCase().contains(description.toLowerCase())) {
                            return false;
                        }
                    }

                    // Filter by minimum amount if provided
                    if (minAmount != null && !minAmount.trim().isEmpty()) {
                        try {
                            BigDecimal min = new BigDecimal(minAmount);
                            if (expense.getAmount().compareTo(min) < 0) {
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid number format
                        }
                    }

                    // Filter by maximum amount if provided
                    if (maxAmount != null && !maxAmount.trim().isEmpty()) {
                        try {
                            BigDecimal max = new BigDecimal(maxAmount);
                            if (expense.getAmount().compareTo(max) > 0) {
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid number format
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    public Expense addExpense(String description, BigDecimal amount, LocalDate date, int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Expense expense = new Expense(description, amount, date, user);
        return expenseRepository.save(expense);
    }

    public Expense createExpense(String description, BigDecimal amount, LocalDate date, User user) {
        // Only authenticated users can create expenses - except for the public endpoint
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || (!auth.isAuthenticated() && !user.getEmail().equals("client-test@expenses.com"))) {
            throw new AccessDeniedException("You must be logged in to create an expense");
        }

        Expense expense = new Expense(description, amount, date, user);
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense addExpenseWithCategories(String description, BigDecimal amount, LocalDate date, int userId, List<Integer> categoryIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Expense expense = new Expense(description, amount, date, user);
        expense = expenseRepository.save(expense);

        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<ExpenseCategory> expenseCategories = new ArrayList<>();

            for (Integer categoryId : categoryIds) {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

                ExpenseCategory expenseCategory = new ExpenseCategory(expense, category);
                expenseCategory = expenseCategoryRepository.save(expenseCategory);
                expenseCategories.add(expenseCategory);
            }

            expense.setExpenseCategories(expenseCategories);
        }

        return expense;
    }

    public void addCategoryToExpense(int expenseId, int categoryId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + expenseId));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        // Check if the association already exists
        if (expenseCategoryRepository.findByExpenseAndCategory(expense, category).isEmpty()) {
            ExpenseCategory expenseCategory = new ExpenseCategory(expense, category);
            expenseCategoryRepository.save(expenseCategory);
        }
    }

    @Transactional
    public Expense updateExpense(int id, String description, BigDecimal amount, LocalDate date) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        // Check if the current user has permission to update this expense
        if (!canUserModifyExpense(expense)) {
            throw new AccessDeniedException("You don't have permission to update this expense");
        }

        if (description != null) {
            expense.setDescription(description);
        }

        if (amount != null) {
            expense.setAmount(amount);
        }

        if (date != null) {
            expense.setDate(date);
        }

        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense updateExpense(int id, String description, BigDecimal amount, LocalDate date, List<Integer> categoryIds) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        // Check if the current user has permission to update this expense
        if (!canUserModifyExpense(expense)) {
            throw new AccessDeniedException("You don't have permission to update this expense");
        }

        if (description != null) {
            expense.setDescription(description);
        }

        if (amount != null) {
            expense.setAmount(amount);
        }

        if (date != null) {
            expense.setDate(date);
        }

        expense = expenseRepository.save(expense);

        // Update categories if provided
        if (categoryIds != null) {
            // Remove existing categories
            if (expense.getExpenseCategories() != null) {
                List<ExpenseCategory> toRemove = new ArrayList<>(expense.getExpenseCategories());
                for (ExpenseCategory ec : toRemove) {
                    expenseCategoryRepository.delete(ec);
                }
            }

            // Add new categories
            for (Integer categoryId : categoryIds) {
                addCategoryToExpense(expense.getId(), categoryId);
            }

            // Refresh the expense to include the updated categories
            expense = expenseRepository.findExpenseWithCategories(expense.getId()).orElse(expense);
        }

        return expense;
    }

    public void removeExpense(int id) {
        expenseRepository.deleteById(id);
    }

    @Transactional
    public void deleteExpense(int id) {
        // Use the custom query that loads expense with categories
        Expense expense = expenseRepository.findExpenseWithCategories(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        // Check if the current user has permission to delete this expense
        if (!canUserModifyExpense(expense)) {
            throw new AccessDeniedException("You don't have permission to delete this expense");
        }

        // Delete the expense - cascade should handle ExpenseCategory deletion
        expenseRepository.delete(expense);
    }

    /**
     * Checks if the current user can modify the given expense.
     * Rules:
     * 1. The user is the owner of the expense
     * 2. OR the user has ADMIN role
     *
     * @param expense The expense to check
     * @return true if the user can modify the expense, false otherwise
     */
    private boolean canUserModifyExpense(Expense expense) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // If not authenticated, no access
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // Admin can modify any expense
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return true;
        }

        // User can modify their own expenses
        if (auth.getName().equals(expense.getUser().getEmail())) {
            return true;
        }

        return false;
    }
}