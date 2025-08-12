package dev.nj.tms.account;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody NewAccountDto newAccountDto) {
        logger.info("Received request to create a new user with email: {}", newAccountDto.email());
        Account newAccount = accountService.register(newAccountDto.email(), newAccountDto.password());
        logger.info("Successfully registered user with email: {}", newAccountDto.email());
        return ResponseEntity.ok(newAccount);
    }
}
