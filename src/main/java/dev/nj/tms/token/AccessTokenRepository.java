package dev.nj.tms.token;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessTokenRepository extends CrudRepository<AccessToken, Long> {
}
