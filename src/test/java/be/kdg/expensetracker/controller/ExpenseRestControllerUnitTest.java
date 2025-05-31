package be.kdg.expensetracker.controller;

import be.kdg.expensetracker.domain.Expense;
import be.kdg.expensetracker.domain.User;
import be.kdg.expensetracker.dto.ExpenseDto;
import be.kdg.expensetracker.dto.ExpensePatchDto;
import be.kdg.expensetracker.dto.ExpensePostDto;
import be.kdg.expensetracker.mapper.ExpenseMapper;
import be.kdg.expensetracker.repository.ExpenseRepository;
import be.kdg.expensetracker.repository.UserRepository;
import be.kdg.expensetracker.service.ExpenseService;
import be.kdg.expensetracker.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseRestController.class)
@ActiveProfiles("test")
class ExpenseRestControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ExpenseRepository expenseRepository;

    @MockBean
    private ExpenseMapper expenseMapper;

    @MockBean
    private UserService userService;

    private User testUser;
    private Expense testExpense;
    private ExpenseDto testExpenseDto;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = new User("test@test.com", "Test User", "password");
        testUser.setId(1);
        testUser.setRole("ROLE_USER");

        testExpense = new Expense("Test Expense", new BigDecimal("50.00"), LocalDate.now(), testUser);
        testExpense.setId(1);

        testExpenseDto = new ExpenseDto();
        testExpenseDto.setId(1);
        testExpenseDto.setDescription("Test Expense");
        testExpenseDto.setAmount(new BigDecimal("50.00"));
        testExpenseDto.setDate(LocalDate.now());
        testExpenseDto.setUserId(1);
    }

    // Test 1: createExpense endpoint - Happy path
    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void createExpense_ValidData_ShouldReturn201() throws Exception {
        // Arrange
        ExpensePostDto postDto = new ExpensePostDto();
        postDto.setDescription("New Expense");
        postDto.setAmount(new BigDecimal("75.50"));
        postDto.setDate(LocalDate.now());

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(testUser));
        given(expenseService.createExpense(anyString(), any(BigDecimal.class), any(LocalDate.class), any(User.class)))
                .willReturn(testExpense);
        given(expenseMapper.toDto(any(Expense.class))).willReturn(testExpenseDto);

        // Act & Assert
        mockMvc.perform(post("/api/expenses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Expense"));

        // Verify interactions
        verify(userRepository, times(1)).findByEmail("test@test.com");
        verify(expenseService, times(1)).createExpense(
                eq("New Expense"),
                eq(new BigDecimal("75.50")),
                eq(LocalDate.now()),
                eq(testUser)
        );
        verify(expenseMapper, times(1)).toDto(testExpense);
    }

    // Test 2: createExpense - User not found
    @Test
    @WithMockUser(username = "nonexistent@test.com", roles = "USER")
    void createExpense_UserNotFound_ShouldReturn400() throws Exception {
        // Arrange
        ExpensePostDto postDto = new ExpensePostDto();
        postDto.setDescription("New Expense");
        postDto.setAmount(new BigDecimal("75.50"));
        postDto.setDate(LocalDate.now());

        given(userRepository.findByEmail("nonexistent@test.com")).willReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/expenses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isBadRequest());

        // Verify service was never called
        verify(userRepository, times(1)).findByEmail("nonexistent@test.com");
        verify(expenseService, never()).createExpense(anyString(), any(), any(), any());
    }

    // Test 3: createExpense - Invalid data (validation failure)
    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void createExpense_InvalidData_ShouldReturn400() throws Exception {
        // Arrange
        ExpensePostDto postDto = new ExpensePostDto();
        postDto.setDescription("A"); // Too short
        postDto.setAmount(new BigDecimal("-10.00")); // Negative
        postDto.setDate(LocalDate.now());

        // Act & Assert
        mockMvc.perform(post("/api/expenses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isBadRequest());

        // Verify service was never called due to validation failure
        verify(expenseService, never()).createExpense(anyString(), any(), any(), any());
    }

    // Test 4: createExpense - Without authentication and CSRF
    @Test
    void createExpense_Unauthenticated_ShouldReturn403() throws Exception {
        // Arrange
        ExpensePostDto postDto = new ExpensePostDto();
        postDto.setDescription("New Expense");
        postDto.setAmount(new BigDecimal("75.50"));
        postDto.setDate(LocalDate.now());

        // Act & Assert - Without CSRF token for unauthenticated request
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isForbidden()); // 403 as CSRF is missing

        // Verify no service calls were made
        verify(expenseService, never()).createExpense(anyString(), any(), any(), any());
    }

    // Test 5: createExpense - unauthenticated with CSRF
    @Test
    void createExpense_UnauthenticatedWithCSRF_ShouldReturn401() throws Exception {
        // Arrange
        ExpensePostDto postDto = new ExpensePostDto();
        postDto.setDescription("New Expense");
        postDto.setAmount(new BigDecimal("75.50"));
        postDto.setDate(LocalDate.now());

        // Act & Assert - With CSRF but no authentication
        mockMvc.perform(post("/api/expenses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isUnauthorized());

        // Verify no service calls were made
        verify(expenseService, never()).createExpense(anyString(), any(), any(), any());
    }

    // Test 6: updateExpense with verify
    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void updateExpense_ValidData_ShouldReturn200() throws Exception {
        // Arrange
        ExpensePatchDto patchDto = new ExpensePatchDto();
        patchDto.setDescription("Updated Expense");
        patchDto.setAmount(new BigDecimal("100.00"));

        given(expenseService.updateExpense(anyInt(), anyString(), any(BigDecimal.class), any()))
                .willReturn(testExpense);
        given(expenseMapper.toDto(any(Expense.class))).willReturn(testExpenseDto);

        // Act & Assert
        mockMvc.perform(patch("/api/expenses/{id}", 1)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        // Verify with specific arguments
        verify(expenseService, times(1)).updateExpense(
                eq(1),
                eq("Updated Expense"),
                eq(new BigDecimal("100.00")),
                isNull()
        );
    }

    // Test 7: deleteExpense with verify
    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void deleteExpense_ValidId_ShouldReturn204() throws Exception {
        // Arrange
        doNothing().when(expenseService).deleteExpense(1);

        // Act & Assert
        mockMvc.perform(delete("/api/expenses/{id}", 1)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify exact method call
        verify(expenseService, times(1)).deleteExpense(1);
        verifyNoMoreInteractions(expenseService);
    }

    // Test 8: updateExpense with Access Denied exception
    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void updateExpense_AccessDenied_ShouldReturn403() throws Exception {
        // Arrange
        ExpensePatchDto patchDto = new ExpensePatchDto();
        patchDto.setDescription("Updated Expense");

        given(expenseService.updateExpense(anyInt(), anyString(), any(), any()))
                .willThrow(new AccessDeniedException("Not authorized"));

        // Act & Assert
        mockMvc.perform(patch("/api/expenses/{id}", 1)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isForbidden());

        verify(expenseService, times(1)).updateExpense(eq(1), eq("Updated Expense"), isNull(), isNull());
    }

    // Test 9: deleteExpense with Access Denied exception
    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void deleteExpense_AccessDenied_ShouldReturn403() throws Exception {
        // Arrange
        doThrow(new AccessDeniedException("Not authorized")).when(expenseService).deleteExpense(1);

        // Act & Assert
        mockMvc.perform(delete("/api/expenses/{id}", 1)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // Verify the method was called before exception
        verify(expenseService, times(1)).deleteExpense(1);
    }
}