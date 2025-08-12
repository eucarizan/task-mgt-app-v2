package dev.nj.tms.account;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {
    boolean existsByEmailIgnoreCase(String email);
    Account save(Account account);
}
