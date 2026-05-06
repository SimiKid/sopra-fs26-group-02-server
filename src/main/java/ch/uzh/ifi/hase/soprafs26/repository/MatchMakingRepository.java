package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs26.entity.MatchMaking;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;



@Repository("matchMakingRepository")
public interface MatchMakingRepository extends JpaRepository<MatchMaking, Long> {

    @Query("SELECT m FROM MatchMaking m WHERE m.id != :userId AND m.matchedGameCode IS NULL ORDER BY m.joinedAt ASC")
    Optional<MatchMaking> findFirstByIdNotAndMatchedGameCodeIsNullOrderByJoinedAtAsc(@Param("userId") Long userId);
}