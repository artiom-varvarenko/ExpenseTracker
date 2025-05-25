package be.kdg.expensetracker.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.kdg.expensetracker.domain.Expense;
import be.kdg.expensetracker.domain.User;

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
    
    @Query("SELECT SUM(e.amount) FROM Expense e")
    BigDecimal calculateTotalAmount();
    
    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId")
    long countByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal calculateTotalAmountByUser(@Param("userId") Integer userId);
    
    List<Expense> findByUserId(Integer userId);
}