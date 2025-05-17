package be.kdg.expensetracker.service;

import be.kdg.expensetracker.domain.Category;
import be.kdg.expensetracker.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(int id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    public Category addCategory(String name, String description) {
        // Check if category with the same name already exists
        if (categoryRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Category with name " + name + " already exists");
        }

        Category category = new Category(name, description);
        return categoryRepository.save(category);
    }

    public void removeCategory(int id) {
        categoryRepository.deleteById(id);
    }
}