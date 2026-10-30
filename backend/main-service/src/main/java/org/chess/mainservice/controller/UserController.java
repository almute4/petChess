package org.chess.mainservice.controller;

import org.chess.mainservice.dto.UserUpsertDto;
import org.chess.mainservice.dto.UserGetDto;
import org.chess.mainservice.service.impl.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("/registration")
    public ResponseEntity<UserGetDto> createUser(@RequestBody UserUpsertDto userUpsertDto) {
        return ResponseEntity.ok(userService.createUser(userUpsertDto));
    }

    @GetMapping
    public ResponseEntity<List<UserGetDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserGetDto> getUserById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserGetDto> updateUserById(@PathVariable(name = "id") Long id, @RequestBody UserUpsertDto userUpsertDto) {
        UserGetDto updatedUser = userService.updateUserById(id, userUpsertDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedUser);
    }
}