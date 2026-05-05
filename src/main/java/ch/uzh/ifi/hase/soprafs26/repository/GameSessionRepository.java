package ch.uzh.ifi.hase.soprafs26.repository;
import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.GameSession;

@Repository("gameSessionRepository")
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    GameSession findByGameCode(String code);
    boolean existsByGameCode(String gameCode);
    List<GameSession> findByPlayer2IdIsNullAndCreatedAtBefore(LocalDateTime cutoff);

    @Query("SELECT g FROM GameSession g WHERE (g.player1Id = :userId OR g.player2Id = :userId) "
         + "AND g.gameStatus = ch.uzh.ifi.hase.soprafs26.constant.GameStatus.FINISHED "
         + "ORDER BY g.createdAt DESC")
    List<GameSession> findFinishedGamesForUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(g) FROM GameSession g WHERE g.gameStatus = ch.uzh.ifi.hase.soprafs26.constant.GameStatus.BATTLE OR g.gameStatus = ch.uzh.ifi.hase.soprafs26.constant.GameStatus.FINISHED")
    long countStartedBattles();
}


