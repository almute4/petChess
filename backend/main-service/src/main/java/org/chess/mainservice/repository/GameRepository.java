package org.chess.mainservice.repository;

import org.chess.mainservice.model.Game;
import org.chess.mainservice.model.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface GameRepository extends JpaRepository<Game, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Game g where g.id = :id")
    Optional<Game> findLockedById(@Param("id") Long id);

    List<Game> findByStatus(GameStatus status);

    @Query("select g from Game g where g.whitePlayer.id = :userId or g.blackPlayer.id = :userId order by g.createdAt desc, g.id desc")
    List<Game> findByPlayerId(@Param("userId") Long userId);
}
