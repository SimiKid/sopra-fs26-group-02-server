package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs26.entity.Player;
import java.util.List;


@Repository("playerRepository")
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByUserIdAndGameSessionId(Long userId, Long gameSessionId);
    List<Player> findByGameSessionId(Long gameSessionId);
}
