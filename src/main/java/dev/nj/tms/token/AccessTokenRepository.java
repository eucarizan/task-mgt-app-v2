package dev.nj.tms.token;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends CrudRepository<AccessToken, Long> {
    Optional<AccessToken> findByToken(String token);

    int deleteByExpiresAtBefore(LocalDateTime dateTime);
}
