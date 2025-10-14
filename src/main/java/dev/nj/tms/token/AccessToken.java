package dev.nj.tms.token;

import dev.nj.tms.account.Account;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class AccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String token;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    Account account;

    LocalDateTime expiresAt;

    public AccessToken() {}

    public AccessToken(String token, Account account, LocalDateTime expiresAt) {
        this.token = token;
        this.account = account;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Account getAccount() {
        return account;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
