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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportsControllerIntegrationTest {

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
    void reportsPage_WithoutAuthentication_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void reportsPage_AsRegularUser_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "MANAGER")
    void reportsPage_AsManager_ShouldShowReports() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/dashboard"))
                .andExpect(model().attributeExists("totalExpenses", "totalAmount"))
                .andExpect(content().string(containsString("Reports Dashboard")));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void reportsPage_AsAdmin_ShouldShowReports() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/dashboard"))
                .andExpect(model().attributeExists("totalExpenses", "totalAmount"));
    }
}