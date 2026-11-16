package org.chess.mainservice.service;

import org.chess.mainservice.dto.GameDto;
import org.chess.mainservice.model.GameResult;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GameEventBroadcaster {
    private final SimpMessagingTemplate messaging;

    public GameEventBroadcaster(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @TransactionalEventListener
    public void publish(GameChanged event) {
        GameDto game = event.game();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", event.type());
        payload.put("gameId", game.id());
        payload.put("status", game.status());
        payload.put("currentFen", game.currentFen());
        payload.put("turn", game.turn());
        payload.put("blackPlayer", game.blackPlayer());
        payload.put("result", game.result());
        payload.put("resultReason", game.resultReason());
        payload.put("finalFen", game.currentFen());
        payload.put("state", game);
        if (!game.moves().isEmpty()) {
            payload.put("lastMove", game.moves().getLast());
        }
        if (game.result() == GameResult.WHITE_WIN) {
            payload.put("winnerUsername", game.whitePlayer().username());
        } else if (game.result() == GameResult.BLACK_WIN) {
            payload.put("winnerUsername", game.blackPlayer().username());
        }
        messaging.convertAndSend("/topic/games/" + game.id(), (Object) payload);
    }
}
