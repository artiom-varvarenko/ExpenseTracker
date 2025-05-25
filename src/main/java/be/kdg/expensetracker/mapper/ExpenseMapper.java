package be.kdg.expensetracker.mapper;

import be.kdg.expensetracker.domain.*;
import be.kdg.expensetracker.dto.*;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {})
public interface ExpenseMapper {
    
    @Mapping(target = "userId", source = "user.id")
    ExpenseDto toDto(Expense expense);
    
    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "expenseCategories", ignore = true)
    Expense toEntity(ExpenseDto dto);
    
    // Updates an existing expense with a PATCH dto
    void updateExpenseFromPatch(ExpensePatchDto dto, @MappingTarget Expense expense);
    
    // Convert Expense to ExpenseViewDto with proper user and category data
    @Mapping(target = "user", source = "user")
    @Mapping(target = "categories", expression = "java(mapCategories(expense))")
    ExpenseViewDto toViewDto(Expense expense);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    UserViewDto userToUserViewDto(User user);
    
    // Custom method to map categories from expense
    default List<CategoryViewDto> mapCategories(Expense expense) {
        if (expense.getExpenseCategories() == null) {
            return List.of();
        }
        
        return expense.getExpenseCategories().stream()
            .map(ec -> {
                Category category = ec.getCategory();
                return new CategoryViewDto(
                    category.getId(),
                    category.getName(),
                    category.getDescription()
                );
            })
            .collect(Collectors.toList());
    }
} 