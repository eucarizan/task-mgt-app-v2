package dev.nj.tms.token;

public interface AccessTokenService {
    AccessTokenResponse createToken(String email, String password);
}
