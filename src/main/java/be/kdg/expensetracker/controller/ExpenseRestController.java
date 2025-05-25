package be.kdg.expensetracker.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.kdg.expensetracker.domain.Expense;
import be.kdg.expensetracker.domain.User;
import be.kdg.expensetracker.dto.ExpenseDto;
import be.kdg.expensetracker.dto.ExpensePatchDto;
import be.kdg.expensetracker.dto.ExpensePostDto;
import be.kdg.expensetracker.dto.ExpenseViewDto;
import be.kdg.expensetracker.mapper.ExpenseMapper;
import be.kdg.expensetracker.repository.ExpenseRepository;
import be.kdg.expensetracker.repository.UserRepository;
import be.kdg.expensetracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseRestController {
    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;
    private final UserRepository userRepository;
    private final ExpenseMapper expenseMapper;

    public ExpenseRestController(ExpenseRepository expenseRepository, ExpenseService expenseService, 
                                UserRepository userRepository, ExpenseMapper expenseMapper) {
        this.expenseRepository = expenseRepository;
        this.expenseService = expenseService;
        this.userRepository = userRepository;
        this.expenseMapper = expenseMapper;
    }

    @GetMapping
    public List<ExpenseDto> getAllExpenses() {
        return expenseService.getAllExpenses().stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpenseById(@PathVariable int id) {
        try {
            Expense expense = expenseService.getExpenseById(id);
            return ResponseEntity.ok(expenseMapper.toDto(expense));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExpenseDto>> getExpensesByUser(@PathVariable int userId) {
        try {
            List<Expense> expenses = expenseService.getExpensesByUserId(userId);
            List<ExpenseDto> dtos = expenses.stream()
                    .map(expenseMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExpenseDto> createExpense(@Valid @RequestBody ExpensePostDto expensePostDto) {
        // Get the currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        
        // Find user by email
        return userRepository.findByEmail(currentUserEmail)
                .map(user -> {
                    // Create the expense with the authenticated user
                    Expense expense = expenseService.createExpense(
                            expensePostDto.getDescription(),
                            expensePostDto.getAmount(),
                            expensePostDto.getDate(),
                            user
                    );
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(expenseMapper.toDto(expense));
                })
                .orElse(ResponseEntity.badRequest().build());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExpenseDto> updateExpense(@PathVariable int id, @Valid @RequestBody ExpensePatchDto expensePatchDto) {
        try {
            Expense expense = expenseService.updateExpense(
                    id,
                    expensePatchDto.getDescription(),
                    expensePatchDto.getAmount(),
                    expensePatchDto.getDate()
            );
            return ResponseEntity.ok(expenseMapper.toDto(expense));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteExpense(@PathVariable int id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
} 