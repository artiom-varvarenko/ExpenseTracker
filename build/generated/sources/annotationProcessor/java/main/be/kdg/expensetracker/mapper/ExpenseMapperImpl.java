package be.kdg.expensetracker.mapper;

import be.kdg.expensetracker.domain.Expense;
import be.kdg.expensetracker.domain.User;
import be.kdg.expensetracker.dto.ExpenseDto;
import be.kdg.expensetracker.dto.ExpensePatchDto;
import be.kdg.expensetracker.dto.ExpenseViewDto;
import be.kdg.expensetracker.dto.UserViewDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-01T13:22:00+0200",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.10.jar, environment: Java 21.0.4 (Amazon.com Inc.)"
)
@Component
public class ExpenseMapperImpl implements ExpenseMapper {

    @Override
    public ExpenseDto toDto(Expense expense) {
        if ( expense == null ) {
            return null;
        }

        ExpenseDto expenseDto = new ExpenseDto();

        expenseDto.setUserId( expenseUserId( expense ) );
        expenseDto.setId( expense.getId() );
        expenseDto.setDescription( expense.getDescription() );
        expenseDto.setAmount( expense.getAmount() );
        expenseDto.setDate( expense.getDate() );

        expenseDto.setCategoryIds( mapCategoryIds(expense) );

        return expenseDto;
    }

    @Override
    public Expense toEntity(ExpenseDto dto) {
        if ( dto == null ) {
            return null;
        }

        Expense expense = new Expense();

        expense.setUser( expenseDtoToUser( dto ) );
        expense.setId( dto.getId() );
        expense.setDescription( dto.getDescription() );
        expense.setAmount( dto.getAmount() );
        expense.setDate( dto.getDate() );

        return expense;
    }

    @Override
    public void updateExpenseFromPatch(ExpensePatchDto dto, Expense expense) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getDescription() != null ) {
            expense.setDescription( dto.getDescription() );
        }
        if ( dto.getAmount() != null ) {
            expense.setAmount( dto.getAmount() );
        }
        if ( dto.getDate() != null ) {
            expense.setDate( dto.getDate() );
        }
    }

    @Override
    public ExpenseViewDto toViewDto(Expense expense) {
        if ( expense == null ) {
            return null;
        }

        ExpenseViewDto expenseViewDto = new ExpenseViewDto();

        expenseViewDto.setUser( userToUserViewDto( expense.getUser() ) );
        expenseViewDto.setId( expense.getId() );
        expenseViewDto.setDescription( expense.getDescription() );
        expenseViewDto.setAmount( expense.getAmount() );
        expenseViewDto.setDate( expense.getDate() );

        expenseViewDto.setCategories( mapCategories(expense) );

        return expenseViewDto;
    }

    @Override
    public UserViewDto userToUserViewDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserViewDto userViewDto = new UserViewDto();

        userViewDto.setId( user.getId() );
        userViewDto.setName( user.getName() );
        userViewDto.setEmail( user.getEmail() );

        return userViewDto;
    }

    private Integer expenseUserId(Expense expense) {
        if ( expense == null ) {
            return null;
        }
        User user = expense.getUser();
        if ( user == null ) {
            return null;
        }
        Integer id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected User expenseDtoToUser(ExpenseDto expenseDto) {
        if ( expenseDto == null ) {
            return null;
        }

        User user = new User();

        user.setId( expenseDto.getUserId() );

        return user;
    }
}
