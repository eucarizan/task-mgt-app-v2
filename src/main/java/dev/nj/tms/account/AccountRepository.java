package dev.nj.tms.account;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {
    boolean existsByEmailIgnoreCase(String email);

    Optional<Account> findByEmailIgnoreCase(String email);
}
