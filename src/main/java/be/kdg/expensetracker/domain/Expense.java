package be.kdg.expensetracker.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    // Using LAZY for JPA best practices
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Cascade ALL and orphanRemoval to properly handle ExpenseCategory deletion
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ExpenseCategory> expenseCategories = new ArrayList<>();

    // Constructors
    public Expense() {
    }

    public Expense(String description, BigDecimal amount, LocalDate date, User user) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.user = user;
    }

    // Helper methods for managing bidirectional relationship
    public void addExpenseCategory(ExpenseCategory expenseCategory) {
        expenseCategories.add(expenseCategory);
        expenseCategory.setExpense(this);
    }

    public void removeExpenseCategory(ExpenseCategory expenseCategory) {
        expenseCategories.remove(expenseCategory);
        expenseCategory.setExpense(null);
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<ExpenseCategory> getExpenseCategories() {
        return expenseCategories;
    }

    public void setExpenseCategories(List<ExpenseCategory> expenseCategories) {
        this.expenseCategories = expenseCategories;
    }
}