package be.kdg.expensetracker.mapper;

import be.kdg.expensetracker.domain.*;
import be.kdg.expensetracker.dto.*;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {})
public interface ExpenseMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "categoryIds", expression = "java(mapCategoryIds(expense))")
    ExpenseDto toDto(Expense expense);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "expenseCategories", ignore = true)
    Expense toEntity(ExpenseDto dto);

    // Updates an existing expense with a PATCH dto
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "expenseCategories", ignore = true)
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

    // Custom method to map category IDs
    default List<Integer> mapCategoryIds(Expense expense) {
        if (expense.getExpenseCategories() == null) {
            return List.of();
        }

        try {
            return expense.getExpenseCategories().stream()
                    .map(ec -> ec.getCategory().getId())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // If lazy loading fails, return empty list
            return List.of();
        }
    }
}