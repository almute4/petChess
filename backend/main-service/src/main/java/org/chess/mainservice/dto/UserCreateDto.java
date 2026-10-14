package org.chess.mainservice.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserCreateDto {
    @NotBlank(message = "Username can't be blank")
    @Size(min = 5, max = 50, message = "Username must be between 3 and 50 characters long")
    private String username;

    @NotBlank(message = "Email can't be blank")
    @Email(message = "Incorrect Email format")
    @Size(max = 100, message = "Email must be less than 100 characters long")
    private String email;

    @NotBlank(message = "Password can't be blank")
    @Size(min = 6, max = 100, message = "Password must be between 3 and 50 characters long")
    private String passwordHash;
}
