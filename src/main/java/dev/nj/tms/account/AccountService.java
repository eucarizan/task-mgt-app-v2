package dev.nj.tms.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account register(String email, String password) {
        logger.info("Attempting to register user with email: {}", email);
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!email.matches("\\w+(\\.\\w+){0,2}@\\w+\\.\\w+")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("Password should be at least 6 characters");
        }

        if (accountRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException("Email already exists: " + email);
        }

        Account account = new Account(email, password);
        accountRepository.save(account);

        logger.info("Successfully registered user with email: {}", email);
        return account;
    }
}
