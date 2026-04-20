package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Battle;

@Repository("battleRepository")
public interface BattleRepository extends JpaRepository<Battle, Long> {
    Battle findByGameId(Long gameId);

    @Query("SELECT SUM(b.damageDealt) FROM Battle b WHERE b.gameId = :gameId")
    Integer sumDamageByGameId(@Param("gameId") Long gameId);

    @Query("SELECT COUNT(b) FROM Battle b WHERE b.gameId = :gameId")
    Integer countTurnsByGameId(@Param("gameId") Long gameId);
}
