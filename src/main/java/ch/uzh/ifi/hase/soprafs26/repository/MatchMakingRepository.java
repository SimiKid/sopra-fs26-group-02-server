package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs26.entity.MatchMaking;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;



@Repository("matchMakingRepository")
public interface MatchMakingRepository extends JpaRepository<MatchMaking, Long> {

    // @Query disables method-name-derived LIMIT, so we must page explicitly —
    // returning Optional with >1 result throws IncorrectResultSizeDataAccessException.
    @Query("SELECT m FROM MatchMaking m WHERE m.id <> :userId AND m.matchedGameCode IS NULL ORDER BY m.joinedAt ASC")
    List<MatchMaking> findWaitingOpponents(@Param("userId") Long userId, Pageable pageable);

    default Optional<MatchMaking> findFirstByIdNotAndMatchedGameCodeIsNullOrderByJoinedAtAsc(Long userId) {
        return findWaitingOpponents(userId, Pageable.ofSize(1)).stream().findFirst();
    }

    void deleteByJoinedAtBeforeAndMatchedGameCodeIsNull(LocalDateTime joinedAt);
}