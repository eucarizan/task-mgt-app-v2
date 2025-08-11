package dev.nj.tms.account;

public interface AccountRepository {
    boolean existsByEmailIgnoreCase(String email);
    Account save(Account account);
}
