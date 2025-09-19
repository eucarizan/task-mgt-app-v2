package dev.nj.tms.token;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String token;

    public AccessToken() {}

    public AccessToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }
}
