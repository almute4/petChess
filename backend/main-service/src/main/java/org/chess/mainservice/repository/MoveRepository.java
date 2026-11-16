package org.chess.mainservice.repository;

import org.chess.mainservice.model.Move;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoveRepository extends JpaRepository<Move, Long> {
    List<Move> findByGameIdOrderByPlyAsc(Long gameId);
}
