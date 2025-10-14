package dev.nj.tms.token;

import dev.nj.tms.security.BearerTokenAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AccessTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenFilter.class);

    private final AuthenticationManager authenticationManager;
    private final List<EndpointMatcher> matchers = new ArrayList<>();
    private final PathPatternParser parser = new PathPatternParser();

    public AccessTokenFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;

        addRequestMatcher("/api/tasks", HttpMethod.GET);
        addRequestMatcher("/api/tasks", HttpMethod.POST);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (requiredBearerAuth(request)) {
            try {
                String token = extractBearerToken(request);
                BearerTokenAuthenticationToken authRequest = new BearerTokenAuthenticationToken(token);
                Authentication authenticated = authenticationManager.authenticate(authRequest);
                SecurityContextHolder.getContext().setAuthentication(authenticated);
                logger.debug("Successfully authenticated user: {}", authenticated.getName());
            } catch (AuthenticationException e) {
                logger.debug("Authentication failed: {}", e.getMessage());

                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(String.format(
                        "{\"error\": \"Authentication failed\", \"message\": \"%s\"}",
                        e.getMessage()
                ));

                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean requiredBearerAuth(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        return matchers.stream().anyMatch(matcher ->
                matcher.method.name().equals(method) && matcher.pattern.matches(PathContainer.parsePath(path))
        );
    }

    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isBlank()) {
            throw new BadCredentialsException("Missing Authorization header");
        }

        String[] parts = authHeader.split("\\s+");

        if (parts.length != 2) {
            throw new BadCredentialsException("Malformed authorization header. Expected format: 'Bearer <token')");
        }

        if (!"Bearer".equalsIgnoreCase(parts[0])) {
            throw new BadCredentialsException("Authorization header must start with 'Bearer'");
        }

        String token = parts[1].trim();
        if (token.isEmpty()) {
            throw new BadCredentialsException("Bearer token cannot be empty");
        }

        logger.debug("Extracted bearer token: {}", token);
        return token;
    }

    private void addRequestMatcher(String pattern, HttpMethod httpMethod) {
        this.matchers.add(new EndpointMatcher(parser.parse(pattern), httpMethod));
    }

    private record EndpointMatcher(PathPattern pattern, HttpMethod method) {

    }
}
