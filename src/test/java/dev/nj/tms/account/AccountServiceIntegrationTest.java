package dev.nj.tms.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class AccountServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("tms_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AccountService accountService;

    @Test
    void shouldSaveAndRetrieveAccountFromDatabase() {
        String email = "user1@example.com";
        String password = "secure123";

        Account savedAccount = accountService.register(email, password);

        assertNotNull(savedAccount);
        assertEquals(email, savedAccount.getEmail());
        assertEquals(password, savedAccount.getPassword());

        assertThrows(EmailAlreadyExistsException.class, () ->
                accountService.register(email, password));
    }

    @Test
    void shouldThrowEmailAlreadyExistsExceptionWhenEmailAlreadyExists() {
        String email = "user2@example.com";
        String password = "secure123";

        Account firstAccount = accountService.register(email, password);
        assertNotNull(firstAccount);

        assertThrows(EmailAlreadyExistsException.class, () ->
                accountService.register(email, password));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenEmailIsNull() {
        String password = "secure123";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.register(null, password)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }
}
