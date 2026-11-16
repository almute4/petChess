package org.chess.mainservice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "games")
public class Game extends BaseEntity {
    public static final String INITIAL_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "white_id", nullable = false)
    private User whitePlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "black_id")
    private User blackPlayer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameStatus status = GameStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GameResult result;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_reason", length = 30)
    private GameResultReason resultReason;

    @Column(name = "current_fen", nullable = false, length = 100)
    private String currentFen = INITIAL_FEN;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    public Game() {
    }

    public Game(User whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    public User getWhitePlayer() {
        return whitePlayer;
    }

    public void setWhitePlayer(User whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    public User getBlackPlayer() {
        return blackPlayer;
    }

    public void setBlackPlayer(User blackPlayer) {
        this.blackPlayer = blackPlayer;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public GameResult getResult() {
        return result;
    }

    public void setResult(GameResult result) {
        this.result = result;
    }

    public GameResultReason getResultReason() {
        return resultReason;
    }

    public void setResultReason(GameResultReason resultReason) {
        this.resultReason = resultReason;
    }

    public String getCurrentFen() {
        return currentFen;
    }

    public void setCurrentFen(String currentFen) {
        this.currentFen = currentFen;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
