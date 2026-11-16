package org.chess.mainservice.dto;

import org.chess.mainservice.model.*;
import java.time.LocalDateTime;
import java.util.List;

public record GameDto(Long id, Player whitePlayer, Player blackPlayer,
                      GameStatus status, GameResult result, GameResultReason resultReason,
                      String currentFen, String turn, List<PlayedMove> moves,
                      LocalDateTime createdAt, LocalDateTime finishedAt) {
    public record Player(Long id, String username) {
        public static Player from(User user) {
            return user == null ? null : new Player(user.getId(), user.getUsername());
        }
    }

    public record PlayedMove(int ply, String uci, String san, String fenAfter, LocalDateTime createdAt) {
        public static PlayedMove from(Move move) {
            return new PlayedMove(move.getPly(), move.getUci(), move.getSan(),
                    move.getFenAfter(), move.getCreatedAt());
        }
    }

    public static GameDto from(Game game, List<Move> moves) {
        return new GameDto(game.getId(), Player.from(game.getWhitePlayer()), Player.from(game.getBlackPlayer()),
                game.getStatus(), game.getResult(), game.getResultReason(), game.getCurrentFen(),
                game.getCurrentFen().split(" ")[1].equals("w") ? "WHITE" : "BLACK",
                moves.stream().map(PlayedMove::from).toList(), game.getCreatedAt(), game.getFinishedAt());
    }
}
