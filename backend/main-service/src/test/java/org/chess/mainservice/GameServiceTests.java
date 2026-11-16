package org.chess.mainservice;

import org.chess.mainservice.dto.*;
import org.chess.mainservice.exception.GameException;
import org.chess.mainservice.model.*;
import org.chess.mainservice.repository.*;
import org.chess.mainservice.service.GameService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class GameServiceTests {
    @Autowired GameService service;
    @Autowired UserRepository users;
    @Autowired GameRepository games;
    @Autowired MoveRepository moves;
    Long white;
    Long black;
    Long gameId;

    @BeforeEach
    void startGame() {
        white = user();
        black = user();
        gameId = service.create(white).id();
        service.join(gameId, black);
    }

    @Test
    void savesLegalMoveAndRejectsRepeatWithoutChangingPosition() {
        GameDto state = play(white, "e2e4");
        assertThat(state.moves()).hasSize(1);
        assertThat(state.moves().getFirst().san()).isEqualTo("e4");
        assertThat(state.currentFen()).isEqualTo(moves.findByGameIdOrderByPlyAsc(gameId).getFirst().getFenAfter());
        assertThatThrownBy(() -> play(white, "e2e4")).isInstanceOf(GameException.class);
        assertThat(moves.findByGameIdOrderByPlyAsc(gameId)).hasSize(1);
    }

    @Test
    void rejectsIllegalMove() {
        assertThatThrownBy(() -> play(white, "e2e5")).hasMessage("Illegal move");
        assertThat(moves.findByGameIdOrderByPlyAsc(gameId)).isEmpty();
        assertThat(games.findById(gameId).orElseThrow().getCurrentFen()).isEqualTo(Game.INITIAL_FEN);
    }

    @Test
    void rejectsWrongTurn() {
        assertThatThrownBy(() -> play(black, "e7e5")).hasMessage("It is not your turn");
    }

    @Test
    void rejectsOutsider() {
        Long stranger = user();
        assertThatThrownBy(() -> service.get(gameId, stranger)).isInstanceOf(GameException.class);
        assertThatThrownBy(() -> service.checkSubscription(gameId, stranger)).isInstanceOf(GameException.class);
        assertThatThrownBy(() -> service.resign(gameId, stranger)).isInstanceOf(GameException.class);
        assertThatThrownBy(() -> play(stranger, "e2e4")).isInstanceOf(GameException.class);
    }

    @Test
    void finishesScholarsMateAndRejectsFurtherMoves() {
        play(white, "e2e4"); play(black, "e7e5");
        play(white, "d1h5"); play(black, "b8c6");
        play(white, "f1c4"); play(black, "g8f6");
        GameDto state = play(white, "h5f7");
        assertThat(state.result()).isEqualTo(GameResult.WHITE_WIN);
        assertThat(state.resultReason()).isEqualTo(GameResultReason.CHECKMATE);
        assertThat(state.finishedAt()).isNotNull();
        assertThat(state.moves().getLast().san()).isEqualTo("Qxf7#");
        assertThatThrownBy(() -> play(black, "a7a6")).hasMessage("Game is not in progress");
    }

    @Test
    void detectsStalemate() {
        position("7k/5K2/8/6Q1/8/8/8/8 w - - 0 1");
        assertThat(play(white, "g5g6").resultReason()).isEqualTo(GameResultReason.STALEMATE);
    }

    @Test
    void detectsInsufficientMaterial() {
        position("7k/8/8/8/8/8/2b5/K7 w - - 0 1");
        assertThat(play(white, "a1a2").resultReason()).isEqualTo(GameResultReason.INSUFFICIENT_MATERIAL);
    }

    @Test
    void detectsFiftyMoveRule() {
        position("7k/8/8/8/8/8/R7/K7 w - - 99 1");
        assertThat(play(white, "a2b2").resultReason()).isEqualTo(GameResultReason.FIFTY_MOVE_RULE);
    }

    @Test
    void detectsRepetitionFromPersistedHistory() {
        for (int round = 0; round < 2; round++) {
            play(white, "g1f3"); play(black, "g8f6");
            play(white, "f3g1"); play(black, "f6g8");
        }
        assertThat(service.get(gameId, white).resultReason()).isEqualTo(GameResultReason.THREEFOLD_REPETITION);
    }

    @Test
    void supportsCastlingEnPassantAndPromotion() {
        position("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        assertThat(play(white, "e1g1").moves().getLast().san()).isEqualTo("O-O");
        position("7k/8/8/3pP3/8/8/8/K7 w - d6 0 1");
        assertThat(play(white, "e5d6").moves().getLast().san()).isEqualTo("exd6");
        position("7k/P7/8/8/8/8/8/K7 w - - 0 1");
        assertThat(play(white, "a7a8q").moves().getLast().san()).isEqualTo("a8=Q+");
    }

    @Test
    void resignsAndIncludesGameInBothPlayersHistory() {
        GameDto state = service.resign(gameId, white);
        assertThat(state.result()).isEqualTo(GameResult.BLACK_WIN);
        assertThat(state.resultReason()).isEqualTo(GameResultReason.RESIGN);
        assertThat(service.history(white)).extracting(GameDto::id).contains(gameId);
        assertThat(service.history(black)).extracting(GameDto::id).contains(gameId);
    }

    @Test
    void rejectsOwnJoinAndMovesBeforeOpponentJoins() {
        Long waiting = service.create(white).id();
        assertThat(service.waiting()).extracting(GameDto::id).contains(waiting).doesNotContain(gameId);
        assertThatThrownBy(() -> service.join(waiting, white)).hasMessage("You cannot join your own game");
        assertThatThrownBy(() -> service.move(waiting, white, new MoveRequest("e2", "e4", null)))
                .hasMessage("Game is not in progress");
    }

    private void position(String fen) {
        games.findById(gameId).orElseThrow().setCurrentFen(fen);
    }

    private GameDto play(Long player, String uci) {
        return service.move(gameId, player, new MoveRequest(uci.substring(0, 2), uci.substring(2, 4),
                uci.length() == 5 ? uci.substring(4) : null));
    }

    private Long user() {
        String name = UUID.randomUUID().toString();
        return users.save(new User(name, name + "@example.com", "test")).getId();
    }
}
