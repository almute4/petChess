package org.chess.mainservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "moves", uniqueConstraints = {
    @UniqueConstraint(name = "uk_moves_game_ply", columnNames = {"game_id", "ply"})
})
public class Move extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private int ply;

    @Column(nullable = false, length = 5)
    private String uci;

    @Column(nullable = false, length = 10)
    private String san;

    @Column(name = "fen_after", nullable = false, length = 100)
    private String fenAfter;

    public Move() {
    }

    public Move(Game game, int ply, String uci, String san, String fenAfter) {
        this.game = game;
        this.ply = ply;
        this.uci = uci;
        this.san = san;
        this.fenAfter = fenAfter;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getPly() {
        return ply;
    }

    public void setPly(int ply) {
        this.ply = ply;
    }

    public String getUci() {
        return uci;
    }

    public void setUci(String uci) {
        this.uci = uci;
    }

    public String getSan() {
        return san;
    }

    public void setSan(String san) {
        this.san = san;
    }

    public String getFenAfter() {
        return fenAfter;
    }

    public void setFenAfter(String fenAfter) {
        this.fenAfter = fenAfter;
    }
}
