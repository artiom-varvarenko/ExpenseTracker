package be.kdg.expensetracker.service;

import be.kdg.expensetracker.domain.Category;
import be.kdg.expensetracker.repository.CategoryRepository;
import be.kdg.expensetracker.testutil.TestDataSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestDataSeeder testDataSeeder;

    @BeforeEach
    void setUp() {
        testDataSeeder.cleanupTestData();
        testDataSeeder.seedTestData();
    }

    @Test
    void testAddCategory_WithValidData_ShouldSucceed() {
        // Arrange
        String name = "Healthcare";
        String description = "Medical and health expenses";

        // Act
        Category createdCategory = categoryService.addCategory(name, description);

        // Assert
        assertNotNull(createdCategory, "Created category should not be null");
        assertNotNull(createdCategory.getId(), "Created category should have an ID");
        assertEquals(name, createdCategory.getName());
        assertEquals(description, createdCategory.getDescription());

        // Verify it was persisted
        assertTrue(categoryRepository.existsById(createdCategory.getId()));
    }

    @Test
    void testAddCategory_WithDuplicateName_ShouldThrowException() {
        // Arrange
        String existingName = "Food"; // Already exists in test data
        String description = "Another food category";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.addCategory(existingName, description);
        });

        assertTrue(exception.getMessage().contains("already exists"),
                "Exception message should indicate duplicate category");
    }

    @Test
    void testAddCategory_WithEmptyName_ShouldSucceedButMayNeedValidation() {
        // Arrange
        String emptyName = "";
        String description = "Category with empty name";

        // Act
        Category createdCategory = categoryService.addCategory(emptyName, description);

        // Assert
        assertNotNull(createdCategory);
        assertEquals(emptyName, createdCategory.getName());
        // Note: In a real application, you might want to add validation to prevent empty names
    }

    @Test
    void testGetCategoryById_ExistingCategory_ShouldReturnCategory() {
        // Arrange
        Category existingCategory = testDataSeeder.getFoodCategory();
        Integer categoryId = existingCategory.getId();

        // Act
        Category foundCategory = categoryService.getCategoryById(categoryId);

        // Assert
        assertNotNull(foundCategory);
        assertEquals(categoryId, foundCategory.getId());
        assertEquals("Food", foundCategory.getName());
    }

    @Test
    void testGetCategoryById_NonExistentCategory_ShouldThrowException() {
        // Arrange
        Integer nonExistentId = 999999;

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.getCategoryById(nonExistentId);
        });

        assertTrue(exception.getMessage().contains("Category not found"),
                "Exception message should indicate category not found");
    }

    @Test
    void testGetAllCategories_ShouldReturnAllCategories() {
        // Act
        List<Category> allCategories = categoryService.getAllCategories();

        // Assert
        assertNotNull(allCategories);
        assertEquals(3, allCategories.size(), "Should return all 3 seeded categories");

        // Verify category names
        assertTrue(allCategories.stream().anyMatch(c -> "Food".equals(c.getName())));
        assertTrue(allCategories.stream().anyMatch(c -> "Transport".equals(c.getName())));
        assertTrue(allCategories.stream().anyMatch(c -> "Entertainment".equals(c.getName())));
    }

    @Test
    void testRemoveCategory_ExistingCategory_ShouldSucceed() {
        // Arrange
        Category newCategory = categoryService.addCategory("ToBeDeleted", "Category to delete");
        Integer categoryId = newCategory.getId();

        // Verify it exists
        assertTrue(categoryRepository.existsById(categoryId));

        // Act
        categoryService.removeCategory(categoryId);

        // Assert
        assertFalse(categoryRepository.existsById(categoryId),
                "Category should be deleted");
    }
}