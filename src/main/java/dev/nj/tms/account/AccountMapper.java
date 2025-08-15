package dev.nj.tms.account;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    private final PasswordEncoder passwordEncoder;

    public AccountMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Account toEntity(String email, String password) {
        return new Account(
                email,
                passwordEncoder.encode(password)
        );
    }
}
