package org.chess.mainservice.controller;

import org.chess.mainservice.dto.UserUpsertDto;
import org.chess.mainservice.dto.UserGetDto;
import org.chess.mainservice.security.CustomUserDetails;
import org.chess.mainservice.service.impl.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserGetDto> getCurrentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(userService.getUserById(principal.user().getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserGetDto> getUserById(@PathVariable(name = "id") Long id,
                                                 @AuthenticationPrincipal CustomUserDetails principal) {
        requireOwner(id, principal);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserGetDto> updateUserById(@PathVariable(name = "id") Long id,
                                                    @RequestBody UserUpsertDto userUpsertDto,
                                                    @AuthenticationPrincipal CustomUserDetails principal) {
        requireOwner(id, principal);
        UserGetDto updatedUser = userService.updateUserById(id, userUpsertDto);
        return ResponseEntity.ok(updatedUser);
    }

    private void requireOwner(Long id, CustomUserDetails principal) {
        if (!id.equals(principal.user().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own account");
        }
    }
}
