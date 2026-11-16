package org.chess.mainservice.service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.chess.mainservice.dto.GameDto;
import org.chess.mainservice.dto.MoveRequest;
import org.chess.mainservice.exception.GameException;
import org.chess.mainservice.model.*;
import org.chess.mainservice.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class GameService {
    private final GameRepository games;
    private final MoveRepository moves;
    private final UserRepository users;
    private final ApplicationEventPublisher events;

    public GameService(GameRepository games, MoveRepository moves, UserRepository users,
                       ApplicationEventPublisher events) {
        this.games = games;
        this.moves = moves;
        this.users = users;
        this.events = events;
    }

    public GameDto create(Long userId) {
        User user = users.findById(userId).orElseThrow(() ->
                new GameException(HttpStatus.UNAUTHORIZED, "User not found"));
        return GameDto.from(games.save(new Game(user)), List.of());
    }

    @Transactional(readOnly = true)
    public List<GameDto> waiting() {
        return games.findByStatus(GameStatus.WAITING).stream()
                .map(game -> GameDto.from(game, List.of())).toList();
    }

    @Transactional(readOnly = true)
    public List<GameDto> history(Long userId) {
        return games.findByPlayerId(userId).stream()
                .map(game -> GameDto.from(game, List.of())).toList();
    }

    public GameDto get(Long gameId, Long userId) {
        Game game = locked(gameId);
        requireViewer(game, userId);
        return snapshot(game);
    }

    @Transactional(readOnly = true)
    public void checkSubscription(Long gameId, Long userId) {
        Game game = games.findById(gameId).orElseThrow(() ->
                new GameException(HttpStatus.NOT_FOUND, "Game not found"));
        requireParticipant(game, userId);
    }

    public GameDto join(Long gameId, Long userId) {
        Game game = locked(gameId);
        if (game.getStatus() != GameStatus.WAITING) {
            throw new GameException(HttpStatus.BAD_REQUEST, "Game is not waiting for a player");
        }
        if (game.getWhitePlayer().getId().equals(userId)) {
            throw new GameException(HttpStatus.BAD_REQUEST, "You cannot join your own game");
        }
        game.setBlackPlayer(users.findById(userId).orElseThrow(() ->
                new GameException(HttpStatus.UNAUTHORIZED, "User not found")));
        game.setStatus(GameStatus.IN_PROGRESS);
        GameDto state = snapshot(game);
        events.publishEvent(new GameChanged("PLAYER_JOINED", state));
        return state;
    }

    public GameDto resign(Long gameId, Long userId) {
        Game game = locked(gameId);
        requireParticipant(game, userId);
        requireInProgress(game);
        finish(game, game.getWhitePlayer().getId().equals(userId) ? GameResult.BLACK_WIN : GameResult.WHITE_WIN,
                GameResultReason.RESIGN);
        GameDto state = snapshot(game);
        events.publishEvent(new GameChanged("GAME_OVER", state));
        return state;
    }

    public GameDto move(Long gameId, Long userId, MoveRequest request) {
        Game game = locked(gameId);
        requireParticipant(game, userId);
        requireInProgress(game);
        List<Move> history = moves.findByGameIdOrderByPlyAsc(gameId);
        Board board = restoreBoard(game, history);
        Long expectedPlayer = board.getSideToMove() == Side.WHITE
                ? game.getWhitePlayer().getId() : game.getBlackPlayer().getId();
        if (!expectedPlayer.equals(userId)) {
            throw new GameException(HttpStatus.BAD_REQUEST, "It is not your turn");
        }
        var legalMove = board.legalMoves().stream()
                .filter(candidate -> candidate.toString().equals(request.uci()))
                .findFirst().orElseThrow(() -> new GameException(HttpStatus.BAD_REQUEST, "Illegal move"));
        MoveList notation = new MoveList(game.getCurrentFen());
        notation.add(legalMove);
        String san = notation.toSanArray()[0];
        board.doMove(legalMove);
        int ply = history.isEmpty() ? 1 : history.getLast().getPly() + 1;
        Move saved = moves.save(new Move(game, ply, request.uci(), san, board.getFen()));
        game.setCurrentFen(board.getFen());
        determineResult(game, board);
        history.add(saved);
        GameDto state = GameDto.from(game, history);
        events.publishEvent(new GameChanged("MOVE", state));
        if (game.getStatus() == GameStatus.FINISHED) {
            events.publishEvent(new GameChanged("GAME_OVER", state));
        }
        return state;
    }

    private Board restoreBoard(Game game, List<Move> history) {
        Board board = new Board();
        board.loadFromFen(game.getCurrentFen());
        if (!history.isEmpty()) {
            // Repetition detection needs past positions, including after a server restart.
            Board previous = new Board();
            board.getHistory().clear();
            board.getHistory().add(previous.getZobristKey());
            for (Move move : history) {
                previous.loadFromFen(move.getFenAfter());
                board.getHistory().add(previous.getZobristKey());
            }
        }
        return board;
    }

    private void determineResult(Game game, Board board) {
        if (board.isMated()) {
            finish(game, board.getSideToMove() == Side.WHITE ? GameResult.BLACK_WIN : GameResult.WHITE_WIN,
                    GameResultReason.CHECKMATE);
        } else if (board.isStaleMate()) {
            finish(game, GameResult.DRAW, GameResultReason.STALEMATE);
        } else if (board.isInsufficientMaterial()) {
            finish(game, GameResult.DRAW, GameResultReason.INSUFFICIENT_MATERIAL);
        } else if (board.getHalfMoveCounter() >= 100) {
            finish(game, GameResult.DRAW, GameResultReason.FIFTY_MOVE_RULE);
        } else if (board.isRepetition()) {
            finish(game, GameResult.DRAW, GameResultReason.THREEFOLD_REPETITION);
        }
    }

    private void finish(Game game, GameResult result, GameResultReason reason) {
        game.setStatus(GameStatus.FINISHED);
        game.setResult(result);
        game.setResultReason(reason);
        game.setFinishedAt(LocalDateTime.now());
    }

    private Game locked(Long id) {
        return games.findLockedById(id).orElseThrow(() ->
                new GameException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    private GameDto snapshot(Game game) {
        return GameDto.from(game, moves.findByGameIdOrderByPlyAsc(game.getId()));
    }

    private void requireViewer(Game game, Long userId) {
        if (game.getStatus() != GameStatus.WAITING) {
            requireParticipant(game, userId);
        }
    }

    private void requireParticipant(Game game, Long userId) {
        boolean white = game.getWhitePlayer().getId().equals(userId);
        boolean black = game.getBlackPlayer() != null && game.getBlackPlayer().getId().equals(userId);
        if (!white && !black) {
            throw new GameException(HttpStatus.FORBIDDEN, "You are not a player in this game");
        }
    }

    private void requireInProgress(Game game) {
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new GameException(HttpStatus.BAD_REQUEST, "Game is not in progress");
        }
    }
}
