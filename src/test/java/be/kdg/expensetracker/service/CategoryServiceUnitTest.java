package be.kdg.expensetracker.service;

import be.kdg.expensetracker.domain.Category;
import be.kdg.expensetracker.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUnitTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category("Food", "Food expenses");
        testCategory.setId(1);
    }

    // Test addCategory - Happy path
    @Test
    void addCategory_ValidData_ShouldCreateCategory() {
        // Arrange
        String name = "Entertainment";
        String description = "Entertainment expenses";
        Category newCategory = new Category(name, description);
        newCategory.setId(2);

        given(categoryRepository.findByName(name)).willReturn(Optional.empty());
        given(categoryRepository.save(any(Category.class))).willReturn(newCategory);

        // Act
        Category result = categoryService.addCategory(name, description);

        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());

        // Verify interactions
        verify(categoryRepository, times(1)).findByName(name);
        verify(categoryRepository, times(1)).save(any(Category.class));

        // Verify the correct category was saved
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        Category savedCategory = captor.getValue();
        assertEquals(name, savedCategory.getName());
        assertEquals(description, savedCategory.getDescription());
    }

    // Test addCategory - Duplicate name
    @Test
    void addCategory_DuplicateName_ShouldThrowException() {
        // Arrange
        String existingName = "Food";
        given(categoryRepository.findByName(existingName)).willReturn(Optional.of(testCategory));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.addCategory(existingName, "Another food category");
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(categoryRepository, times(1)).findByName(existingName);
        verify(categoryRepository, never()).save(any());
    }

    // Test getAllCategories
    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        // Arrange
        Category category2 = new Category("Transport", "Transport expenses");
        category2.setId(2);
        List<Category> categories = Arrays.asList(testCategory, category2);

        given(categoryRepository.findAll()).willReturn(categories);

        // Act
        List<Category> result = categoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(categoryRepository, times(1)).findAll();
    }

    // Test removeCategory with verify
    @Test
    void removeCategory_ExistingCategory_ShouldCallDeleteById() {
        // Arrange
        Integer categoryId = 1;
        doNothing().when(categoryRepository).deleteById(categoryId);

        // Act
        categoryService.removeCategory(categoryId);

        // Assert - Verify the exact method was called with correct argument
        verify(categoryRepository, times(1)).deleteById(categoryId);
        verifyNoMoreInteractions(categoryRepository);
    }
}