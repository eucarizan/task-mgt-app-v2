package dev.nj.tms.account;

import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Long, Account> {
    boolean existsByEmailIgnoreCase(String email);
    Account save(Account account);
}
