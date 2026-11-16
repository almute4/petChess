package org.chess.mainservice.controller;

import jakarta.validation.Valid;
import org.chess.mainservice.dto.GameDto;
import org.chess.mainservice.dto.MoveRequest;
import org.chess.mainservice.security.CustomUserDetails;
import org.chess.mainservice.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService games;

    public GameController(GameService games) {
        this.games = games;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameDto create(@AuthenticationPrincipal CustomUserDetails user) {
        return games.create(user.user().getId());
    }

    @GetMapping
    public List<GameDto> waiting() {
        return games.waiting();
    }

    @GetMapping("/my")
    public List<GameDto> history(@AuthenticationPrincipal CustomUserDetails user) {
        return games.history(user.user().getId());
    }

    @GetMapping("/{id}")
    public GameDto get(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) {
        return games.get(id, user.user().getId());
    }

    @PostMapping("/{id}/join")
    public GameDto join(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) {
        return games.join(id, user.user().getId());
    }

    @PostMapping("/{id}/resign")
    public GameDto resign(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) {
        return games.resign(id, user.user().getId());
    }

    @PostMapping("/{id}/moves")
    public GameDto move(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user,
                        @Valid @RequestBody MoveRequest move) {
        return games.move(id, user.user().getId(), move);
    }
}
