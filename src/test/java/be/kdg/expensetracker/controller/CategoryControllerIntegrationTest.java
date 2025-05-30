package be.kdg.expensetracker.controller;

import be.kdg.expensetracker.testutil.TestDataSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataSeeder testDataSeeder;

    @BeforeEach
    void setUp() {
        testDataSeeder.cleanupTestData();
        testDataSeeder.seedTestData();
    }

    @Test
    void categoriesPage_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "USER")
    void categoriesPage_AuthenticatedUser_ShouldShowPage() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Categories")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("protected page")));
    }
}