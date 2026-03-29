package ch.uzh.ifi.hase.soprafs26.repository;
import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.GameSession;

@Repository("gameSessionRepository")
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    GameSession findByGameCode(String code);
    boolean existsByGameCode(String gameCode);
    List<GameSession> findByPlayer2IdIsNullAndCreatedAtBefore(LocalDateTime cutoff);
}


