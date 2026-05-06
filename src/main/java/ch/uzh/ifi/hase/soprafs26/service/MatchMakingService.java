package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.hase.soprafs26.repository.MatchMakingRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.entity.MatchMaking;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.Optional;
import ch.uzh.ifi.hase.soprafs26.entity.GameSession;
import ch.uzh.ifi.hase.soprafs26.repository.GameSessionRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import ch.uzh.ifi.hase.soprafs26.service.GameSessionService;


@Service
@Transactional
public class MatchMakingService {

    private final MatchMakingRepository matchMakingRepository;
    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;  
    private final SimpMessagingTemplate messagingTemplate;
    private final GameSessionService gameSessionService;
    public MatchMakingService(MatchMakingRepository matchMakingRepository, UserRepository userRepository, GameSessionRepository gameSessionRepository, SimpMessagingTemplate messagingTemplate, GameSessionService gameSessionService) {
        this.matchMakingRepository = matchMakingRepository;
        this.userRepository = userRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.messagingTemplate = messagingTemplate;
        this.gameSessionService = gameSessionService;
    }


    public void joinRandomGameSession(Long userId) {
        MatchMaking myEntry =join(userId);

        MatchMaking opponent = searchOpponent(userId);
        if (opponent != null) {
            GameSession gameSession = createGameSession(userId, opponent.getId());
            myEntry.setMatchedGameCode(gameSession.getGameCode()); 
            opponent.setMatchedGameCode(gameSession.getGameCode());
            matchMakingRepository.save(myEntry);
            matchMakingRepository.save(opponent);

            messagingTemplate.convertAndSend("/topic/match/" + userId, gameSession.getId());
            messagingTemplate.convertAndSend("/topic/match/" + opponent.getId(), gameSession.getId());

            matchMakingRepository.delete(myEntry);
            matchMakingRepository.delete(opponent);
        }
        return;
    }


    private MatchMaking join(Long userId) {
        Optional<MatchMaking> existingEntry = matchMakingRepository.findById(userId);
        if (existingEntry.isPresent()) {
            existingEntry.get().setJoinedAt(LocalDateTime.now());
            return matchMakingRepository.save(existingEntry.get());
        }

        MatchMaking matchMaking = new MatchMaking();
        matchMaking.setId(userId);
        matchMaking.setUser(userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
        matchMaking.setJoinedAt(LocalDateTime.now());
        matchMaking.setMatchedGameCode(null);
        return matchMakingRepository.save(matchMaking);
    }

    private MatchMaking searchOpponent(Long userId) {
        Optional<MatchMaking> opponent = matchMakingRepository.findFirstByIdNotAndMatchedGameCodeIsNullOrderByJoinedAtAsc(userId);
        if (opponent.isPresent()) {
            return opponent.get();
        }
        return null;
    }

    private GameSession createGameSession(Long userId1, Long userId2) {
        GameSession newGameSession = new GameSession();
        newGameSession.setPlayer1Id(userId1);
        newGameSession.setPlayer2Id(userId2);
        return gameSessionService.createGameSession(newGameSession);
    }

    @Scheduled(fixedDelay = 5000) 
    public void purgeExpiredEntries() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(60);
        matchMakingRepository.deleteByJoinedAtBeforeAndMatchedGameCodeIsNull(cutoff);
    }

    public void leaveGameSession(Long userId) {
        MatchMaking myEntry = matchMakingRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        matchMakingRepository.delete(myEntry);
        return;
    }
}
