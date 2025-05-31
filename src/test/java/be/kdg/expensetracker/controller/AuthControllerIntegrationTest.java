package be.kdg.expensetracker.controller;

import be.kdg.expensetracker.domain.User;
import be.kdg.expensetracker.repository.UserRepository;
import be.kdg.expensetracker.testutil.TestDataSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestDataSeeder testDataSeeder;

    @BeforeEach
    void setUp() {
        testDataSeeder.cleanupTestData();
        testDataSeeder.seedTestData();
    }

    @Test
    void loginPage_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Login")));
    }

    @Test
    void registerPage_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void registerUser_ValidData_ShouldCreateUser() throws Exception {
        // Act
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", "newuser@test.com")
                        .param("name", "New User")
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        // Assert
        User createdUser = userRepository.findByEmail("newuser@test.com").orElse(null);
        assertNotNull(createdUser);
        assertEquals("New User", createdUser.getName());
        assertTrue(passwordEncoder.matches("password123", createdUser.getPassword()));
    }

    @Test
    void registerUser_DuplicateEmail_ShouldShowError() throws Exception {
        // Arrange - user already exists from test data
        String existingEmail = testDataSeeder.getTestUser1().getEmail();

        // Act & Assert
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("email", existingEmail)
                        .param("name", "Duplicate User")
                        .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().hasErrors());
    }

    @Test
    void registerUser_MissingCSRF_ShouldReturn403() throws Exception {
        // Test without CSRF token - should be forbidden
        mockMvc.perform(post("/register")
                        .param("email", "test@test.com")
                        .param("name", "Test")
                        .param("password", "test"))
                .andExpect(status().isForbidden());
    }
}
