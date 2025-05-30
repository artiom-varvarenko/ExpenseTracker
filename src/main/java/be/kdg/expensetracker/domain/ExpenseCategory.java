package be.kdg.expensetracker.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "expense_categories",
        uniqueConstraints = {@UniqueConstraint(
                name = "uq_expense_category",
                columnNames = {"expense_id", "category_id"}
        )}
)
public class ExpenseCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Using LAZY for JPA best practices
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Constructors
    public ExpenseCategory() {
    }

    public ExpenseCategory(Expense expense, Category category) {
        this.expense = expense;
        this.category = category;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}