package dev.nj.tms.account;

public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account register(String email, String password) {
        if (accountRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException("Email already exists: " + email);
        }
        Account account = new Account(email, password);
        accountRepository.save(account);
        return account;
    }
}
