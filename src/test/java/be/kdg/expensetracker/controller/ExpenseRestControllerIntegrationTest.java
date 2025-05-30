package be.kdg.expensetracker.controller;

import be.kdg.expensetracker.domain.Expense;
import be.kdg.expensetracker.domain.User;
import be.kdg.expensetracker.dto.ExpenseDto;
import be.kdg.expensetracker.dto.ExpensePatchDto;
import be.kdg.expensetracker.dto.ExpensePostDto;
import be.kdg.expensetracker.repository.ExpenseRepository;
import be.kdg.expensetracker.repository.UserRepository;
import be.kdg.expensetracker.testutil.TestDataSeeder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExpenseRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataSeeder testDataSeeder;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        testDataSeeder.cleanupTestData();
        testDataSeeder.seedTestData();
    }

    @Test
    void getAllExpenses_ShouldReturnListOfExpenses() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/expenses")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3))) // 3 expenses from test data
                .andExpect(jsonPath("$[0].description").isNotEmpty())
                .andExpect(jsonPath("$[0].amount").isNotEmpty())
                .andExpect(jsonPath("$[0].date").isNotEmpty())
                .andExpect(jsonPath("$[0].userId").isNumber());
    }

    @Test
    void getExpenseById_ExistingExpense_ShouldReturnExpense() throws Exception {
        // Arrange
        Expense expense = testDataSeeder.getExpense1();

        // Act & Assert
        mockMvc.perform(get("/api/expenses/{id}", expense.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expense.getId()))
                .andExpect(jsonPath("$.description").value(expense.getDescription()))
                .andExpect(jsonPath("$.amount").value(expense.getAmount().doubleValue()))
                .andExpect(jsonPath("$.userId").value(expense.getUser().getId()));
    }

    @Test
    void getExpenseById_NonExistentExpense_ShouldReturn404() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/expenses/{id}", 999999)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getExpensesByUser_ExistingUser_ShouldReturnUserExpenses() throws Exception {
        // Arrange
        User user = testDataSeeder.getTestUser1();

        // Act & Assert
        mockMvc.perform(get("/api/expenses/user/{userId}", user.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2))) // User 1 has 2 expenses
                .andExpect(jsonPath("$[*].userId", everyItem(is(user.getId()))));
    }

    @Test
    void getExpensesByUser_NonExistentUser_ShouldReturnEmptyList() throws Exception {
        // Act & Assert - Changed expectation from 404 to 200 with empty list
        mockMvc.perform(get("/api/expenses/user/{userId}", 999999)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "newuser@test.com", roles = "USER")
    void createExpense_ValidData_ShouldCreateExpense() throws Exception {
        // Arrange
        User user = new User("newuser@test.com", "New User", "password");
        user.setRole("ROLE_USER");
        userRepository.save(user);

        ExpensePostDto expenseDto = new ExpensePostDto();
        expenseDto.setDescription("New test expense");
        expenseDto.setAmount(new BigDecimal("99.99"));
        expenseDto.setDate(LocalDate.now());
        expenseDto.setCategoryIds(Arrays.asList(testDataSeeder.getFoodCategory().getId()));

        // Act & Assert
        mockMvc.perform(post("/api/expenses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("New test expense"))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void createExpense_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Arrange
        ExpensePostDto expenseDto = new ExpensePostDto();
        expenseDto.setDescription("Unauthorized expense");
        expenseDto.setAmount(new BigDecimal("50.00"));
        expenseDto.setDate(LocalDate.now());

        // Act & Assert - Changed from 403 to 401 for unauthenticated access
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void createExpense_InvalidData_ShouldReturn400() throws Exception {
        // Arrange
        ExpensePostDto expenseDto = new ExpensePostDto();
        expenseDto.setDescription("A"); // Too short
        expenseDto.setAmount(new BigDecimal("-10.00")); // Negative
        expenseDto.setDate(LocalDate.now());

        // Act & Assert
        mockMvc.perform(post("/api/expenses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.amount").exists());
    }

    @Test
    @WithMockUser(username = "test1@test.com", roles = "USER")
    void updateExpense_ByOwner_ShouldSucceed() throws Exception {
        // Arrange
        Expense expense = testDataSeeder.getExpense1();
        ExpensePatchDto patchDto = new ExpensePatchDto();
        patchDto.setDescription("Updated expense description");
        patchDto.setAmount(new BigDecimal("150.00"));

        // Act & Assert
        mockMvc.perform(patch("/api/expenses/{id}", expense.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated expense description"))
                .andExpect(jsonPath("$.amount").value(150.00));
    }

    @Test
    @WithMockUser(username = "test2@test.com", roles = "USER")
    void updateExpense_ByNonOwner_ShouldReturn403() throws Exception {
        // Arrange
        Expense expense = testDataSeeder.getExpense1(); // Owned by user1
        ExpensePatchDto patchDto = new ExpensePatchDto();
        patchDto.setDescription("Unauthorized update");

        // Act & Assert
        mockMvc.perform(patch("/api/expenses/{id}", expense.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void updateExpense_ByAdmin_ShouldSucceed() throws Exception {
        // Arrange
        Expense expense = testDataSeeder.getExpense1();
        ExpensePatchDto patchDto = new ExpensePatchDto();
        patchDto.setDescription("Admin updated expense");

        // Act & Assert
        mockMvc.perform(patch("/api/expenses/{id}", expense.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Admin updated expense"));
    }

    @Test
    @WithMockUser(username = "test1@test.com", roles = "USER")
    void deleteExpense_ByOwner_ShouldSucceed() throws Exception {
        // Arrange
        User user = testDataSeeder.getTestUser1();
        Expense expense = new Expense("Expense to delete", new BigDecimal("50.00"), LocalDate.now(), user);
        expense = expenseRepository.save(expense);

        // Act & Assert
        mockMvc.perform(delete("/api/expenses/{id}", expense.getId())
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify deletion
        assert !expenseRepository.existsById(expense.getId());
    }

    @Test
    @WithMockUser(username = "test2@test.com", roles = "USER")
    void deleteExpense_ByNonOwner_ShouldReturn403() throws Exception {
        // Arrange
        Expense expense = testDataSeeder.getExpense1(); // Owned by user1

        // Act & Assert
        mockMvc.perform(delete("/api/expenses/{id}", expense.getId())
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteExpense_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Arrange
        Expense expense = testDataSeeder.getExpense1();

        // Act & Assert - Changed from 403 to 401 for unauthenticated access
        mockMvc.perform(delete("/api/expenses/{id}", expense.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}