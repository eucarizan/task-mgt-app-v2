package dev.nj.tms.token;

public interface AccessTokenService {
    String createToken(String email, String password);
}
