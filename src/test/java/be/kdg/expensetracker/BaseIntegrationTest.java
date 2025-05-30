package be.kdg.expensetracker;

import be.kdg.expensetracker.testutil.TestDataSeeder;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected TestDataSeeder testDataSeeder;

    @BeforeEach
    void setUpBase() {
        testDataSeeder.cleanupTestData();
        testDataSeeder.seedTestData();
    }
}