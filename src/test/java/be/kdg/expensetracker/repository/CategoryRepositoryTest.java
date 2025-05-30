package be.kdg.expensetracker.repository;

import be.kdg.expensetracker.domain.Category;
import be.kdg.expensetracker.testutil.TestDataSeeder;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

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

        // Clear persistence context
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testCategoryNameUniqueness_ShouldThrowExceptionForDuplicateName() {
        // Arrange
        String existingCategoryName = "Food"; // This already exists from test data
        Category duplicateCategory = new Category(existingCategoryName, "Another food category");

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            categoryRepository.save(duplicateCategory);
            categoryRepository.flush();
        }, "Saving category with duplicate name should throw DataIntegrityViolationException");
    }

    @Test
    void testCategoryNameNotNull_ShouldThrowException() {
        // Arrange
        Category categoryWithNullName = new Category(null, "Description without name");

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            categoryRepository.save(categoryWithNullName);
            categoryRepository.flush();
        }, "Saving category with null name should throw DataIntegrityViolationException");
    }

    @Test
    void testFindByName_ShouldReturnCorrectCategory() {
        // Arrange
        String categoryName = "Food";

        // Act
        Optional<Category> foundCategory = categoryRepository.findByName(categoryName);

        // Assert
        assertTrue(foundCategory.isPresent(), "Category should be found by name");
        assertEquals(categoryName, foundCategory.get().getName(),
                "Found category should have the correct name");
    }

    @Test
    void testFindByName_ShouldReturnEmptyForNonExistentCategory() {
        // Arrange
        String nonExistentName = "NonExistentCategory";

        // Act
        Optional<Category> foundCategory = categoryRepository.findByName(nonExistentName);

        // Assert
        assertFalse(foundCategory.isPresent(),
                "Should return empty Optional for non-existent category name");
    }

    @Test
    void testDeleteCategory_WithNoExpenses_ShouldSucceed() {
        // Arrange
        Category newCategory = new Category("Utilities", "Utility bills");
        newCategory = categoryRepository.save(newCategory);
        Integer categoryId = newCategory.getId();

        // Act
        categoryRepository.deleteById(categoryId);
        categoryRepository.flush();

        // Assert
        assertFalse(categoryRepository.existsById(categoryId),
                "Category should be deleted when it has no associated expenses");
    }
}