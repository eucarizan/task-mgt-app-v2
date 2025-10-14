package dev.nj.tms.token;

import dev.nj.tms.security.BearerTokenAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AccessTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenFilter.class);

    private final AuthenticationManager authenticationManager;

    public AccessTokenFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.info("Extracted bearer token: {}", token );

            try {
                BearerTokenAuthenticationToken authRequest = new BearerTokenAuthenticationToken(token);
                Authentication authenticated = authenticationManager.authenticate(authRequest);
                SecurityContextHolder.getContext().setAuthentication(authenticated);
                logger.debug("Successfully authenticated user: {}", authenticated.getName());
            } catch (AuthenticationException e) {
                logger.debug("Authentication failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
