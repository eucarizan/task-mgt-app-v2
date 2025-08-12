package dev.nj.tms.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewAccountDto(
        @Email(message = "Incorrect email format")
        @NotBlank(message = "Email should not be blank")
        String email,

        @NotBlank(message = "Password should not be blank")
        @Size(min = 6, message = "Password should be at least 6 characters")
        String password
) {
}
