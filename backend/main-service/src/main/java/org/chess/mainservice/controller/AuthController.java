package org.chess.mainservice.controller;

import jakarta.validation.Valid;
import org.chess.mainservice.dto.UserGetDto;
import org.chess.mainservice.dto.UserUpsertDto;
import org.chess.mainservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserGetDto> register(@Valid @RequestBody UserUpsertDto dto) {
        UserGetDto createdUser = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}
