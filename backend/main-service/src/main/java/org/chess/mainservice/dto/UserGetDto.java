package org.chess.mainservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserGetDto {
    private Long id;

    @NotBlank(message = "Username can't be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters long")
    private String username;

    @NotBlank(message = "Email can't be blank")
    @Email(message = "Incorrect Email format")
    @Size(max = 100, message = "Email must be less than 100 characters long")
    private String email;

    public UserGetDto() {
    }

    public UserGetDto(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}