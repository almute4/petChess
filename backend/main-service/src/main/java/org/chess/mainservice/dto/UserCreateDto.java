package org.chess.mainservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserCreateDto {
    @NotBlank(message = "Username can't be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters long")
    private String username;

    @NotBlank(message = "Email can't be blank")
    @Email(message = "Incorrect Email format")
    @Size(max = 100, message = "Email must be less than 100 characters long")
    private String email;

    @NotBlank(message = "Password can't be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters long")
    private String passwordHash;

    public UserCreateDto() {
    }

    public UserCreateDto(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}