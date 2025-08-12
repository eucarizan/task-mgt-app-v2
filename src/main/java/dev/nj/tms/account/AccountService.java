package dev.nj.tms.account;

public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account register(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!email.matches("\\w+(\\.\\w+){0,2}@\\w+\\.\\w+")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (accountRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException("Email already exists: " + email);
        }

        Account account = new Account(email, password);
        accountRepository.save(account);
        return account;
    }
}
