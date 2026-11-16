package org.chess.mainservice.repository;

import org.chess.mainservice.model.Game;
import org.chess.mainservice.model.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByStatus(GameStatus status);

    @Query("select g from Game g where g.whitePlayer.id = :userId or g.blackPlayer.id = :userId order by g.createdAt desc, g.id desc")
    List<Game> findByPlayerId(@Param("userId") Long userId);
}
