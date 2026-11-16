package org.chess.mainservice.controller;

import jakarta.validation.Valid;
import org.chess.mainservice.dto.MoveRequest;
import org.chess.mainservice.dto.GameDto;
import org.chess.mainservice.exception.GameException;
import org.chess.mainservice.security.CustomUserDetails;
import org.chess.mainservice.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class GameSocketController {
    private static final Logger LOG = LoggerFactory.getLogger(GameSocketController.class);
    private final GameService games;

    public GameSocketController(GameService games) {
        this.games = games;
    }

    @MessageMapping("/games/{id}/move")
    public void move(@DestinationVariable Long id, @Valid @Payload MoveRequest move, Principal principal) {
        CustomUserDetails user = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        games.move(id, user.user().getId(), move);
    }

    @MessageMapping("/games/{id}/sync")
    @SendToUser(value = "/queue/game-state", broadcast = false)
    public GameDto sync(@DestinationVariable Long id, Principal principal) {
        CustomUserDetails user = (CustomUserDetails) ((Authentication) principal).getPrincipal();
        return games.get(id, user.user().getId());
    }

    @MessageExceptionHandler(GameException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public Map<String, String> gameError(GameException exception) {
        return Map.of("error", exception.getStatus().name(), "message", exception.getMessage());
    }

    @MessageExceptionHandler({
            org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException.class,
            org.springframework.messaging.converter.MessageConversionException.class
    })
    @SendToUser(value = "/queue/errors", broadcast = false)
    public Map<String, String> invalidMove(Exception exception) {
        return Map.of("error", "BAD_REQUEST", "message", "Invalid move format");
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public Map<String, String> unexpectedError(Exception exception) {
        LOG.error("WebSocket move failed", exception);
        return Map.of("error", "INTERNAL_SERVER_ERROR", "message", "Could not process move");
    }
}
