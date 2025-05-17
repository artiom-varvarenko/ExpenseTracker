package be.kdg.expensetracker.service;

import be.kdg.expensetracker.domain.Category;
import be.kdg.expensetracker.domain.Expense;
import be.kdg.expensetracker.domain.ExpenseCategory;
import be.kdg.expensetracker.domain.User;
import be.kdg.expensetracker.repository.CategoryRepository;
import be.kdg.expensetracker.repository.ExpenseCategoryRepository;
import be.kdg.expensetracker.repository.ExpenseRepository;
import be.kdg.expensetracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;

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

    public Expense addExpense(String description, BigDecimal amount, LocalDate date, int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Expense expense = new Expense(description, amount, date, user);
        return expenseRepository.save(expense);
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

    public void removeExpense(int id) {
        expenseRepository.deleteById(id);
    }
}