package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Battle;

@Repository("battleRepository")
public interface BattleRepository extends JpaRepository<Battle, Long> {
    Battle findByGameId(Long gameId);
}