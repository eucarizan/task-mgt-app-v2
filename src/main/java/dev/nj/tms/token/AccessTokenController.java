package dev.nj.tms.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AccessTokenController {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenController.class);

    private final AccessTokenService tokenService;

    public AccessTokenController(AccessTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/token")
    public ResponseEntity<AccessTokenResponse> createToken(@AuthenticationPrincipal UserDetails userDetails) {
        String author = userDetails.getUsername();
        logger.info("Received request to create token by: {}", author);
        AccessTokenResponse tokenResponse = tokenService.createToken(author);
        logger.info("Successfully created token for: {}", author);
        return ResponseEntity.ok(tokenResponse);
    }
}
