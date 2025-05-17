package be.kdg.expensetracker.repository;

import be.kdg.expensetracker.domain.Expense;
import be.kdg.expensetracker.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    List<Expense> findByUser(User user);

    @Query("""
        FROM Expense e
        LEFT JOIN FETCH e.expenseCategories ec
        LEFT JOIN FETCH ec.category
        WHERE e.id = :id
        """)
    Optional<Expense> findExpenseWithCategories(@Param("id") int id);
}