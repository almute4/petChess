package org.chess.mainservice;

import jakarta.persistence.EntityManager;
import org.chess.mainservice.model.*;
import org.chess.mainservice.repository.GameRepository;
import org.chess.mainservice.repository.MoveRepository;
import org.chess.mainservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class GamePersistenceTests {
    private static final String AFTER_E4 =
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";
    private static final String AFTER_E5 =
            "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2";

    @Autowired
    private GameRepository games;

    @Autowired
    private MoveRepository moves;

    @Autowired
    private UserRepository users;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesWaitingGameWithInitialPositionAndWhitePlayer() {
        User white = createUser();
        Game game = games.saveAndFlush(new Game(white));
        entityManager.clear();

        Game stored = games.findById(game.getId()).orElseThrow();
        assertThat(stored.getWhitePlayer().getId()).isEqualTo(white.getId());
        assertThat(stored.getBlackPlayer()).isNull();
        assertThat(stored.getStatus()).isEqualTo(GameStatus.WAITING);
        assertThat(stored.getCurrentFen()).isEqualTo(Game.INITIAL_FEN);
        assertThat(stored.getCreatedAt()).isNotNull();
        assertThat(stored.getResult()).isNull();
        assertThat(stored.getResultReason()).isNull();
        assertThat(stored.getFinishedAt()).isNull();
        assertThat(games.findByStatus(GameStatus.WAITING))
                .extracting(Game::getId).contains(game.getId());
    }

    @Test
    void savesPlayersFinalPositionAndResult() {
        User white = createUser();
        User black = createUser();
        Game game = new Game(white);
        game.setBlackPlayer(black);
        game.setStatus(GameStatus.FINISHED);
        game.setResult(GameResult.BLACK_WIN);
        game.setResultReason(GameResultReason.RESIGN);
        game.setCurrentFen(AFTER_E4);
        LocalDateTime finishedAt = LocalDateTime.of(2026, 11, 16, 12, 0);
        game.setFinishedAt(finishedAt);
        games.saveAndFlush(game);
        entityManager.clear();

        Game stored = games.findById(game.getId()).orElseThrow();
        assertThat(stored.getBlackPlayer().getId()).isEqualTo(black.getId());
        assertThat(stored.getCurrentFen()).isEqualTo(AFTER_E4);
        assertThat(stored.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(stored.getResult()).isEqualTo(GameResult.BLACK_WIN);
        assertThat(stored.getResultReason()).isEqualTo(GameResultReason.RESIGN);
        assertThat(stored.getFinishedAt()).isEqualTo(finishedAt);
        assertThat(games.findByPlayerId(white.getId()))
                .extracting(Game::getId).containsExactly(game.getId());
        assertThat(games.findByPlayerId(black.getId()))
                .extracting(Game::getId).containsExactly(game.getId());
        assertThat(games.findByPlayerId(createUser().getId())).isEmpty();
    }

    @Test
    void loadsMovesInPlyOrderAndAllowsSamePlyInDifferentGames() {
        Game game = games.save(new Game(createUser()));
        Game otherGame = games.save(new Game(createUser()));
        moves.save(new Move(game, 2, "e7e5", "e5", AFTER_E5));
        moves.save(new Move(game, 1, "e2e4", "e4", AFTER_E4));
        moves.saveAndFlush(new Move(otherGame, 1, "e2e4", "e4", AFTER_E4));
        entityManager.clear();

        var stored = moves.findByGameIdOrderByPlyAsc(game.getId());
        assertThat(stored).extracting(Move::getPly).containsExactly(1, 2);
        assertThat(stored).extracting(Move::getUci).containsExactly("e2e4", "e7e5");
        assertThat(stored).extracting(Move::getSan).containsExactly("e4", "e5");
        assertThat(stored).extracting(Move::getFenAfter).containsExactly(AFTER_E4, AFTER_E5);
        assertThat(stored).allSatisfy(move -> {
            assertThat(move.getCreatedAt()).isNotNull();
            assertThat(move.getGame().getId()).isEqualTo(game.getId());
        });
    }

    @Test
    void databaseRejectsDuplicatePlyWithinGame() {
        Game game = games.save(new Game(createUser()));
        moves.saveAndFlush(new Move(game, 1, "e2e4", "e4", AFTER_E4));

        assertThatThrownBy(() ->
                moves.saveAndFlush(new Move(game, 1, "d2d4", "d4", AFTER_E4)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasStackTraceContaining("uk_moves_game_ply");
    }

    private User createUser() {
        String username = UUID.randomUUID().toString();
        return users.save(new User(username, username + "@example.com", "test-hash"));
    }
}
